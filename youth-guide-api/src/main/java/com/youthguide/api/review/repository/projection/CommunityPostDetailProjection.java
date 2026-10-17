package com.youthguide.api.review.repository.projection;

import java.time.LocalDateTime;

public interface CommunityPostDetailProjection {

    Long getPostId();

    Long getCertificateId();

    String getCertificateName();

    String getTitle();

    String getBody();

    String getNickname();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    Long getLockVersion();

    Long getViewCount();

    Long getLikeCount();

    Long getCommentCount();
}
