package com.youthguide.api.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CommunityPostCreateRequest(
        @NotBlank
        @Pattern(regexp = "[0-9]+")
        String certificateId,

        @NotBlank
        @Size(max = 20)
        String title,

        @NotBlank
        String body
) {
}
