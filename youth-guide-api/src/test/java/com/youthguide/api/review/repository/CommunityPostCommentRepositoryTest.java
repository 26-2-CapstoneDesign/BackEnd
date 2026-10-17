package com.youthguide.api.review.repository;

import com.youthguide.api.review.domain.CommunityPostComment;
import jakarta.persistence.Column;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityPostCommentRepositoryTest {

    @Test
    void commentEntityUsesScalarForeignKeysAndVersionColumn() throws NoSuchFieldException {
        assertColumn("postId", "post_id", false);
        assertColumn("authorMemberId", "author_member_id", false);
        assertColumn("parentCommentId", "parent_comment_id", true);
        assertColumn("replyToCommentId", "reply_to_comment_id", true);
        assertColumn("body", "body", false);
        assertColumn("deletedAt", "deleted_at", true);

        Field version = CommunityPostComment.class.getDeclaredField("lockVersion");
        assertThat(version.isAnnotationPresent(Version.class)).isTrue();
        assertThat(version.getAnnotation(Column.class).name()).isEqualTo("lock_version");
    }

    @Test
    void commentLookupUsesPostScopedPessimisticWriteLock() throws NoSuchMethodException {
        Method method = CommunityPostCommentCommandRepository.class.getMethod(
                "findForUpdateByCommentIdAndPostId", Long.class, Long.class
        );

        assertThat(method.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(method.getAnnotation(Query.class).value())
                .contains("comment.commentId = :commentId")
                .contains("comment.postId = :postId");
    }

    @Test
    void topLevelQueryPaginatesThreadsInOldestOrderAndKeepsTombstones() throws NoSuchMethodException {
        Method method = CommunityPostCommentQueryRepository.class.getMethod(
                "findTopLevelPage", Long.class, Pageable.class
        );
        Query query = method.getAnnotation(Query.class);

        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("pc.parent_comment_id IS NULL")
                .contains("CASE WHEN pc.deleted_at IS NULL THEN m.nickname ELSE NULL END AS authorNickname")
                .contains("ORDER BY pc.created_at ASC, pc.comment_id ASC");
        assertThat(query.countQuery())
                .contains("pc.parent_comment_id IS NULL")
                .doesNotContain("deleted_at IS NULL");
    }

    @Test
    void replyQueryLoadsPageParentsInOneQueryAndOldestOrder() throws NoSuchMethodException {
        Method method = CommunityPostCommentQueryRepository.class.getMethod(
                "findRepliesByParentCommentIds", List.class
        );
        Query query = method.getAnnotation(Query.class);

        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("pc.parent_comment_id IN (:parentCommentIds)")
                .contains("pc.reply_to_comment_id AS replyToCommentId")
                .contains("reply_target_member.nickname AS replyToNickname")
                .contains("reply_target.comment_id = pc.reply_to_comment_id")
                .contains("ORDER BY pc.created_at ASC, pc.comment_id ASC");
    }

    private void assertColumn(String fieldName, String columnName, boolean nullable) throws NoSuchFieldException {
        Field field = CommunityPostComment.class.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);
        assertThat(column.name()).isEqualTo(columnName);
        assertThat(column.nullable()).isEqualTo(nullable);
    }
}
