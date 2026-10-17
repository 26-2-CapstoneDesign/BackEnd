package com.youthguide.api.review.service;

import com.youthguide.api.certificate.repository.CertificateRepository;
import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.dto.CommunityPostCreateRequest;
import com.youthguide.api.review.dto.CommunityPostLikeResponse;
import com.youthguide.api.review.dto.CommunityPostReportCreateRequest;
import com.youthguide.api.review.dto.CommunityPostUpdateRequest;
import com.youthguide.api.review.repository.CommunityPostCommandRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class CommunityPostCommandService {

    private static final long HIDE_REPORT_THRESHOLD = 3L;
    private static final String UQ_POST_REPORTER = "uq_community_post_report_post_reporter";

    private final CommunityPostCommandRepository repository;
    private final CertificateRepository certificateRepository;

    public CommunityPostCommandService(
            CommunityPostCommandRepository repository,
            CertificateRepository certificateRepository
    ) {
        this.repository = repository;
        this.certificateRepository = certificateRepository;
    }

    public Long createPost(Long authorMemberId, CommunityPostCreateRequest request) {
        Long certificateId = Long.valueOf(request.certificateId());
        validateCertificateExists(certificateId);
        CommunityPost post = CommunityPost.create(
                authorMemberId,
                certificateId,
                CommunityPostKind.EXAM_REVIEW,
                request.title(),
                request.body()
        );

        return repository.save(post).getPostId();
    }

    public UpdateResult updatePost(
            Long postId,
            Long authorMemberId,
            CommunityPostUpdateRequest request
    ) {
        Long requestedVersion = Long.valueOf(request.version());
        CommunityPost post = repository.findByPostIdAndPostKind(
                postId,
                CommunityPostKind.EXAM_REVIEW
        ).orElse(null);
        if (post == null) {
            return new UpdateResult(UpdateStatus.TARGET_NOT_FOUND, postId, requestedVersion, null);
        }

        Long currentVersion = post.getLockVersion();
        if (!post.isAuthoredBy(authorMemberId)) {
            return new UpdateResult(UpdateStatus.AUTHOR_MISMATCH, postId, requestedVersion, currentVersion);
        }

        if (!requestedVersion.equals(currentVersion)) {
            return new UpdateResult(UpdateStatus.VERSION_CONFLICT, postId, requestedVersion, currentVersion);
        }

        Long certificateId = Long.valueOf(request.certificateId());
        validateCertificateExists(certificateId);
        post.updateContent(
                certificateId,
                request.title(),
                request.body()
        );
        return new UpdateResult(UpdateStatus.UPDATED, postId, requestedVersion, currentVersion);
    }

    public DeleteResult deletePost(Long postId, Long authorMemberId, Long expectedVersion) {
        CommunityPost post = repository.findForUpdateByPostIdAndPostKind(
                postId,
                CommunityPostKind.EXAM_REVIEW
        ).orElse(null);
        if (post == null) {
            return new DeleteResult(DeleteStatus.TARGET_NOT_FOUND, postId, expectedVersion, null);
        }

        Long currentVersion = post.getLockVersion();
        if (!post.isAuthoredBy(authorMemberId)) {
            return new DeleteResult(DeleteStatus.AUTHOR_MISMATCH, postId, expectedVersion, currentVersion);
        }

        if (!expectedVersion.equals(currentVersion)) {
            return new DeleteResult(DeleteStatus.VERSION_CONFLICT, postId, expectedVersion, currentVersion);
        }

        repository.deletePostRepliesByPostId(postId);
        repository.deletePostCommentsByPostId(postId);
        repository.deletePostLikesByPostId(postId);
        repository.deletePostMetricByPostId(postId);
        repository.deletePostReportsByPostId(postId);
        repository.delete(post);
        return new DeleteResult(DeleteStatus.DELETED, postId, expectedVersion, currentVersion);
    }

    public void reportPost(
            Long postId,
            Long reporterMemberId,
            CommunityPostReportCreateRequest request
    ) {
        CommunityPost post = repository.findForUpdateByPostIdAndPostKind(
                postId,
                CommunityPostKind.EXAM_REVIEW
        ).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (post.isHidden()) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        if (post.isAuthoredBy(reporterMemberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (repository.countPostReportByPostIdAndReporterMemberId(postId, reporterMemberId) > 0L) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        try {
            repository.insertPostReport(
                    postId,
                    reporterMemberId,
                    request.reason().name(),
                    request.normalizedDetail()
            );
        } catch (DataIntegrityViolationException exception) {
            throw toReportDuplicateException(exception);
        }

        if (repository.countPostReportsByPostId(postId) >= HIDE_REPORT_THRESHOLD) {
            post.hide();
        }
    }

    public CommunityPostLikeResponse addLike(Long postId, Long memberId) {
        lockExamReviewPost(postId);
        if (repository.countPostLikeByPostIdAndMemberId(postId, memberId) == 0L) {
            repository.insertPostLike(postId, memberId);
        }
        return new CommunityPostLikeResponse(true, repository.countPostLikesByPostId(postId));
    }

    public CommunityPostLikeResponse removeLike(Long postId, Long memberId) {
        lockExamReviewPost(postId);
        if (repository.countPostLikeByPostIdAndMemberId(postId, memberId) > 0L) {
            repository.deletePostLikeByPostIdAndMemberId(postId, memberId);
        }
        return new CommunityPostLikeResponse(false, repository.countPostLikesByPostId(postId));
    }

    public void incrementViewCount(Long postId) {
        int affectedRows = repository.incrementPostViewCount(postId, CommunityPostKind.EXAM_REVIEW);
        if (affectedRows == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
    }

    public void incrementVisibleViewCount(Long postId, Long viewerMemberId) {
        int affectedRows = repository.incrementVisiblePostViewCount(
                postId,
                CommunityPostKind.EXAM_REVIEW,
                viewerMemberId
        );
        if (affectedRows == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
    }

    private void lockExamReviewPost(Long postId) {
        repository.findForUpdateByPostIdAndPostKind(postId, CommunityPostKind.EXAM_REVIEW)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateCertificateExists(Long certificateId) {
        if (!certificateRepository.existsById(certificateId)) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }
    }

    private BusinessException toReportDuplicateException(DataIntegrityViolationException exception) {
        String detail = String.valueOf(exception.getMostSpecificCause().getMessage())
                .toLowerCase(Locale.ROOT);
        if (detail.contains(UQ_POST_REPORTER)) {
            return new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS, exception);
        }
        throw exception;
    }

    public enum UpdateStatus {
        UPDATED,
        TARGET_NOT_FOUND,
        AUTHOR_MISMATCH,
        VERSION_CONFLICT
    }

    public record UpdateResult(
            UpdateStatus status,
            Long postId,
            Long requestedVersion,
            Long currentVersion
    ) {
    }

    public enum DeleteStatus {
        DELETED,
        TARGET_NOT_FOUND,
        AUTHOR_MISMATCH,
        VERSION_CONFLICT
    }

    public record DeleteResult(
            DeleteStatus status,
            Long postId,
            Long expectedVersion,
            Long currentVersion
    ) {
    }
}
