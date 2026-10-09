package com.youthguide.api.review.repository;

import com.youthguide.api.review.repository.projection.CommunityPostDetailProjection;
import com.youthguide.api.review.repository.projection.CommunityPostSummaryProjection;
import com.youthguide.api.review.support.LikePatternEscaper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("mysql-integration")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
@ActiveProfiles("local")
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.datasource.hikari.read-only=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(readOnly = true)
class CommunityPostQueryRepositoryMySqlIntegrationTest {

    private static final int PAGE_SIZE = 20;
    private static final List<String> TABLES = List.of(
            "community_post",
            "member",
            "certificate",
            "community_post_metric",
            "community_post_like"
    );

    @Autowired
    private CommunityPostQueryRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void readsMySqlMetadataThroughReadOnlyConnection() {
        Boolean readOnly = jdbcTemplate.execute((ConnectionCallback<Boolean>) Connection::isReadOnly);
        assertThat(readOnly).isTrue();

        Map<String, Object> metadata = jdbcTemplate.queryForMap("""
                SELECT
                    VERSION() AS mysqlVersion,
                    @@SESSION.sql_mode AS sessionSqlMode,
                    @@SESSION.time_zone AS sessionTimeZone
                """);

        String mysqlVersion = String.valueOf(metadata.get("mysqlVersion"));
        String sessionSqlMode = String.valueOf(metadata.get("sessionSqlMode"));
        String sessionTimeZone = String.valueOf(metadata.get("sessionTimeZone"));

        System.out.printf(
                "MySQL version=%s, sessionSqlMode=%s, sessionTimeZone=%s%n",
                mysqlVersion,
                sessionSqlMode,
                sessionTimeZone
        );

        assertThat(mysqlVersion).startsWith("8.4");
        assertThat(sessionSqlMode).doesNotContain("NO_BACKSLASH_ESCAPES");

        for (String table : TABLES) {
            Map<String, Object> row = jdbcTemplate.queryForMap("SHOW CREATE TABLE `" + table + "`");
            String ddl = String.valueOf(row.values().stream().skip(1).findFirst().orElse(""));
            assertThat(ddl)
                    .containsIgnoringCase("CREATE TABLE")
                    .contains("`" + table + "`");
        }
    }

    @Test
    void executesListCountProjectionAndFixedSort() {
        String postKind = requireExistingPostKind();
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

        Page<CommunityPostSummaryProjection> page = repository.findPage(postKind, null, pageRequest);

        long expectedTotal = countPosts(postKind);
        List<Long> expectedIds = findOrderedPostIds(postKind, 0, PAGE_SIZE);
        List<Long> actualIds = page.getContent().stream()
                .map(CommunityPostSummaryProjection::getPostId)
                .toList();

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(PAGE_SIZE);
        assertThat(page.getTotalElements()).isEqualTo(expectedTotal);
        assertThat(actualIds).containsExactlyElementsOf(expectedIds);

        CommunityPostSummaryProjection first = page.getContent().getFirst();
        assertThat(first.getPostId()).isNotNull();
        assertThat(first.getTitle()).isNotNull();
        assertThat(first.getNickname()).isNotNull();
        assertThat(first.getCreatedAt()).isNotNull();
        assertThat(first.getUpdatedAt()).isNotNull();
        assertThat(first.getViewCount()).isNotNull();
        assertThat(first.getLikeCount()).isNotNull();

        Map<String, Object> expectedCounts = jdbcTemplate.queryForMap("""
                SELECT
                    COALESCE(pm.view_count, 0) AS viewCount,
                    (SELECT COUNT(*)
                       FROM community_post_like post_like
                      WHERE post_like.post_id = p.post_id) AS likeCount
                  FROM community_post p
                  LEFT JOIN community_post_metric pm ON pm.post_id = p.post_id
                 WHERE p.post_id = ?
                """, first.getPostId());

        assertThat(first.getViewCount()).isEqualTo(number(expectedCounts.get("viewCount")));
        assertThat(first.getLikeCount()).isEqualTo(number(expectedCounts.get("likeCount")));
    }

    @Test
    void appliesPageableLimitAndOffset() {
        String postKind = requireExistingPostKind();
        long total = countPosts(postKind);
        Assumptions.assumeTrue(total > PAGE_SIZE, "21개 이상의 동일 post_kind 데이터가 없어 두 번째 페이지 검증을 건너뜁니다.");

        Page<CommunityPostSummaryProjection> page = repository.findPage(
                postKind,
                null,
                PageRequest.of(1, PAGE_SIZE)
        );

        List<Long> expectedIds = findOrderedPostIds(postKind, PAGE_SIZE, PAGE_SIZE);
        List<Long> actualIds = page.getContent().stream()
                .map(CommunityPostSummaryProjection::getPostId)
                .toList();

        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(PAGE_SIZE);
        assertThat(page.getTotalElements()).isEqualTo(total);
        assertThat(page.hasPrevious()).isTrue();
        assertThat(page.hasNext()).isEqualTo(total > PAGE_SIZE * 2L);
        assertThat(actualIds).containsExactlyElementsOf(expectedIds);
    }

