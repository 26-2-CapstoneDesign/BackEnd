package com.youthguide.api.review.dto;

public record CommunityPostLikeResponse(
        boolean liked,
        long likeCount
) {
}
