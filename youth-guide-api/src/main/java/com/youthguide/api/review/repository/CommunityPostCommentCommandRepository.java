package com.youthguide.api.review.repository;

import com.youthguide.api.review.domain.CommunityPostComment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityPostCommentCommandRepository extends JpaRepository<CommunityPostComment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT comment
            FROM CommunityPostComment comment
            WHERE comment.commentId = :commentId
              AND comment.postId = :postId
            """)
    Optional<CommunityPostComment> findForUpdateByCommentIdAndPostId(
            @Param("commentId") Long commentId,
            @Param("postId") Long postId
    );

    long countByParentCommentId(Long parentCommentId);
}
