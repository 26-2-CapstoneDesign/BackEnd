package com.youthguide.api.review.dto;

import java.time.Instant;
import java.util.List;

public record CommunityPostCommentResponse(
        String commentId,
        String parentCommentId,
        String replyToCommentId,
        String replyToNickname,
        String authorNickname,
        String body,
        Instant createdAt,
        Instant updatedAt,
        String version,
        boolean mine,
        boolean deleted,
        List<CommunityPostCommentReplyResponse> replies
) {
    public CommunityPostCommentResponse {
        replies = List.copyOf(replies);
    }
}
