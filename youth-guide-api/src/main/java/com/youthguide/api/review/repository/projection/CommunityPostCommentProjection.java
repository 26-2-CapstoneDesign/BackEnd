package com.youthguide.api.review.repository.projection;

import java.time.LocalDateTime;

public interface CommunityPostCommentProjection {

    Long getCommentId();

    Long getParentCommentId();

    Long getReplyToCommentId();

    String getReplyToNickname();

    Long getAuthorMemberId();

    String getAuthorNickname();

    String getBody();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    LocalDateTime getDeletedAt();

    Long getLockVersion();
}
