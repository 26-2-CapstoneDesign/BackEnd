package com.youthguide.api.review.service;

import com.youthguide.api.certificate.repository.CertificateRepository;
import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.repository.CommunityPostCommandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.youthguide.api.review.service.CommunityPostCommandService.DeleteStatus.AUTHOR_MISMATCH;
import static com.youthguide.api.review.service.CommunityPostCommandService.DeleteStatus.DELETED;
import static com.youthguide.api.review.service.CommunityPostCommandService.DeleteStatus.TARGET_NOT_FOUND;
import static com.youthguide.api.review.service.CommunityPostCommandService.DeleteStatus.VERSION_CONFLICT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityPostDeleteServiceTest {

    @Mock
    private CommunityPostCommandRepository repository;

    @Mock
    private CertificateRepository certificateRepository;

    @InjectMocks
    private CommunityPostCommandService service;

    @Test
    void deletesAssociationsThenOwnedPostWhenVersionMatches() {
        CommunityPost post = existingPost(100L, 300L, 7L);
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
        when(repository.deletePostLikesByPostId(300L)).thenReturn(2);
        when(repository.deletePostMetricByPostId(300L)).thenReturn(1);
        when(repository.deletePostReportsByPostId(300L)).thenReturn(3);

        CommunityPostCommandService.DeleteResult result = service.deletePost(300L, 100L, 7L);

        assertThat(result.status()).isEqualTo(DELETED);
        assertThat(result.postId()).isEqualTo(300L);
        assertThat(result.expectedVersion()).isEqualTo(7L);
        assertThat(result.currentVersion()).isEqualTo(7L);
        InOrder order = inOrder(repository);
        order.verify(repository).findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW);
        order.verify(repository).deletePostLikesByPostId(300L);
        order.verify(repository).deletePostMetricByPostId(300L);
        order.verify(repository).deletePostReportsByPostId(300L);
        order.verify(repository).delete(post);
        verifyNoInteractions(certificateRepository);
    }

    @Test
    void deletesPostWhenNoAssociationRowsExist() {
        CommunityPost post = existingPost(100L, 300L, 7L);
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));

        CommunityPostCommandService.DeleteResult result = service.deletePost(300L, 100L, 7L);

        assertThat(result.status()).isEqualTo(DELETED);
        verify(repository).deletePostLikesByPostId(300L);
        verify(repository).deletePostMetricByPostId(300L);
        verify(repository).deletePostReportsByPostId(300L);
        verify(repository).delete(post);
    }

    @Test
    void returnsNotFoundWithoutDeletingAnything() {
        when(repository.findForUpdateByPostIdAndPostKind(999L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.empty());

        CommunityPostCommandService.DeleteResult result = service.deletePost(999L, 100L, 7L);

        assertThat(result.status()).isEqualTo(TARGET_NOT_FOUND);
        assertThat(result.currentVersion()).isNull();
        verify(repository, never()).deletePostLikesByPostId(999L);
        verify(repository, never()).deletePostMetricByPostId(999L);
        verify(repository, never()).deletePostReportsByPostId(999L);
    }

    @Test
    void rejectsAnotherAuthorWithoutDeletingAnything() {
        CommunityPost post = existingPost(100L, 300L, 7L);
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));

        CommunityPostCommandService.DeleteResult result = service.deletePost(300L, 999L, 7L);

        assertThat(result.status()).isEqualTo(AUTHOR_MISMATCH);
        assertThat(result.currentVersion()).isEqualTo(7L);
        verify(repository, never()).deletePostLikesByPostId(300L);
        verify(repository, never()).deletePostMetricByPostId(300L);
        verify(repository, never()).deletePostReportsByPostId(300L);
        verify(repository, never()).delete(post);
    }

    @Test
    void rejectsVersionConflictWithoutDeletingAnything() {
        CommunityPost post = existingPost(100L, 300L, 7L);
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));

        CommunityPostCommandService.DeleteResult result = service.deletePost(300L, 100L, 6L);

        assertThat(result.status()).isEqualTo(VERSION_CONFLICT);
        assertThat(result.expectedVersion()).isEqualTo(6L);
        assertThat(result.currentVersion()).isEqualTo(7L);
        verify(repository, never()).deletePostLikesByPostId(300L);
        verify(repository, never()).deletePostMetricByPostId(300L);
        verify(repository, never()).deletePostReportsByPostId(300L);
        verify(repository, never()).delete(post);
    }

    @Test
    void propagatesFailureBeforeParentDeleteForTransactionRollback() {
        CommunityPost post = existingPost(100L, 300L, 7L);
        when(repository.findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
        when(repository.deletePostReportsByPostId(300L))
                .thenThrow(new IllegalStateException("forced relation delete failure"));

        assertThatThrownBy(() -> service.deletePost(300L, 100L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("forced relation delete failure");

        verify(repository).deletePostLikesByPostId(300L);
        verify(repository).deletePostMetricByPostId(300L);
        verify(repository, never()).delete(post);
        assertThat(CommunityPostCommandService.class.isAnnotationPresent(Transactional.class)).isTrue();
    }

    private CommunityPost existingPost(Long authorMemberId, Long postId, Long version) {
        CommunityPost post = CommunityPost.create(
                authorMemberId,
                200L,
                CommunityPostKind.EXAM_REVIEW,
                "기존 제목",
                "기존 본문"
        );
        ReflectionTestUtils.setField(post, "postId", postId);
        ReflectionTestUtils.setField(post, "lockVersion", version);
        return post;
    }
}
