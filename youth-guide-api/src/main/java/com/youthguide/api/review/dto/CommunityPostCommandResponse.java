package com.youthguide.api.review.dto;

public record CommunityPostCommandResponse(
        String postId
) {
    public static CommunityPostCommandResponse from(Long postId) {
        return new CommunityPostCommandResponse(String.valueOf(postId));
    }
}