    @Test
    void executesDetailProjection() {
        String postKind = requireExistingPostKind();
        Long postId = requireFirstPostId(postKind);

        Optional<CommunityPostDetailProjection> result = repository.findDetail(postId, postKind);

        assertThat(result).hasValueSatisfying(detail -> {
            assertThat(detail.getPostId()).isEqualTo(postId);
            assertThat(detail.getTitle()).isNotNull();
            assertThat(detail.getBody()).isNotNull();
            assertThat(detail.getNickname()).isNotNull();
            assertThat(detail.getCreatedAt()).isNotNull();
            assertThat(detail.getUpdatedAt()).isNotNull();
            assertThat(detail.getLockVersion()).isNotNull();
            assertThat(detail.getViewCount()).isNotNull();
            assertThat(detail.getLikeCount()).isNotNull();
        });
    }

    @Test
    void searchesTitleWithEscapedBoundPattern() {
        verifyExistingSearchFixture(findTitleFixture(), "제목 검색을 검증할 기존 데이터가 없습니다.");
    }

    @Test
    void searchesBodyWithEscapedBoundPattern() {
        verifyExistingSearchFixture(findBodyFixture(), "본문 검색을 검증할 기존 데이터가 없습니다.");
    }

    @Test
    void searchesCertificateNameWithEscapedBoundPattern() {
        verifyExistingSearchFixture(findCertificateFixture(), "자격증명 검색을 검증할 기존 데이터가 없습니다.");
    }

    @Test
    void returnsZeroWhenMetricRowIsMissing() {
        Optional<PostIdentity> fixture = queryIdentity("""
                SELECT p.post_id, p.post_kind
                  FROM community_post p
                  LEFT JOIN community_post_metric pm ON pm.post_id = p.post_id
                 WHERE pm.post_id IS NULL
                 ORDER BY p.created_at DESC, p.post_id DESC
                 LIMIT 1
                """);
        Assumptions.assumeTrue(fixture.isPresent(), "metric 행이 없는 기존 게시글이 없어 검증을 건너뜁니다.");

        PostIdentity post = fixture.orElseThrow();
        Optional<CommunityPostDetailProjection> result = repository.findDetail(post.postId(), post.postKind());

        assertThat(result).hasValueSatisfying(detail -> assertThat(detail.getViewCount()).isZero());
    }

    @Test
    void returnsZeroWhenPostHasNoLikes() {
        Optional<PostIdentity> fixture = queryIdentity("""
                SELECT p.post_id, p.post_kind
                  FROM community_post p
                 WHERE NOT EXISTS (
                           SELECT 1
                             FROM community_post_like post_like
                            WHERE post_like.post_id = p.post_id
                       )
                 ORDER BY p.created_at DESC, p.post_id DESC
                 LIMIT 1
                """);
        Assumptions.assumeTrue(fixture.isPresent(), "좋아요가 없는 기존 게시글이 없어 검증을 건너뜁니다.");

        PostIdentity post = fixture.orElseThrow();
        Optional<CommunityPostDetailProjection> result = repository.findDetail(post.postId(), post.postKind());

        assertThat(result).hasValueSatisfying(detail -> assertThat(detail.getLikeCount()).isZero());
    }

    @Test
    void treatsExistingLikeWildcardCharactersAsLiteralText() {
        int verifiedCharacters = 0;
        for (String character : List.of("%", "_", "\\")) {
            Optional<SearchFixture> fixture = findSpecialCharacterFixture(character);
            if (fixture.isEmpty()) {
                continue;
            }

            SearchFixture post = fixture.orElseThrow();
            Page<CommunityPostSummaryProjection> page = repository.findPage(
                    post.postKind(),
                    LikePatternEscaper.toContainsPattern(character),
                    PageRequest.of(0, PAGE_SIZE)
            );

            assertThat(page.getTotalElements()).isEqualTo(countContaining(post.postKind(), character));
            verifiedCharacters++;
        }

        Assumptions.assumeTrue(
                verifiedCharacters > 0,
                "%, _, \\ 문자가 검색 대상 컬럼에 포함된 기존 데이터가 없어 LIKE escape 검증을 건너뜁니다."
        );
    }

    private String requireExistingPostKind() {
        List<String> postKinds = jdbcTemplate.query("""
                SELECT post_kind
                  FROM community_post
                 GROUP BY post_kind
                 ORDER BY COUNT(*) DESC, post_kind
                 LIMIT 1
                """, (resultSet, rowNumber) -> resultSet.getString("post_kind"));
        Assumptions.assumeTrue(!postKinds.isEmpty(), "기존 community_post 데이터가 없어 검증을 건너뜁니다.");
        return postKinds.getFirst();
    }

    private Long requireFirstPostId(String postKind) {
        List<Long> postIds = findOrderedPostIds(postKind, 0, 1);
        Assumptions.assumeTrue(!postIds.isEmpty(), "상세 조회에 사용할 기존 게시글이 없습니다.");
        return postIds.getFirst();
    }

