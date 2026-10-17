package com.youthguide.api.review.dto;

import java.time.Instant;

public record CommunityPostDetailResponse(
        String postId,
        String certificateId,
        String certificateName,
        String title,
        CommunityPostAuthorResponse author,
        Instant createdAt,
        Instant updatedAt,
        String body,
        String version,
        long viewCount,
        long likeCount,
        long commentCount
) {
}
