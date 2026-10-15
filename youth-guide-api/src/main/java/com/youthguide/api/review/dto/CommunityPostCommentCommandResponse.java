package com.youthguide.api.review.dto;

public record CommunityPostCommentCommandResponse(String commentId) {

    public static CommunityPostCommentCommandResponse from(Long commentId) {
        return new CommunityPostCommentCommandResponse(commentId.toString());
    }
}
