package com.youthguide.api.review.repository;

import com.youthguide.api.review.domain.CommunityPostKind;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("mysql-integration")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_VIEW_COUNT_WRITE_TESTS", matches = "true")
@ActiveProfiles("local")
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.datasource.hikari.read-only=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
class CommunityPostViewCountMySqlIntegrationTest {

    private static final Pattern LOOPBACK_MYSQL_URL = Pattern.compile(
            "^jdbc:mysql://(localhost|127\\.0\\.0\\.1|\\[::1])(?::\\d+)?/.*$",
            Pattern.CASE_INSENSITIVE
    );

    @Autowired
    private CommunityPostCommandRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void requireLoopbackMySqlConnection() {
        String jdbcUrl = jdbcTemplate.execute(
                (ConnectionCallback<String>) connection -> connection.getMetaData().getURL()
        );
        Assumptions.assumeTrue(
                jdbcUrl != null && LOOPBACK_MYSQL_URL.matcher(jdbcUrl).matches(),
                "조회수 쓰기 통합 테스트는 localhost 또는 loopback MySQL에서만 실행합니다."
        );
    }

    @Test
    void insertsMissingMetricThenAtomicallyIncrementsExistingMetricWithinRollback() {
        Long postId = requireExamReviewPostId();
        jdbcTemplate.update("DELETE FROM community_post_metric WHERE post_id = ?", postId);

        int insertResult = repository.incrementPostViewCount(postId, CommunityPostKind.EXAM_REVIEW);
        assertThat(insertResult).isOne();
        assertThat(viewCount(postId)).isOne();

        int updateResult = repository.incrementPostViewCount(postId, CommunityPostKind.EXAM_REVIEW);
        assertThat(updateResult).isIn(1, 2);
        assertThat(viewCount(postId)).isEqualTo(2L);
    }

    @Test
    void doesNotCreateMetricForPostOutsideExamReviewScope() {
        List<Long> postIds = jdbcTemplate.query("""
                SELECT post_id
                  FROM community_post
                 WHERE post_kind <> ?
                 ORDER BY post_id
                 LIMIT 1
                """, (resultSet, rowNumber) -> resultSet.getLong("post_id"), CommunityPostKind.EXAM_REVIEW);
        Assumptions.assumeTrue(!postIds.isEmpty(), "EXAM_REVIEW가 아닌 기존 게시글이 없습니다.");

        Long postId = postIds.getFirst();
        long metricRowsBefore = metricRowCount(postId);
        long viewCountBefore = viewCount(postId);

        int affectedRows = repository.incrementPostViewCount(postId, CommunityPostKind.EXAM_REVIEW);

        assertThat(affectedRows).isZero();
        assertThat(metricRowCount(postId)).isEqualTo(metricRowsBefore);
        assertThat(viewCount(postId)).isEqualTo(viewCountBefore);
    }

    private Long requireExamReviewPostId() {
        List<Long> postIds = jdbcTemplate.query("""
                SELECT post_id
                  FROM community_post
                 WHERE post_kind = ?
                 ORDER BY post_id
                 LIMIT 1
                """, (resultSet, rowNumber) -> resultSet.getLong("post_id"), CommunityPostKind.EXAM_REVIEW);
        Assumptions.assumeTrue(!postIds.isEmpty(), "기존 EXAM_REVIEW 게시글이 없습니다.");
        return postIds.getFirst();
    }

    private long metricRowCount(Long postId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM community_post_metric WHERE post_id = ?",
                Long.class,
                postId
        );
        return count == null ? 0L : count;
    }

    private long viewCount(Long postId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(view_count), 0) FROM community_post_metric WHERE post_id = ?",
                Long.class,
                postId
        );
        return count == null ? 0L : count;
    }
}
