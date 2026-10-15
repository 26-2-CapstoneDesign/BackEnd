package com.youthguide.api.review.service;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPostComment;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.dto.CommunityPostCommentCreateRequest;
import com.youthguide.api.review.dto.CommunityPostCommentUpdateRequest;
import com.youthguide.api.review.repository.CommunityPostCommandRepository;
import com.youthguide.api.review.repository.CommunityPostCommentCommandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommunityPostCommentCommandService {

    private final CommunityPostCommandRepository postRepository;
    private final CommunityPostCommentCommandRepository commentRepository;

    public CommunityPostCommentCommandService(
            CommunityPostCommandRepository postRepository,
            CommunityPostCommentCommandRepository commentRepository
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public Long createComment(
            Long postId,
            Long authorMemberId,
            CommunityPostCommentCreateRequest request
    ) {
        lockExamReviewPost(postId);
        CommunityPostComment comment = CommunityPostComment.create(
                postId,
                authorMemberId,
                null,
                null,
                request.body()
        );
        return commentRepository.save(comment).getCommentId();
    }

    public Long createReply(
            Long postId,
            Long replyTargetCommentId,
            Long authorMemberId,
            CommunityPostCommentCreateRequest request
    ) {
        lockExamReviewPost(postId);
        CommunityPostComment replyTarget = findCommentForUpdate(postId, replyTargetCommentId);
        if (replyTarget.isDeleted()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Long rootCommentId = replyTarget.getCommentId();
        Long replyToCommentId = null;
        if (replyTarget.isReply()) {
            CommunityPostComment rootComment = findCommentForUpdate(
                    postId,
                    replyTarget.getParentCommentId()
            );
            if (rootComment.isReply()) {
                throw new BusinessException(ErrorCode.COMMENT_DEPTH_EXCEEDED);
            }
            if (rootComment.isDeleted()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            rootCommentId = rootComment.getCommentId();
            replyToCommentId = replyTarget.getCommentId();
        }

        CommunityPostComment reply = CommunityPostComment.create(
                postId,
                authorMemberId,
                rootCommentId,
                replyToCommentId,
                request.body()
        );
        return commentRepository.save(reply).getCommentId();
    }

    public Long updateComment(
            Long postId,
            Long commentId,
            Long authorMemberId,
            CommunityPostCommentUpdateRequest request
    ) {
        lockExamReviewPost(postId);
        CommunityPostComment comment = findCommentForUpdate(postId, commentId);
        validateMutableComment(comment, authorMemberId, parseVersion(request.version()));
        comment.updateBody(request.body());
        return commentId;
    }

    public void deleteComment(
            Long postId,
            Long commentId,
            Long authorMemberId,
            Long expectedVersion
    ) {
        lockExamReviewPost(postId);
        CommunityPostComment comment = findCommentForUpdate(postId, commentId);
        validateMutableComment(comment, authorMemberId, expectedVersion);

        if (!comment.isReply()) {
            deleteTopLevelComment(comment);
            return;
        }

        Long parentCommentId = comment.getParentCommentId();
        commentRepository.delete(comment);
        commentRepository.flush();
        removeEmptyTombstoneParent(postId, parentCommentId);
    }

    private void deleteTopLevelComment(CommunityPostComment comment) {
        if (commentRepository.countByParentCommentId(comment.getCommentId()) == 0L) {
            commentRepository.delete(comment);
            return;
        }
        comment.markDeleted();
    }

    private void removeEmptyTombstoneParent(Long postId, Long parentCommentId) {
        if (commentRepository.countByParentCommentId(parentCommentId) != 0L) {
            return;
        }
        commentRepository.findForUpdateByCommentIdAndPostId(parentCommentId, postId)
                .filter(CommunityPostComment::isDeleted)
                .ifPresent(commentRepository::delete);
    }

    private CommunityPostComment findCommentForUpdate(Long postId, Long commentId) {
        return commentRepository.findForUpdateByCommentIdAndPostId(commentId, postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void validateMutableComment(
            CommunityPostComment comment,
            Long authorMemberId,
            Long expectedVersion
    ) {
        if (comment.isDeleted()) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (!comment.isAuthoredBy(authorMemberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (!expectedVersion.equals(comment.getLockVersion())) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT);
        }
    }

    private void lockExamReviewPost(Long postId) {
        postRepository.findForUpdateByPostIdAndPostKind(postId, CommunityPostKind.EXAM_REVIEW)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private Long parseVersion(String version) {
        try {
            return Long.valueOf(version);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, exception);
        }
    }
}
