package com.youthguide.api.review.controller;

import com.youthguide.api.review.dto.CommunityPostDetailResponse;
import com.youthguide.api.review.dto.CommunityPostPageResponse;
import com.youthguide.api.review.service.CommunityPostQueryService;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return communityPostQueryService.getPosts(q, page, size);
    }

    @GetMapping("/{postId}")
    public CommunityPostDetailResponse getPost(@PathVariable Long postId) {
        return communityPostQueryService.getPost(postId);
    }
}
