package com.youthguide.api.review.service;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.dto.CommunityPostCommentPageResponse;
import com.youthguide.api.review.dto.CommunityPostCommentReplyResponse;
import com.youthguide.api.review.dto.CommunityPostCommentResponse;
import com.youthguide.api.review.repository.CommunityPostCommentQueryRepository;
import com.youthguide.api.review.repository.CommunityPostQueryRepository;
import com.youthguide.api.review.repository.projection.CommunityPostCommentProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CommunityPostCommentQueryService {

    private static final String DELETED_COMMENT_BODY = "삭제된 댓글입니다.";

    private final CommunityPostQueryRepository postRepository;
    private final CommunityPostCommentQueryRepository commentRepository;

    public CommunityPostCommentQueryService(
            CommunityPostQueryRepository postRepository,
            CommunityPostCommentQueryRepository commentRepository
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public CommunityPostCommentPageResponse getComments(
            Long postId,
            int page,
            int size,
            Long viewerMemberId
    ) {
        if (!postRepository.existsByPostIdAndPostKind(postId, CommunityPostKind.EXAM_REVIEW)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        Page<CommunityPostCommentProjection> topLevelPage = commentRepository.findTopLevelPage(
                postId,
                PageRequest.of(page, size)
        );
        List<Long> parentCommentIds = topLevelPage.getContent().stream()
                .map(CommunityPostCommentProjection::getCommentId)
                .toList();

        List<CommunityPostCommentProjection> replyProjections = parentCommentIds.isEmpty()
                ? List.of()
                : commentRepository.findRepliesByParentCommentIds(parentCommentIds);
        Map<Long, List<CommunityPostCommentReplyResponse>> repliesByParent = replyProjections.stream()
                .collect(Collectors.groupingBy(
                        CommunityPostCommentProjection::getParentCommentId,
                        Collectors.mapping(
                                projection -> toReplyResponse(projection, viewerMemberId),
                                Collectors.toList()
                        )
                ));

        List<CommunityPostCommentResponse> items = topLevelPage.getContent().stream()
                .map(projection -> toCommentResponse(
                        projection,
                        viewerMemberId,
                        repliesByParent.getOrDefault(projection.getCommentId(), List.of())
                ))
                .toList();

        return new CommunityPostCommentPageResponse(
                items,
                topLevelPage.getNumber(),
                topLevelPage.getSize(),
                topLevelPage.getTotalElements(),
                topLevelPage.getTotalPages(),
                topLevelPage.hasNext()
        );
    }

    private static CommunityPostCommentResponse toCommentResponse(
            CommunityPostCommentProjection projection,
            Long viewerMemberId,
            List<CommunityPostCommentReplyResponse> replies
    ) {
        boolean deleted = projection.getDeletedAt() != null;
        return new CommunityPostCommentResponse(
                projection.getCommentId().toString(),
                null,
                toNullableString(projection.getReplyToCommentId()),
                projection.getReplyToNickname(),
                deleted ? null : projection.getAuthorNickname(),
                deleted ? DELETED_COMMENT_BODY : projection.getBody(),
                toInstant(projection.getCreatedAt()),
                toInstant(projection.getUpdatedAt()),
                projection.getLockVersion().toString(),
                !deleted && isMine(projection, viewerMemberId),
                deleted,
                replies
        );
    }

    private static CommunityPostCommentReplyResponse toReplyResponse(
            CommunityPostCommentProjection projection,
            Long viewerMemberId
    ) {
        boolean deleted = projection.getDeletedAt() != null;
        return new CommunityPostCommentReplyResponse(
                projection.getCommentId().toString(),
                projection.getParentCommentId().toString(),
                toNullableString(projection.getReplyToCommentId()),
                projection.getReplyToNickname(),
                deleted ? null : projection.getAuthorNickname(),
                deleted ? DELETED_COMMENT_BODY : projection.getBody(),
                toInstant(projection.getCreatedAt()),
                toInstant(projection.getUpdatedAt()),
                projection.getLockVersion().toString(),
                !deleted && isMine(projection, viewerMemberId),
                deleted
        );
    }

    private static boolean isMine(CommunityPostCommentProjection projection, Long viewerMemberId) {
        return viewerMemberId != null && Objects.equals(projection.getAuthorMemberId(), viewerMemberId);
    }

    private static String toNullableString(Long value) {
        return value == null ? null : value.toString();
    }

    private static Instant toInstant(LocalDateTime value) {
        return value.toInstant(ZoneOffset.UTC);
    }
}
