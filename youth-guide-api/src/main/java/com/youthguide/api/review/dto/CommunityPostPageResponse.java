package com.youthguide.api.review.dto;

import java.util.List;

public record CommunityPostPageResponse(
        List<CommunityPostSummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public CommunityPostPageResponse {
        items = List.copyOf(items);
    }
}
