package com.youthguide.api.review.dto;

import java.time.Instant;

public record CommunityPostCommentReplyResponse(
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
        boolean deleted
) {
}
