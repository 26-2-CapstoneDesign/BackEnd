package com.youthguide.api.review.service;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.dto.CommunityPostAuthorResponse;
import com.youthguide.api.review.dto.CommunityPostDetailResponse;
import com.youthguide.api.review.dto.CommunityPostPageResponse;
import com.youthguide.api.review.dto.CommunityPostSummaryResponse;
import com.youthguide.api.review.repository.CommunityPostQueryRepository;
import com.youthguide.api.review.repository.projection.CommunityPostDetailProjection;
import com.youthguide.api.review.repository.projection.CommunityPostSummaryProjection;
import com.youthguide.api.review.support.LikePatternEscaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommunityPostQueryService {

    private final CommunityPostQueryRepository repository;

    public CommunityPostQueryService(CommunityPostQueryRepository repository) {
        this.repository = repository;
    }

    public CommunityPostPageResponse getPosts(String q, int page, int size) {
        return getPosts(q, page, size, null);
    }

    public CommunityPostPageResponse getPosts(String q, int page, int size, Long viewerMemberId) {
        String qPattern = LikePatternEscaper.toContainsPattern(q);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<CommunityPostSummaryProjection> result = repository.findPage(
                CommunityPostKind.EXAM_REVIEW,
                qPattern,
                viewerMemberId,
                pageRequest
        );

        List<CommunityPostSummaryResponse> items = result.getContent().stream()
                .map(CommunityPostQueryService::toSummaryResponse)
                .toList();

        return new CommunityPostPageResponse(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    public CommunityPostDetailResponse getPost(Long postId) {
        return getPost(postId, null);
    }

    public CommunityPostDetailResponse getPost(Long postId, Long viewerMemberId) {
        return repository.findDetail(postId, CommunityPostKind.EXAM_REVIEW, viewerMemberId)
                .map(CommunityPostQueryService::toDetailResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private static CommunityPostSummaryResponse toSummaryResponse(CommunityPostSummaryProjection projection) {
        return new CommunityPostSummaryResponse(
                toStringId(projection.getPostId()),
                toStringId(projection.getCertificateId()),
                projection.getCertificateName(),
                projection.getTitle(),
                new CommunityPostAuthorResponse(projection.getNickname()),
                toInstant(projection.getCreatedAt()),
                toInstant(projection.getUpdatedAt()),
                projection.getViewCount(),
                projection.getLikeCount(),
                projection.getCommentCount()
        );
    }

    private static CommunityPostDetailResponse toDetailResponse(CommunityPostDetailProjection projection) {
        return new CommunityPostDetailResponse(
                toStringId(projection.getPostId()),
                toStringId(projection.getCertificateId()),
                projection.getCertificateName(),
                projection.getTitle(),
                new CommunityPostAuthorResponse(projection.getNickname()),
                toInstant(projection.getCreatedAt()),
                toInstant(projection.getUpdatedAt()),
                projection.getBody(),
                toStringId(projection.getLockVersion()),
                projection.getViewCount(),
                projection.getLikeCount(),
                projection.getCommentCount()
        );
    }

    private static String toStringId(Long value) {
        return value == null ? null : value.toString();
    }

    private static Instant toInstant(LocalDateTime value) {
        return value.toInstant(ZoneOffset.UTC);
    }
}
