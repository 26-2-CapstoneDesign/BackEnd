package com.youthguide.api.review.repository;

import com.youthguide.api.review.domain.CommunityPost;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityPostCommandRepository extends Repository<CommunityPost, Long> {

    CommunityPost save(CommunityPost post);

    Optional<CommunityPost> findByPostIdAndPostKind(Long postId, String postKind);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT post
            FROM CommunityPost post
            WHERE post.postId = :postId
              AND post.postKind = :postKind
            """)
    Optional<CommunityPost> findForUpdateByPostIdAndPostKind(
            @Param("postId") Long postId,
            @Param("postKind") String postKind
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM community_post_like
            WHERE post_id = :postId
              AND member_id = :memberId
            """, nativeQuery = true)
    long countPostLikeByPostIdAndMemberId(
            @Param("postId") Long postId,
            @Param("memberId") Long memberId
    );

    @Modifying
    @Query(value = """
            INSERT INTO community_post_like (post_id, member_id)
            VALUES (:postId, :memberId)
            """, nativeQuery = true)
    int insertPostLike(
            @Param("postId") Long postId,
            @Param("memberId") Long memberId
    );

    @Modifying
    @Query(value = """
            DELETE FROM community_post_like
            WHERE post_id = :postId
              AND member_id = :memberId
            """, nativeQuery = true)
    int deletePostLikeByPostIdAndMemberId(
            @Param("postId") Long postId,
            @Param("memberId") Long memberId
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM community_post_like
            WHERE post_id = :postId
            """, nativeQuery = true)
    long countPostLikesByPostId(@Param("postId") Long postId);

    @Query(value = """
            SELECT COUNT(*)
            FROM community_post_report
            WHERE post_id = :postId
              AND reporter_member_id = :reporterMemberId
            """, nativeQuery = true)
    long countPostReportByPostIdAndReporterMemberId(
            @Param("postId") Long postId,
            @Param("reporterMemberId") Long reporterMemberId
    );

    @Modifying
    @Query(value = """
            INSERT INTO community_post_report (post_id, reporter_member_id, reason, detail)
            VALUES (:postId, :reporterMemberId, :reason, :detail)
            """, nativeQuery = true)
    int insertPostReport(
            @Param("postId") Long postId,
            @Param("reporterMemberId") Long reporterMemberId,
            @Param("reason") String reason,
            @Param("detail") String detail
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM community_post_report
            WHERE post_id = :postId
            """, nativeQuery = true)
    long countPostReportsByPostId(@Param("postId") Long postId);

    @Modifying
    @Query(value = """
            INSERT INTO community_post_metric (post_id, view_count)
            SELECT p.post_id, 1
            FROM community_post p
            WHERE p.post_id = :postId
              AND p.post_kind = :postKind
            ON DUPLICATE KEY UPDATE
                view_count = view_count + 1
            """, nativeQuery = true)
    int incrementPostViewCount(
            @Param("postId") Long postId,
            @Param("postKind") String postKind
    );

    @Modifying
    @Query(value = """
            INSERT INTO community_post_metric (post_id, view_count)
            SELECT p.post_id, 1
            FROM community_post p
            WHERE p.post_id = :postId
              AND p.post_kind = :postKind
              AND p.hidden_at IS NULL
              AND NOT EXISTS (
                  SELECT 1
                  FROM community_post_report report
                  WHERE report.post_id = p.post_id
                    AND report.reporter_member_id = :viewerMemberId
              )
            ON DUPLICATE KEY UPDATE
                view_count = view_count + 1
            """, nativeQuery = true)
    int incrementVisiblePostViewCount(
            @Param("postId") Long postId,
            @Param("postKind") String postKind,
            @Param("viewerMemberId") Long viewerMemberId
    );

    @Modifying
    @Query(value = "DELETE FROM community_post_like WHERE post_id = :postId", nativeQuery = true)
    int deletePostLikesByPostId(@Param("postId") Long postId);

    @Modifying
    @Query(value = "DELETE FROM community_post_metric WHERE post_id = :postId", nativeQuery = true)
    int deletePostMetricByPostId(@Param("postId") Long postId);

    @Modifying
    @Query(value = "DELETE FROM community_post_report WHERE post_id = :postId", nativeQuery = true)
    int deletePostReportsByPostId(@Param("postId") Long postId);

    @Modifying
    @Query(value = """
            DELETE FROM community_post_comment
            WHERE post_id = :postId
              AND parent_comment_id IS NOT NULL
            """, nativeQuery = true)
    int deletePostRepliesByPostId(@Param("postId") Long postId);

    @Modifying
    @Query(value = """
            DELETE FROM community_post_comment
            WHERE post_id = :postId
              AND parent_comment_id IS NULL
            """, nativeQuery = true)
    int deletePostCommentsByPostId(@Param("postId") Long postId);

    void delete(CommunityPost post);
}
