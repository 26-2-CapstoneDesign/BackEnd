package com.youthguide.api.review.service;

import com.youthguide.api.certificate.repository.CertificateRepository;
import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.dto.CommunityPostLikeResponse;
import com.youthguide.api.review.repository.CommunityPostCommandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityPostLikeServiceTest {

    @Mock
    private CommunityPostCommandRepository repository;

    @Mock
    private CertificateRepository certificateRepository;

    @InjectMocks
    private CommunityPostCommandService service;

    @Test
    void addsFirstLikeAndReturnsCurrentCountInRepositoryOrder() {
        CommunityPost post = existingPost();
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
        when(repository.countPostLikeByPostIdAndMemberId(300L, 100L)).thenReturn(0L);
        when(repository.insertPostLike(300L, 100L)).thenReturn(1);
        when(repository.countPostLikesByPostId(300L)).thenReturn(3L);

        CommunityPostLikeResponse response = service.addLike(300L, 100L);

        assertThat(response.liked()).isTrue();
        assertThat(response.likeCount()).isEqualTo(3L);
        InOrder order = inOrder(repository);
        order.verify(repository).findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW);
        order.verify(repository).countPostLikeByPostIdAndMemberId(300L, 100L);
        order.verify(repository).insertPostLike(300L, 100L);
        order.verify(repository).countPostLikesByPostId(300L);
    }

    @Test
    void keepsRepeatedPutIdempotentWithoutDuplicateInsert() {
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(existingPost()));
        when(repository.countPostLikeByPostIdAndMemberId(300L, 100L)).thenReturn(1L);
        when(repository.countPostLikesByPostId(300L)).thenReturn(3L);

        CommunityPostLikeResponse response = service.addLike(300L, 100L);

        assertThat(response).isEqualTo(new CommunityPostLikeResponse(true, 3L));
        verify(repository, never()).insertPostLike(300L, 100L);
    }

    @Test
    void removesExistingLikeAndReturnsCurrentCountInRepositoryOrder() {
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(existingPost()));
        when(repository.countPostLikeByPostIdAndMemberId(300L, 100L)).thenReturn(1L);
        when(repository.deletePostLikeByPostIdAndMemberId(300L, 100L)).thenReturn(1);
        when(repository.countPostLikesByPostId(300L)).thenReturn(2L);

        CommunityPostLikeResponse response = service.removeLike(300L, 100L);

        assertThat(response).isEqualTo(new CommunityPostLikeResponse(false, 2L));
        InOrder order = inOrder(repository);
        order.verify(repository).findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW);
        order.verify(repository).countPostLikeByPostIdAndMemberId(300L, 100L);
        order.verify(repository).deletePostLikeByPostIdAndMemberId(300L, 100L);
        order.verify(repository).countPostLikesByPostId(300L);
    }

    @Test
    void keepsRepeatedDeleteIdempotentWithoutDeleteQuery() {
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(existingPost()));
        when(repository.countPostLikeByPostIdAndMemberId(300L, 100L)).thenReturn(0L);
        when(repository.countPostLikesByPostId(300L)).thenReturn(2L);

        CommunityPostLikeResponse response = service.removeLike(300L, 100L);

        assertThat(response).isEqualTo(new CommunityPostLikeResponse(false, 2L));
        verify(repository, never()).deletePostLikeByPostIdAndMemberId(300L, 100L);
    }

    @Test
    void allowsAddingAgainAfterDelete() {
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(existingPost()));
        when(repository.countPostLikeByPostIdAndMemberId(300L, 100L)).thenReturn(1L, 0L);
        when(repository.countPostLikesByPostId(300L)).thenReturn(0L, 1L);

        CommunityPostLikeResponse removed = service.removeLike(300L, 100L);
        CommunityPostLikeResponse added = service.addLike(300L, 100L);

        assertThat(removed).isEqualTo(new CommunityPostLikeResponse(false, 0L));
        assertThat(added).isEqualTo(new CommunityPostLikeResponse(true, 1L));
        verify(repository).deletePostLikeByPostIdAndMemberId(300L, 100L);
        verify(repository).insertPostLike(300L, 100L);
    }

    @Test
    void keepsMembersAndPostsIndependentByBoundIdentifiers() {
        CommunityPost firstPost = existingPost();
        CommunityPost secondPost = CommunityPost.create(
                101L, 200L, CommunityPostKind.EXAM_REVIEW, "다른 후기", "다른 본문"
        );
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(firstPost));
        when(repository.findForUpdateByPostIdAndPostKind(301L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(secondPost));
        when(repository.countPostLikesByPostId(300L)).thenReturn(1L, 2L);
        when(repository.countPostLikesByPostId(301L)).thenReturn(1L);

        service.addLike(300L, 100L);
        service.addLike(300L, 101L);
        service.addLike(301L, 100L);

        verify(repository).insertPostLike(300L, 100L);
        verify(repository).insertPostLike(300L, 101L);
        verify(repository).insertPostLike(301L, 100L);
    }

    @Test
    void returnsPostNotFoundBeforeLikeQueriesWhenTargetIsMissing() {
        when(repository.findForUpdateByPostIdAndPostKind(999L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addLike(999L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.POST_NOT_FOUND);

        verify(repository, never()).countPostLikeByPostIdAndMemberId(999L, 100L);
        verify(repository, never()).countPostLikesByPostId(999L);
    }

    @Test
    void treatsPostOutsideExamReviewScopeAsNotFound() {
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeLike(300L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.POST_NOT_FOUND);

        verify(repository).findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW);
        verify(repository, never()).deletePostLikeByPostIdAndMemberId(300L, 100L);
    }

    @Test
    void usesExistingTransactionalServiceBoundary() {
        assertThat(CommunityPostCommandService.class.isAnnotationPresent(Transactional.class)).isTrue();
    }

    private CommunityPost existingPost() {
        return CommunityPost.create(
                100L,
                200L,
                CommunityPostKind.EXAM_REVIEW,
                "시험 후기",
                "시험 후기 본문"
        );
    }
}
