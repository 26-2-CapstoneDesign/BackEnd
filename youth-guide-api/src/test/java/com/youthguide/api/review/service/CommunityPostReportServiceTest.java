package com.youthguide.api.review.service;

import com.youthguide.api.certificate.repository.CertificateRepository;
import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.domain.CommunityPostReportReason;
import com.youthguide.api.review.dto.CommunityPostReportCreateRequest;
import com.youthguide.api.review.repository.CommunityPostCommandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityPostReportServiceTest {

    private static final Long POST_ID = 300L;
    private static final Long AUTHOR_ID = 100L;
    private static final Long REPORTER_ID = 200L;

    @Mock
    private CommunityPostCommandRepository repository;

    @Mock
    private CertificateRepository certificateRepository;

    @InjectMocks
    private CommunityPostCommandService service;

    @Test
    void reportsVisibleExamReviewWithPessimisticLockAndBoundReporter() {
        CommunityPost post = postBy(AUTHOR_ID);
        CommunityPostReportCreateRequest request = request(CommunityPostReportReason.SPAM, null);
        when(repository.findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
        when(repository.countPostReportByPostIdAndReporterMemberId(POST_ID, REPORTER_ID)).thenReturn(0L);
        when(repository.insertPostReport(POST_ID, REPORTER_ID, "SPAM", null)).thenReturn(1);
        when(repository.countPostReportsByPostId(POST_ID)).thenReturn(1L);

        service.reportPost(POST_ID, REPORTER_ID, request);

        InOrder order = inOrder(repository);
        order.verify(repository).findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW);
        order.verify(repository).countPostReportByPostIdAndReporterMemberId(POST_ID, REPORTER_ID);
        order.verify(repository).insertPostReport(POST_ID, REPORTER_ID, "SPAM", null);
        order.verify(repository).countPostReportsByPostId(POST_ID);
        assertThat(post.isHidden()).isFalse();
        verifyNoInteractions(certificateRepository);
    }

    @Test
    void trimsOtherDetailBeforeInsert() {
        CommunityPost post = postBy(AUTHOR_ID);
        when(repository.findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
        when(repository.countPostReportByPostIdAndReporterMemberId(POST_ID, REPORTER_ID)).thenReturn(0L);
        when(repository.countPostReportsByPostId(POST_ID)).thenReturn(1L);

        service.reportPost(
                POST_ID,
                REPORTER_ID,
                request(CommunityPostReportReason.OTHER, "  기타 상세 사유  ")
        );

        verify(repository).insertPostReport(POST_ID, REPORTER_ID, "OTHER", "기타 상세 사유");
    }

    @Test
    void hidesPostWhenThirdDistinctReportIsStored() {
        CommunityPost post = postBy(AUTHOR_ID);
        when(repository.findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
        when(repository.countPostReportByPostIdAndReporterMemberId(POST_ID, REPORTER_ID)).thenReturn(0L);
        when(repository.countPostReportsByPostId(POST_ID)).thenReturn(3L);

        service.reportPost(POST_ID, REPORTER_ID, request(CommunityPostReportReason.ABUSE, null));

        assertThat(post.isHidden()).isTrue();
    }

    @Test
    void doesNotHidePostBeforeThirdReport() {
        CommunityPost post = postBy(AUTHOR_ID);
        when(repository.findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
        when(repository.countPostReportByPostIdAndReporterMemberId(POST_ID, REPORTER_ID)).thenReturn(0L);
        when(repository.countPostReportsByPostId(POST_ID)).thenReturn(2L);

        service.reportPost(POST_ID, REPORTER_ID, request(CommunityPostReportReason.ABUSE, null));

        assertThat(post.isHidden()).isFalse();
    }

    @Test
    void rejectsMissingOrNonExamReviewPostAsNotFound() {
        when(repository.findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.empty());

        assertBusinessError(
                () -> service.reportPost(POST_ID, REPORTER_ID, request(CommunityPostReportReason.SPAM, null)),
                ErrorCode.POST_NOT_FOUND
        );

        verify(repository, never()).insertPostReport(POST_ID, REPORTER_ID, "SPAM", null);
    }

    @Test
    void rejectsAlreadyHiddenPostAsNotFound() {
        CommunityPost post = postBy(AUTHOR_ID);
        post.hide();
        when(repository.findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));

        assertBusinessError(
                () -> service.reportPost(POST_ID, REPORTER_ID, request(CommunityPostReportReason.SPAM, null)),
                ErrorCode.POST_NOT_FOUND
        );

        verify(repository, never()).countPostReportByPostIdAndReporterMemberId(POST_ID, REPORTER_ID);
        verify(repository, never()).insertPostReport(POST_ID, REPORTER_ID, "SPAM", null);
    }

    @Test
    void rejectsSelfReport() {
        CommunityPost post = postBy(AUTHOR_ID);
        when(repository.findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));

        assertBusinessError(
                () -> service.reportPost(POST_ID, AUTHOR_ID, request(CommunityPostReportReason.SPAM, null)),
                ErrorCode.ACCESS_DENIED
        );

        verify(repository, never()).insertPostReport(POST_ID, AUTHOR_ID, "SPAM", null);
    }

    @Test
    void rejectsDuplicateBeforeInsert() {
        CommunityPost post = postBy(AUTHOR_ID);
        when(repository.findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
        when(repository.countPostReportByPostIdAndReporterMemberId(POST_ID, REPORTER_ID)).thenReturn(1L);

        assertBusinessError(
                () -> service.reportPost(POST_ID, REPORTER_ID, request(CommunityPostReportReason.SPAM, null)),
                ErrorCode.REPORT_ALREADY_EXISTS
        );

        verify(repository, never()).insertPostReport(POST_ID, REPORTER_ID, "SPAM", null);
    }

    @Test
    void mapsOnlyReportUniqueConstraintViolationToDuplicateBusinessError() {
        CommunityPost post = postBy(AUTHOR_ID);
        when(repository.findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
        when(repository.countPostReportByPostIdAndReporterMemberId(POST_ID, REPORTER_ID)).thenReturn(0L);
        DataIntegrityViolationException duplicate = new DataIntegrityViolationException(
                "duplicate",
                new SQLException("Duplicate entry for key 'uq_community_post_report_post_reporter'")
        );
        when(repository.insertPostReport(POST_ID, REPORTER_ID, "SPAM", null)).thenThrow(duplicate);

        assertBusinessError(
                () -> service.reportPost(POST_ID, REPORTER_ID, request(CommunityPostReportReason.SPAM, null)),
                ErrorCode.REPORT_ALREADY_EXISTS
        );
    }

    @Test
    void doesNotMaskUnrelatedIntegrityViolation() {
        CommunityPost post = postBy(AUTHOR_ID);
        when(repository.findForUpdateByPostIdAndPostKind(POST_ID, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(Optional.of(post));
        when(repository.countPostReportByPostIdAndReporterMemberId(POST_ID, REPORTER_ID)).thenReturn(0L);
        DataIntegrityViolationException unrelated = new DataIntegrityViolationException(
                "constraint",
                new SQLException("Cannot add or update child row: fk_community_post_report_member")
        );
        when(repository.insertPostReport(POST_ID, REPORTER_ID, "SPAM", null)).thenThrow(unrelated);

        assertThatThrownBy(() -> service.reportPost(
                POST_ID,
                REPORTER_ID,
                request(CommunityPostReportReason.SPAM, null)
        )).isSameAs(unrelated);
    }

    private CommunityPost postBy(Long authorMemberId) {
        return CommunityPost.create(
                authorMemberId,
                400L,
                CommunityPostKind.EXAM_REVIEW,
                "시험 후기",
                "본문"
        );
    }

    private CommunityPostReportCreateRequest request(CommunityPostReportReason reason, String detail) {
        return new CommunityPostReportCreateRequest(reason, detail);
    }

    private void assertBusinessError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(expected);
    }
}
