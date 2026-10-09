package com.youthguide.api.review.service;

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
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CommunityPostQueryService {

    private final CommunityPostQueryRepository repository;

    public CommunityPostQueryService(CommunityPostQueryRepository repository) {
        this.repository = repository;
    }

    public CommunityPostPageResponse getPosts(String postKind, String q, int page, int size) {
        String qPattern = LikePatternEscaper.toContainsPattern(q);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<CommunityPostSummaryProjection> result = repository.findPage(postKind, qPattern, pageRequest);

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

    public Optional<CommunityPostDetailResponse> getPost(String postKind, Long postId) {
        return repository.findDetail(postId, postKind)
                .map(CommunityPostQueryService::toDetailResponse);
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
                projection.getLikeCount()
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
                projection.getLikeCount()
        );
    }

    private static String toStringId(Long value) {
        return value == null ? null : value.toString();
    }

    private static Instant toInstant(LocalDateTime value) {
        return value.toInstant(ZoneOffset.UTC);
    }
}
