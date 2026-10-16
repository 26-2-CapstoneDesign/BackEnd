package com.youthguide.api.review.service;

import com.youthguide.api.certificate.repository.CertificateRepository;
import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.repository.CommunityPostCommandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityPostViewCountServiceTest {

    @Mock
    private CommunityPostCommandRepository repository;

    @Mock
    private CertificateRepository certificateRepository;

    @InjectMocks
    private CommunityPostCommandService service;

    @Test
    void incrementsViewCountWithoutPessimisticPostLock() {
        when(repository.incrementPostViewCount(300L, CommunityPostKind.EXAM_REVIEW)).thenReturn(1);

        service.incrementViewCount(300L);

        verify(repository).incrementPostViewCount(300L, CommunityPostKind.EXAM_REVIEW);
        verify(repository, never()).findForUpdateByPostIdAndPostKind(300L, CommunityPostKind.EXAM_REVIEW);
    }

    @Test
    void acceptsExistingMetricRowUpdateResult() {
        when(repository.incrementPostViewCount(300L, CommunityPostKind.EXAM_REVIEW)).thenReturn(2);

        service.incrementViewCount(300L);

        verify(repository).incrementPostViewCount(300L, CommunityPostKind.EXAM_REVIEW);
    }

    @Test
    void incrementsAgainForEveryRepeatedRequest() {
        when(repository.incrementPostViewCount(300L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(1, 2, 2);

        service.incrementViewCount(300L);
        service.incrementViewCount(300L);
        service.incrementViewCount(300L);

        verify(repository, times(3)).incrementPostViewCount(300L, CommunityPostKind.EXAM_REVIEW);
    }

    @Test
    void rejectsMissingOrNonExamReviewWithoutCreatingMetric() {
        when(repository.incrementPostViewCount(999L, CommunityPostKind.EXAM_REVIEW)).thenReturn(0);

        assertThatThrownBy(() -> service.incrementViewCount(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }
}
