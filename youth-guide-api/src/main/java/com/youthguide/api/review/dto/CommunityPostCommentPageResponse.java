package com.youthguide.api.review.dto;

import java.util.List;

public record CommunityPostCommentPageResponse(
        List<CommunityPostCommentResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public CommunityPostCommentPageResponse {
        items = List.copyOf(items);
    }
}
