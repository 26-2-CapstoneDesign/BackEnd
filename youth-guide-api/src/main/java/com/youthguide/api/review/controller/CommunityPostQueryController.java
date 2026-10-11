package com.youthguide.api.review.controller;

import com.youthguide.api.review.dto.CommunityPostDetailResponse;
import com.youthguide.api.review.dto.CommunityPostPageResponse;
import com.youthguide.api.review.service.CommunityPostQueryService;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/community/posts")
public class CommunityPostQueryController {

    private final CommunityPostQueryService communityPostQueryService;

    public CommunityPostQueryController(CommunityPostQueryService communityPostQueryService) {
        this.communityPostQueryService = communityPostQueryService;
    }

    @GetMapping
    public CommunityPostPageResponse getPosts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.") int size
    ) {
        return communityPostQueryService.getPosts(q, page, size);
    }

    @GetMapping("/{postId}")
    public CommunityPostDetailResponse getPost(@PathVariable Long postId) {
        return communityPostQueryService.getPost(postId);
    }
}
