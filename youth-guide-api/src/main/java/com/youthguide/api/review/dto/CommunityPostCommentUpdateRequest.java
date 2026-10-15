package com.youthguide.api.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CommunityPostCommentUpdateRequest(
        @NotBlank
        @Size(max = 255)
        String body,

        @NotBlank
        @Pattern(regexp = "[0-9]+")
        String version
) {
}
