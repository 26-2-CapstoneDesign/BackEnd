package com.youthguide.api.review.repository;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityPostCommandRepositoryTest {

    @Test
    void deleteLookupUsesPessimisticWriteLockAndPostKind() throws NoSuchMethodException {
        Method method = CommunityPostCommandRepository.class.getMethod(
                "findForUpdateByPostIdAndPostKind",
                Long.class,
                String.class
        );

        assertThat(method.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(method.getAnnotation(Query.class).value())
                .contains("post.postId = :postId")
                .contains("post.postKind = :postKind");
    }

    @Test
    void associationDeletesUseBoundNativeQueries() throws NoSuchMethodException {
        assertNativeDelete("deletePostLikesByPostId", "community_post_like");
        assertNativeDelete("deletePostMetricByPostId", "community_post_metric");
        assertNativeDelete("deletePostReportsByPostId", "community_post_report");
        assertNativeDelete("deletePostRepliesByPostId", "community_post_comment");
        assertNativeDelete("deletePostCommentsByPostId", "community_post_comment");

        Query replies = CommunityPostCommandRepository.class
                .getMethod("deletePostRepliesByPostId", Long.class)
                .getAnnotation(Query.class);
        Query topLevel = CommunityPostCommandRepository.class
                .getMethod("deletePostCommentsByPostId", Long.class)
                .getAnnotation(Query.class);
        assertThat(replies.value()).contains("parent_comment_id IS NOT NULL");
        assertThat(topLevel.value()).contains("parent_comment_id IS NULL");
    }

    @Test
    void likeCommandsUseCompositeKeyParametersAndDatabaseCount() throws NoSuchMethodException {
        Method exists = CommunityPostCommandRepository.class.getMethod(
                "countPostLikeByPostIdAndMemberId", Long.class, Long.class
        );
        Query existsQuery = exists.getAnnotation(Query.class);
        assertThat(existsQuery.nativeQuery()).isTrue();
        assertThat(existsQuery.value())
                .contains("FROM community_post_like")
                .contains("post_id = :postId")
                .contains("member_id = :memberId");

        Method insert = CommunityPostCommandRepository.class.getMethod(
                "insertPostLike", Long.class, Long.class
        );
        Query insertQuery = insert.getAnnotation(Query.class);
        assertThat(insert.isAnnotationPresent(Modifying.class)).isTrue();
        assertThat(insertQuery.nativeQuery()).isTrue();
        assertThat(insertQuery.value())
                .contains("INSERT INTO community_post_like (post_id, member_id)")
                .contains("VALUES (:postId, :memberId)");

        Method delete = CommunityPostCommandRepository.class.getMethod(
                "deletePostLikeByPostIdAndMemberId", Long.class, Long.class
        );
        Query deleteQuery = delete.getAnnotation(Query.class);
        assertThat(delete.isAnnotationPresent(Modifying.class)).isTrue();
        assertThat(deleteQuery.nativeQuery()).isTrue();
        assertThat(deleteQuery.value())
                .contains("DELETE FROM community_post_like")
                .contains("post_id = :postId")
                .contains("member_id = :memberId");

        Method count = CommunityPostCommandRepository.class.getMethod("countPostLikesByPostId", Long.class);
        Query countQuery = count.getAnnotation(Query.class);
        assertThat(countQuery.nativeQuery()).isTrue();
        assertThat(countQuery.value())
                .contains("SELECT COUNT(*)")
                .contains("FROM community_post_like")
                .contains("post_id = :postId");
    }

    @Test
    void viewCountIncrementUsesAtomicUpsertAndPostKindGuard() throws NoSuchMethodException {
        Method increment = CommunityPostCommandRepository.class.getMethod(
                "incrementPostViewCount", Long.class, String.class
        );
        Query query = increment.getAnnotation(Query.class);

        assertThat(increment.isAnnotationPresent(Modifying.class)).isTrue();
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("INSERT INTO community_post_metric (post_id, view_count)")
                .contains("SELECT p.post_id, 1")
                .contains("FROM community_post p")
                .contains("p.post_id = :postId")
                .contains("p.post_kind = :postKind")
                .contains("ON DUPLICATE KEY UPDATE")
                .contains("view_count = view_count + 1")
                .doesNotContain("FOR UPDATE");
    }

    @Test
    void reportCommandsUseReporterUniqueKeyColumnsAndBoundValues() throws NoSuchMethodException {
        Method duplicateCheck = CommunityPostCommandRepository.class.getMethod(
                "countPostReportByPostIdAndReporterMemberId", Long.class, Long.class
        );
        Query duplicateQuery = duplicateCheck.getAnnotation(Query.class);
        assertThat(duplicateQuery.nativeQuery()).isTrue();
        assertThat(duplicateQuery.value())
                .contains("FROM community_post_report")
                .contains("post_id = :postId")
                .contains("reporter_member_id = :reporterMemberId");

        Method insert = CommunityPostCommandRepository.class.getMethod(
                "insertPostReport", Long.class, Long.class, String.class, String.class
        );
        Query insertQuery = insert.getAnnotation(Query.class);
        assertThat(insert.isAnnotationPresent(Modifying.class)).isTrue();
        assertThat(insertQuery.nativeQuery()).isTrue();
        assertThat(insertQuery.value())
                .contains("INSERT INTO community_post_report (post_id, reporter_member_id, reason, detail)")
                .contains("VALUES (:postId, :reporterMemberId, :reason, :detail)");

        Method count = CommunityPostCommandRepository.class.getMethod(
                "countPostReportsByPostId", Long.class
        );
        Query countQuery = count.getAnnotation(Query.class);
        assertThat(countQuery.nativeQuery()).isTrue();
        assertThat(countQuery.value())
                .contains("SELECT COUNT(*)")
                .contains("FROM community_post_report")
                .contains("post_id = :postId");
    }

    @Test
    void visibleViewIncrementExcludesHiddenAndReporterSpecificPosts() throws NoSuchMethodException {
        Method increment = CommunityPostCommandRepository.class.getMethod(
                "incrementVisiblePostViewCount", Long.class, String.class, Long.class
        );
        Query query = increment.getAnnotation(Query.class);

        assertThat(increment.isAnnotationPresent(Modifying.class)).isTrue();
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("p.post_kind = :postKind")
                .contains("p.hidden_at IS NULL")
                .contains("NOT EXISTS")
                .contains("FROM community_post_report report")
                .contains("report.reporter_member_id = :viewerMemberId")
                .contains("ON DUPLICATE KEY UPDATE")
                .contains("view_count = view_count + 1");
    }

    private void assertNativeDelete(String methodName, String tableName) throws NoSuchMethodException {
        Method method = CommunityPostCommandRepository.class.getMethod(methodName, Long.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.isAnnotationPresent(Modifying.class)).isTrue();
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("DELETE FROM " + tableName)
                .contains("post_id = :postId");
    }
}
