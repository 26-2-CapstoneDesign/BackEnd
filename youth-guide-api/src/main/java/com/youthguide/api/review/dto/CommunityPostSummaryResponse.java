package com.youthguide.api.review.dto;

import java.time.Instant;

public record CommunityPostSummaryResponse(
        String postId,
        String certificateId,
        String certificateName,
        String title,
        CommunityPostAuthorResponse author,
        Instant createdAt,
        Instant updatedAt,
        long viewCount,
        long likeCount,
        long commentCount
) {
}
