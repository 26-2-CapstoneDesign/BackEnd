package com.youthguide.api.review.repository;

import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.repository.projection.CommunityPostDetailProjection;
import com.youthguide.api.review.repository.projection.CommunityPostSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityPostQueryRepository extends Repository<CommunityPost, Long> {

    @Query(
            value = """
                    SELECT
                        p.post_id AS postId,
                        p.certificate_id AS certificateId,
                        c.display_name AS certificateName,
                        p.title AS title,
                        m.nickname AS nickname,
                        p.created_at AS createdAt,
                        p.updated_at AS updatedAt,
                        COALESCE(pm.view_count, 0) AS viewCount,
                        (
                            SELECT COUNT(*)
                            FROM community_post_like post_like
                            WHERE post_like.post_id = p.post_id
                        ) AS likeCount
                    FROM community_post p
                    JOIN member m
                        ON m.member_id = p.author_member_id
                    LEFT JOIN certificate c
                        ON c.certificate_id = p.certificate_id
                    LEFT JOIN community_post_metric pm
                        ON pm.post_id = p.post_id
                    WHERE p.post_kind = :postKind
                      AND (
                          :qPattern IS NULL
                          OR p.title LIKE :qPattern ESCAPE '\\\\'
                          OR p.body LIKE :qPattern ESCAPE '\\\\'
                          OR c.display_name LIKE :qPattern ESCAPE '\\\\'
                      )
                    ORDER BY p.created_at DESC, p.post_id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM community_post p
                    LEFT JOIN certificate c
                        ON c.certificate_id = p.certificate_id
                    WHERE p.post_kind = :postKind
                      AND (
                          :qPattern IS NULL
                          OR p.title LIKE :qPattern ESCAPE '\\\\'
                          OR p.body LIKE :qPattern ESCAPE '\\\\'
                          OR c.display_name LIKE :qPattern ESCAPE '\\\\'
                      )
                    """,
            nativeQuery = true
    )
    Page<CommunityPostSummaryProjection> findPage(
            @Param("postKind") String postKind,
            @Param("qPattern") String qPattern,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT
                        p.post_id AS postId,
                        p.certificate_id AS certificateId,
                        c.display_name AS certificateName,
                        p.title AS title,
                        p.body AS body,
                        m.nickname AS nickname,
                        p.created_at AS createdAt,
                        p.updated_at AS updatedAt,
                        p.lock_version AS lockVersion,
                        COALESCE(pm.view_count, 0) AS viewCount,
                        (
                            SELECT COUNT(*)
                            FROM community_post_like post_like
                            WHERE post_like.post_id = p.post_id
                        ) AS likeCount
                    FROM community_post p
                    JOIN member m
                        ON m.member_id = p.author_member_id
                    LEFT JOIN certificate c
                        ON c.certificate_id = p.certificate_id
                    LEFT JOIN community_post_metric pm
                        ON pm.post_id = p.post_id
                    WHERE p.post_id = :postId
                      AND p.post_kind = :postKind
                    """,
            nativeQuery = true
    )
    Optional<CommunityPostDetailProjection> findDetail(
            @Param("postId") Long postId,
            @Param("postKind") String postKind
    );
}