    private long countPosts(String postKind) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM community_post WHERE post_kind = ?",
                Long.class,
                postKind
        );
        return count == null ? 0L : count;
    }

    private List<Long> findOrderedPostIds(String postKind, int offset, int limit) {
        return jdbcTemplate.query("""
                SELECT p.post_id
                  FROM community_post p
                  JOIN member m ON m.member_id = p.author_member_id
                  LEFT JOIN certificate c ON c.certificate_id = p.certificate_id
                  LEFT JOIN community_post_metric pm ON pm.post_id = p.post_id
                 WHERE p.post_kind = ?
                 ORDER BY p.created_at DESC, p.post_id DESC
                 LIMIT ? OFFSET ?
                """, (resultSet, rowNumber) -> resultSet.getLong("post_id"), postKind, limit, offset);
    }

    private Optional<PostIdentity> queryIdentity(String sql) {
        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new PostIdentity(
                        resultSet.getLong("post_id"),
                        resultSet.getString("post_kind")
                )
        ).stream().findFirst();
    }

    private Optional<SearchFixture> findTitleFixture() {
        return querySearchFixture("""
                SELECT p.post_id, p.post_kind, p.title AS source_text
                  FROM community_post p
                 WHERE CHAR_LENGTH(TRIM(p.title)) > 0
                 ORDER BY p.created_at DESC, p.post_id DESC
                 LIMIT 1
                """);
    }

    private Optional<SearchFixture> findBodyFixture() {
        return querySearchFixture("""
                SELECT p.post_id, p.post_kind, p.body AS source_text
                  FROM community_post p
                 WHERE CHAR_LENGTH(TRIM(p.body)) > 0
                 ORDER BY p.created_at DESC, p.post_id DESC
                 LIMIT 1
                """);
    }

    private Optional<SearchFixture> findCertificateFixture() {
        return querySearchFixture("""
                SELECT p.post_id, p.post_kind, c.display_name AS source_text
                  FROM community_post p
                  JOIN certificate c ON c.certificate_id = p.certificate_id
                 WHERE CHAR_LENGTH(TRIM(c.display_name)) > 0
                 ORDER BY p.created_at DESC, p.post_id DESC
                 LIMIT 1
                """);
    }

    private Optional<SearchFixture> findSpecialCharacterFixture(String character) {
        return jdbcTemplate.query("""
                SELECT p.post_id, p.post_kind, ? AS source_text
                  FROM community_post p
                  LEFT JOIN certificate c ON c.certificate_id = p.certificate_id
                 WHERE LOCATE(?, p.title) > 0
                    OR LOCATE(?, p.body) > 0
                    OR LOCATE(?, c.display_name) > 0
                 ORDER BY p.created_at DESC, p.post_id DESC
                 LIMIT 1
                """, (resultSet, rowNumber) -> new SearchFixture(
                        resultSet.getLong("post_id"),
                        resultSet.getString("post_kind"),
                        resultSet.getString("source_text")
                ), character, character, character, character).stream().findFirst();
    }

    private Optional<SearchFixture> querySearchFixture(String sql) {
        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new SearchFixture(
                        resultSet.getLong("post_id"),
                        resultSet.getString("post_kind"),
                        resultSet.getString("source_text")
                )
        ).stream().findFirst();
    }

    private void verifyExistingSearchFixture(Optional<SearchFixture> fixture, String skipReason) {
        Assumptions.assumeTrue(fixture.isPresent(), skipReason);
        SearchFixture post = fixture.orElseThrow();
        String keyword = prefix(post.sourceText(), 24);

        Page<CommunityPostSummaryProjection> page = repository.findPage(
                post.postKind(),
                LikePatternEscaper.toContainsPattern(keyword),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(page.getTotalElements()).isEqualTo(countContaining(post.postKind(), keyword));
        assertThat(page.getTotalElements()).isPositive();
    }

    private long countContaining(String postKind, String keyword) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                  FROM community_post p
                  LEFT JOIN certificate c ON c.certificate_id = p.certificate_id
                 WHERE p.post_kind = ?
                   AND (
                       LOCATE(?, p.title) > 0
                       OR LOCATE(?, p.body) > 0
                       OR LOCATE(?, c.display_name) > 0
                   )
                """, Long.class, postKind, keyword, keyword, keyword);
        return count == null ? 0L : count;
    }

    private static String prefix(String value, int maximumCodePoints) {
        String trimmed = value.trim();
        int codePoints = trimmed.codePointCount(0, trimmed.length());
        int end = trimmed.offsetByCodePoints(0, Math.min(codePoints, maximumCodePoints));
        return trimmed.substring(0, end);
    }

    private static long number(Object value) {
        return ((Number) value).longValue();
    }

    private record PostIdentity(Long postId, String postKind) {
    }

    private record SearchFixture(Long postId, String postKind, String sourceText) {
    }
}
