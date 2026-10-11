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
