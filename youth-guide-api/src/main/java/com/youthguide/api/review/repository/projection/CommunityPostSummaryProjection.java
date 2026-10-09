package com.youthguide.api.review.repository.projection;

import java.time.LocalDateTime;

public interface CommunityPostSummaryProjection {

    Long getPostId();

    Long getCertificateId();

    String getCertificateName();

    String getTitle();

    String getNickname();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    Long getViewCount();

    Long getLikeCount();
}
