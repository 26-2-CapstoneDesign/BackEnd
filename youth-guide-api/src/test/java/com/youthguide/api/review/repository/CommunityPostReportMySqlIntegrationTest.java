package com.youthguide.api.review.repository;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.domain.CommunityPostReportReason;
import com.youthguide.api.review.dto.CommunityPostReportCreateRequest;
import com.youthguide.api.review.service.CommunityPostCommandService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("mysql-integration")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_REPORT_WRITE_TESTS", matches = "true")
@ActiveProfiles("local")
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.datasource.hikari.read-only=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CommunityPostCommandService.class)
@Transactional
@Rollback
class CommunityPostReportMySqlIntegrationTest {

    private static final Pattern LOOPBACK_MYSQL_URL = Pattern.compile(
            "^jdbc:mysql://(localhost|127\\.0\\.0\\.1|\\[::1])(?::\\d+)?/.*$",
            Pattern.CASE_INSENSITIVE
    );

    @Autowired
    private CommunityPostCommandService service;

    @Autowired
    private CommunityPostCommandRepository commandRepository;

    @Autowired
    private CommunityPostQueryRepository queryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void requireLoopbackMySqlConnectionAndV2Schema() {
        String jdbcUrl = jdbcTemplate.execute(
                (ConnectionCallback<String>) connection -> connection.getMetaData().getURL()
        );
        Assumptions.assumeTrue(
                jdbcUrl != null && LOOPBACK_MYSQL_URL.matcher(jdbcUrl).matches(),
                "신고 쓰기 통합 테스트는 localhost 또는 loopback MySQL에서만 실행합니다."
        );

        Integer requiredColumns = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND (
                      (table_name = 'community_post_report' AND column_name IN ('reason', 'detail'))
                      OR (table_name = 'community_post' AND column_name = 'hidden_at')
                  )
                """, Integer.class);
        Assumptions.assumeTrue(requiredColumns != null && requiredColumns == 3, "V2 schema가 적용되지 않았습니다.");
    }

    @Test
    void databaseUniqueConstraintRejectsSameReporterForSamePost() {
        Fixture fixture = requireFixture(1);
        resetReportState(fixture.postId());
        Long reporterId = fixture.reporterIds().getFirst();

        commandRepository.insertPostReport(fixture.postId(), reporterId, "SPAM", null);

        assertThatThrownBy(() -> commandRepository.insertPostReport(
                fixture.postId(),
                reporterId,
                "ABUSE",
                null
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void personalHidingAndThirdReportGlobalHidingWorkAgainstMySql() {
        Fixture fixture = requireFixture(3);
        resetReportState(fixture.postId());
        Long firstReporter = fixture.reporterIds().get(0);
        Long secondReporter = fixture.reporterIds().get(1);
        Long thirdReporter = fixture.reporterIds().get(2);
        CommunityPostReportCreateRequest request = new CommunityPostReportCreateRequest(
                CommunityPostReportReason.SPAM,
                null
        );

        service.reportPost(fixture.postId(), firstReporter, request);

        assertThat(queryRepository.findDetail(
                fixture.postId(),
                CommunityPostKind.EXAM_REVIEW,
                firstReporter
        )).isEmpty();
        assertThat(queryRepository.findDetail(
                fixture.postId(),
                CommunityPostKind.EXAM_REVIEW,
                secondReporter
        )).isPresent();
        assertThat(queryRepository.findDetail(
                fixture.postId(),
                CommunityPostKind.EXAM_REVIEW,
                null
        )).isPresent();
        assertThat(hiddenAtCount(fixture.postId())).isZero();

        service.reportPost(fixture.postId(), secondReporter, request);
        assertThat(hiddenAtCount(fixture.postId())).isZero();

        service.reportPost(fixture.postId(), thirdReporter, request);
        entityManager.flush();
        entityManager.clear();

        assertThat(hiddenAtCount(fixture.postId())).isOne();
        assertThat(queryRepository.findDetail(
                fixture.postId(),
                CommunityPostKind.EXAM_REVIEW,
                null
        )).isEmpty();
        assertThatThrownBy(() -> service.reportPost(fixture.postId(), firstReporter, request))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    private Fixture requireFixture(int reporterCount) {
        List<Fixture> fixtures = jdbcTemplate.query("""
                SELECT p.post_id, p.author_member_id
                FROM community_post p
                WHERE p.post_kind = ?
                ORDER BY p.post_id
                LIMIT 1
                """, (resultSet, rowNumber) -> new Fixture(
                resultSet.getLong("post_id"),
                resultSet.getLong("author_member_id"),
                List.of()
        ), CommunityPostKind.EXAM_REVIEW);
        Assumptions.assumeTrue(!fixtures.isEmpty(), "기존 EXAM_REVIEW 게시글이 없습니다.");

        Fixture post = fixtures.getFirst();
        List<Long> reporterIds = jdbcTemplate.query("""
                SELECT member_id
                FROM member
                WHERE member_id <> ?
                ORDER BY member_id
                LIMIT ?
                """, (resultSet, rowNumber) -> resultSet.getLong("member_id"), post.authorMemberId(), reporterCount);
        Assumptions.assumeTrue(
                reporterIds.size() == reporterCount,
                "작성자를 제외한 기존 회원 fixture가 부족합니다."
        );
        return new Fixture(post.postId(), post.authorMemberId(), reporterIds);
    }

    private void resetReportState(Long postId) {
        jdbcTemplate.update("DELETE FROM community_post_report WHERE post_id = ?", postId);
        jdbcTemplate.update("UPDATE community_post SET hidden_at = NULL WHERE post_id = ?", postId);
        entityManager.clear();
    }

    private long hiddenAtCount(Long postId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM community_post WHERE post_id = ? AND hidden_at IS NOT NULL",
                Long.class,
                postId
        );
        return count == null ? 0L : count;
    }

    private record Fixture(Long postId, Long authorMemberId, List<Long> reporterIds) {
    }
}
