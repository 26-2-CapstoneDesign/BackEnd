package com.youthguide.api.review.dto;

import com.youthguide.api.review.domain.CommunityPostReportReason;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommunityPostReportCreateRequest(
        @NotNull
        CommunityPostReportReason reason,

        @Size(max = 255)
        String detail
) {

    @AssertTrue(message = "신고 상세 사유가 신고 유형 정책에 맞지 않습니다.")
    public boolean isDetailValid() {
        if (reason == null) {
            return true;
        }
        if (reason == CommunityPostReportReason.OTHER) {
            return detail != null && !detail.isBlank();
        }
        return detail == null;
    }

    public String normalizedDetail() {
        return reason == CommunityPostReportReason.OTHER && detail != null
                ? detail.trim()
                : null;
    }
}
