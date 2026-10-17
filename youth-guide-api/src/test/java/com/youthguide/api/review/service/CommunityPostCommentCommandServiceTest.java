package com.youthguide.api.review.service;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.domain.CommunityPostComment;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.dto.CommunityPostCommentCreateRequest;
import com.youthguide.api.review.dto.CommunityPostCommentUpdateRequest;
import com.youthguide.api.review.repository.CommunityPostCommandRepository;
import com.youthguide.api.review.repository.CommunityPostCommentCommandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityPostCommentCommandServiceTest {

    @Mock
    private CommunityPostCommandRepository postRepository;

    @Mock
    private CommunityPostCommentCommandRepository commentRepository;

    @InjectMocks
    private CommunityPostCommentCommandService service;

    @Test
    void createsTopLevelCommentAfterLockingExamReviewPost() {
        stubPost(10L);
        when(commentRepository.save(any(CommunityPostComment.class))).thenAnswer(invocation -> {
            CommunityPostComment comment = invocation.getArgument(0);
            ReflectionTestUtils.setField(comment, "commentId", 101L);
            return comment;
        });

        Long commentId = service.createComment(
                10L,
                20L,
                new CommunityPostCommentCreateRequest("댓글")
        );

        assertThat(commentId).isEqualTo(101L);
        ArgumentCaptor<CommunityPostComment> captor = ArgumentCaptor.forClass(CommunityPostComment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(ReflectionTestUtils.getField(captor.getValue(), "postId")).isEqualTo(10L);
        assertThat(ReflectionTestUtils.getField(captor.getValue(), "authorMemberId")).isEqualTo(20L);
        assertThat(captor.getValue().getParentCommentId()).isNull();
        assertThat(captor.getValue().getReplyToCommentId()).isNull();
        assertThat(captor.getValue().getBody()).isEqualTo("댓글");
        verify(postRepository).findForUpdateByPostIdAndPostKind(10L, CommunityPostKind.EXAM_REVIEW);
    }

    @Test
    void createsOneLevelReply() {
        stubPost(10L);
        CommunityPostComment parent = comment(101L, 10L, 20L, null, "부모", 0L, false);
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(parent));
        when(commentRepository.save(any(CommunityPostComment.class))).thenAnswer(invocation -> {
            CommunityPostComment reply = invocation.getArgument(0);
            ReflectionTestUtils.setField(reply, "commentId", 102L);
            return reply;
        });

        Long replyId = service.createReply(
                10L,
                101L,
                30L,
                new CommunityPostCommentCreateRequest("답글")
        );

        assertThat(replyId).isEqualTo(102L);
        ArgumentCaptor<CommunityPostComment> captor = ArgumentCaptor.forClass(CommunityPostComment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getParentCommentId()).isEqualTo(101L);
        assertThat(captor.getValue().getReplyToCommentId()).isNull();
    }

    @Test
    void flattensReplyToReplyUnderRootAndKeepsActualTarget() {
        stubPost(10L);
        CommunityPostComment root = comment(101L, 10L, 20L, null, "부모", 0L, false);
        CommunityPostComment reply = comment(102L, 10L, 30L, 101L, "답글", 0L, false);
        when(commentRepository.findForUpdateByCommentIdAndPostId(102L, 10L))
                .thenReturn(Optional.of(reply));
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(root));
        when(commentRepository.save(any(CommunityPostComment.class))).thenAnswer(invocation -> {
            CommunityPostComment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "commentId", 103L);
            return saved;
        });

        Long replyId = service.createReply(
                10L, 102L, 40L, new CommunityPostCommentCreateRequest("평탄화 답글")
        );

        assertThat(replyId).isEqualTo(103L);
        ArgumentCaptor<CommunityPostComment> captor = ArgumentCaptor.forClass(CommunityPostComment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getParentCommentId()).isEqualTo(101L);
        assertThat(captor.getValue().getReplyToCommentId()).isEqualTo(102L);
    }

    @Test
    void keepsRootDepthWhenReplyingToFlattenedReply() {
        stubPost(10L);
        CommunityPostComment root = comment(101L, 10L, 20L, null, "부모", 0L, false);
        CommunityPostComment flattenedReply = comment(
                103L, 10L, 40L, 101L, 102L, "B에게 답한 C", 0L, false
        );
        when(commentRepository.findForUpdateByCommentIdAndPostId(103L, 10L))
                .thenReturn(Optional.of(flattenedReply));
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(root));
        when(commentRepository.save(any(CommunityPostComment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createReply(10L, 103L, 50L, new CommunityPostCommentCreateRequest("C에게 답한 D"));

        ArgumentCaptor<CommunityPostComment> captor = ArgumentCaptor.forClass(CommunityPostComment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getParentCommentId()).isEqualTo(101L);
        assertThat(captor.getValue().getReplyToCommentId()).isEqualTo(103L);
    }

    @Test
    void rejectsMalformedDepthGreaterThanOne() {
        stubPost(10L);
        CommunityPostComment malformedTarget = comment(103L, 10L, 40L, 102L, "비정상 2단계", 0L, false);
        CommunityPostComment malformedParent = comment(102L, 10L, 30L, 101L, "대댓글", 0L, false);
        when(commentRepository.findForUpdateByCommentIdAndPostId(103L, 10L))
                .thenReturn(Optional.of(malformedTarget));
        when(commentRepository.findForUpdateByCommentIdAndPostId(102L, 10L))
                .thenReturn(Optional.of(malformedParent));

        assertBusinessError(
                () -> service.createReply(
                        10L, 103L, 50L, new CommunityPostCommentCreateRequest("더 깊은 답글")
                ),
                ErrorCode.COMMENT_DEPTH_EXCEEDED
        );
        verify(commentRepository, never()).save(any());
    }

    @Test
    void rejectsReplyToDeletedParent() {
        stubPost(10L);
        CommunityPostComment parent = comment(101L, 10L, 20L, null, "", 1L, true);
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(parent));

        assertBusinessError(
                () -> service.createReply(
                        10L, 101L, 30L, new CommunityPostCommentCreateRequest("답글")
                ),
                ErrorCode.INVALID_INPUT
        );
        verify(commentRepository, never()).save(any());
    }

    @Test
    void rejectsReplyToReplyWhenRootIsDeleted() {
        stubPost(10L);
        CommunityPostComment replyTarget = comment(102L, 10L, 30L, 101L, "답글", 0L, false);
        CommunityPostComment deletedRoot = comment(101L, 10L, 20L, null, "", 1L, true);
        when(commentRepository.findForUpdateByCommentIdAndPostId(102L, 10L))
                .thenReturn(Optional.of(replyTarget));
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(deletedRoot));

        assertBusinessError(
                () -> service.createReply(
                        10L, 102L, 40L, new CommunityPostCommentCreateRequest("답글")
                ),
                ErrorCode.INVALID_INPUT
        );
        verify(commentRepository, never()).save(any());
    }

    @Test
    void treatsCommentFromAnotherPostAsNotFound() {
        stubPost(10L);
        when(commentRepository.findForUpdateByCommentIdAndPostId(999L, 10L))
                .thenReturn(Optional.empty());

        assertBusinessError(
                () -> service.updateComment(
                        10L,
                        999L,
                        20L,
                        new CommunityPostCommentUpdateRequest("수정", "0")
                ),
                ErrorCode.COMMENT_NOT_FOUND
        );
    }

    @Test
    void updatesOwnedCommentWhenVersionMatches() {
        stubPost(10L);
        CommunityPostComment comment = comment(101L, 10L, 20L, null, "기존", 3L, false);
        LocalDateTime previousUpdatedAt = comment.getUpdatedAt();
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(comment));

        Long result = service.updateComment(
                10L,
                101L,
                20L,
                new CommunityPostCommentUpdateRequest("수정", "3")
        );

        assertThat(result).isEqualTo(101L);
        assertThat(comment.getBody()).isEqualTo("수정");
        assertThat(comment.getUpdatedAt()).isAfterOrEqualTo(previousUpdatedAt);
    }

    @Test
    void rejectsOtherAuthorAndStaleVersionWithoutChangingComment() {
        stubPost(10L);
        CommunityPostComment comment = comment(101L, 10L, 20L, null, "기존", 3L, false);
        LocalDateTime updatedAt = comment.getUpdatedAt();
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(comment));

        assertBusinessError(
                () -> service.updateComment(
                        10L, 101L, 99L, new CommunityPostCommentUpdateRequest("타인 수정", "3")
                ),
                ErrorCode.ACCESS_DENIED
        );
        assertBusinessError(
                () -> service.updateComment(
                        10L, 101L, 20L, new CommunityPostCommentUpdateRequest("오래된 수정", "2")
                ),
                ErrorCode.VERSION_CONFLICT
        );
        assertThat(comment.getBody()).isEqualTo("기존");
        assertThat(comment.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void physicallyDeletesTopLevelCommentWithoutReplies() {
        stubPost(10L);
        CommunityPostComment comment = comment(101L, 10L, 20L, null, "댓글", 0L, false);
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(comment));
        when(commentRepository.countByParentCommentId(101L)).thenReturn(0L);

        service.deleteComment(10L, 101L, 20L, 0L);

        verify(commentRepository).delete(comment);
        assertThat(comment.getDeletedAt()).isNull();
    }

    @Test
    void tombstonesTopLevelCommentWithReplies() {
        stubPost(10L);
        CommunityPostComment comment = comment(101L, 10L, 20L, null, "원본", 0L, false);
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(comment));
        when(commentRepository.countByParentCommentId(101L)).thenReturn(2L);

        service.deleteComment(10L, 101L, 20L, 0L);

        assertThat(comment.getBody()).isEmpty();
        assertThat(comment.getDeletedAt()).isNotNull();
        verify(commentRepository, never()).delete(comment);
    }

    @Test
    void deletesReplyAndRemovesLastTombstoneParent() {
        stubPost(10L);
        CommunityPostComment reply = comment(102L, 10L, 30L, 101L, "답글", 2L, false);
        CommunityPostComment parent = comment(101L, 10L, 20L, null, "", 1L, true);
        when(commentRepository.findForUpdateByCommentIdAndPostId(102L, 10L))
                .thenReturn(Optional.of(reply));
        when(commentRepository.countByParentCommentId(101L)).thenReturn(0L);
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(parent));

        service.deleteComment(10L, 102L, 30L, 2L);

        InOrder order = inOrder(commentRepository);
        order.verify(commentRepository).delete(reply);
        order.verify(commentRepository).flush();
        order.verify(commentRepository).countByParentCommentId(101L);
        order.verify(commentRepository).findForUpdateByCommentIdAndPostId(101L, 10L);
        order.verify(commentRepository).delete(parent);
    }

    @Test
    void keepsActiveParentWhenLastReplyIsDeleted() {
        stubPost(10L);
        CommunityPostComment reply = comment(102L, 10L, 30L, 101L, "답글", 2L, false);
        CommunityPostComment parent = comment(101L, 10L, 20L, null, "부모", 1L, false);
        when(commentRepository.findForUpdateByCommentIdAndPostId(102L, 10L))
                .thenReturn(Optional.of(reply));
        when(commentRepository.countByParentCommentId(101L)).thenReturn(0L);
        when(commentRepository.findForUpdateByCommentIdAndPostId(101L, 10L))
                .thenReturn(Optional.of(parent));

        service.deleteComment(10L, 102L, 30L, 2L);

        verify(commentRepository).delete(reply);
        verify(commentRepository, never()).delete(parent);
    }

    @Test
    void returnsPostNotFoundBeforeAnyCommentWrite() {
        when(postRepository.findForUpdateByPostIdAndPostKind(999L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.empty());

        assertBusinessError(
                () -> service.createComment(
                        999L, 20L, new CommunityPostCommentCreateRequest("댓글")
                ),
                ErrorCode.POST_NOT_FOUND
        );
        verifyNoInteractions(commentRepository);
        assertThat(CommunityPostCommentCommandService.class.isAnnotationPresent(Transactional.class)).isTrue();
    }

    private void stubPost(Long postId) {
        CommunityPost post = CommunityPost.create(
                1L,
                2L,
                CommunityPostKind.EXAM_REVIEW,
                "제목",
                "본문"
        );
        ReflectionTestUtils.setField(post, "postId", postId);
        ReflectionTestUtils.setField(post, "lockVersion", 0L);
        when(postRepository.findForUpdateByPostIdAndPostKind(postId, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
    }

    private CommunityPostComment comment(
            Long commentId,
            Long postId,
            Long authorMemberId,
            Long parentCommentId,
            String body,
            Long version,
            boolean deleted
    ) {
        CommunityPostComment comment = CommunityPostComment.create(
                postId,
                authorMemberId,
                parentCommentId,
                null,
                body
        );
        ReflectionTestUtils.setField(comment, "commentId", commentId);
        ReflectionTestUtils.setField(comment, "lockVersion", version);
        if (deleted) {
            comment.markDeleted();
        }
        return comment;
    }

    private CommunityPostComment comment(
            Long commentId,
            Long postId,
            Long authorMemberId,
            Long parentCommentId,
            Long replyToCommentId,
            String body,
            Long version,
            boolean deleted
    ) {
        CommunityPostComment comment = CommunityPostComment.create(
                postId,
                authorMemberId,
                parentCommentId,
                replyToCommentId,
                body
        );
        ReflectionTestUtils.setField(comment, "commentId", commentId);
        ReflectionTestUtils.setField(comment, "lockVersion", version);
        if (deleted) {
            comment.markDeleted();
        }
        return comment;
    }

    private void assertBusinessError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(expected);
    }
}
