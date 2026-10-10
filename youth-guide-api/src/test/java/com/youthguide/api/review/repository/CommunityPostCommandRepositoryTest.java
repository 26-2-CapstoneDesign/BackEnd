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
