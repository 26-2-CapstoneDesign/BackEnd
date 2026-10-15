package com.youthguide.api.review.repository;

import com.youthguide.api.review.domain.CommunityPostComment;
import com.youthguide.api.review.repository.projection.CommunityPostCommentProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostCommentQueryRepository extends Repository<CommunityPostComment, Long> {

    @Query(
            value = """
                    SELECT
                        pc.comment_id AS commentId,
                        pc.parent_comment_id AS parentCommentId,
                        pc.reply_to_comment_id AS replyToCommentId,
                        reply_target_member.nickname AS replyToNickname,
                        pc.author_member_id AS authorMemberId,
                        CASE WHEN pc.deleted_at IS NULL THEN m.nickname ELSE NULL END AS authorNickname,
                        pc.body AS body,
                        pc.created_at AS createdAt,
                        pc.updated_at AS updatedAt,
                        pc.deleted_at AS deletedAt,
                        pc.lock_version AS lockVersion
                    FROM community_post_comment pc
                    LEFT JOIN member m
                        ON m.member_id = pc.author_member_id
                    LEFT JOIN community_post_comment reply_target
                        ON reply_target.comment_id = pc.reply_to_comment_id
                       AND reply_target.post_id = pc.post_id
                    LEFT JOIN member reply_target_member
                        ON reply_target_member.member_id = reply_target.author_member_id
                    WHERE pc.post_id = :postId
                      AND pc.parent_comment_id IS NULL
                    ORDER BY pc.created_at ASC, pc.comment_id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM community_post_comment pc
                    WHERE pc.post_id = :postId
                      AND pc.parent_comment_id IS NULL
                    """,
            nativeQuery = true
    )
    Page<CommunityPostCommentProjection> findTopLevelPage(
            @Param("postId") Long postId,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                pc.comment_id AS commentId,
                pc.parent_comment_id AS parentCommentId,
                pc.reply_to_comment_id AS replyToCommentId,
                reply_target_member.nickname AS replyToNickname,
                pc.author_member_id AS authorMemberId,
                m.nickname AS authorNickname,
                pc.body AS body,
                pc.created_at AS createdAt,
                pc.updated_at AS updatedAt,
                pc.deleted_at AS deletedAt,
                pc.lock_version AS lockVersion
            FROM community_post_comment pc
            JOIN member m
                ON m.member_id = pc.author_member_id
            LEFT JOIN community_post_comment reply_target
                ON reply_target.comment_id = pc.reply_to_comment_id
               AND reply_target.post_id = pc.post_id
            LEFT JOIN member reply_target_member
                ON reply_target_member.member_id = reply_target.author_member_id
            WHERE pc.parent_comment_id IN (:parentCommentIds)
            ORDER BY pc.created_at ASC, pc.comment_id ASC
            """, nativeQuery = true)
    List<CommunityPostCommentProjection> findRepliesByParentCommentIds(
            @Param("parentCommentIds") List<Long> parentCommentIds
    );
}
