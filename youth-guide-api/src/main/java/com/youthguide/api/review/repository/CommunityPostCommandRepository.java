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

    @Modifying
    @Query(value = "DELETE FROM community_post_like WHERE post_id = :postId", nativeQuery = true)
    int deletePostLikesByPostId(@Param("postId") Long postId);

    @Modifying
    @Query(value = "DELETE FROM community_post_metric WHERE post_id = :postId", nativeQuery = true)
    int deletePostMetricByPostId(@Param("postId") Long postId);

    @Modifying
    @Query(value = "DELETE FROM community_post_report WHERE post_id = :postId", nativeQuery = true)
    int deletePostReportsByPostId(@Param("postId") Long postId);

    void delete(CommunityPost post);
}
