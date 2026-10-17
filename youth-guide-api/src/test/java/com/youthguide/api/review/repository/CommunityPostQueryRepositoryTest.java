package com.youthguide.api.review.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityPostQueryRepositoryTest {

    @Test
    void listQueryKeepsApprovedReadContract() throws NoSuchMethodException {
        Method method = CommunityPostQueryRepository.class.getMethod(
                "findPage",
                String.class,
                String.class,
                Long.class,
                Pageable.class
        );
        Query query = method.getAnnotation(Query.class);

        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("p.post_kind = :postKind")
                .contains("p.hidden_at IS NULL")
                .contains("report.reporter_member_id = :viewerMemberId")
                .contains("p.title LIKE :qPattern ESCAPE '\\\\'")
                .contains("p.body LIKE :qPattern ESCAPE '\\\\'")
                .contains("c.display_name LIKE :qPattern ESCAPE '\\\\'")
                .contains("COALESCE(pm.view_count, 0) AS viewCount")
                .contains("FROM community_post_like post_like")
                .contains("FROM community_post_comment post_comment")
                .contains("post_comment.deleted_at IS NULL")
                .contains("AS commentCount")
                .contains("ORDER BY p.created_at DESC, p.post_id DESC");

        assertThat(query.countQuery())
                .contains("p.post_kind = :postKind")
                .contains("p.hidden_at IS NULL")
                .contains("report.reporter_member_id = :viewerMemberId")
                .contains("c.display_name LIKE :qPattern ESCAPE '\\\\'")
                .doesNotContain("community_post_metric")
                .doesNotContain("community_post_like");
    }

    @Test
    void detailQueryUsesBoundPostKindParameter() throws NoSuchMethodException {
        Method method = CommunityPostQueryRepository.class.getMethod(
                "findDetail",
                Long.class,
                String.class,
                Long.class
        );
        Query query = method.getAnnotation(Query.class);

        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("p.post_id = :postId")
                .contains("p.post_kind = :postKind")
                .contains("p.hidden_at IS NULL")
                .contains("report.reporter_member_id = :viewerMemberId")
                .contains("COALESCE(pm.view_count, 0) AS viewCount")
                .contains("FROM community_post_like post_like")
                .contains("FROM community_post_comment post_comment")
                .contains("post_comment.deleted_at IS NULL")
                .contains("AS commentCount");
    }
}
