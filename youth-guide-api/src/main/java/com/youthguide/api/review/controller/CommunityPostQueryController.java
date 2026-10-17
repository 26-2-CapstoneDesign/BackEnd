package com.youthguide.api.review.controller;

import com.youthguide.api.global.security.LoginMember;
import com.youthguide.api.review.dto.CommunityPostDetailResponse;
import com.youthguide.api.review.dto.CommunityPostPageResponse;
import com.youthguide.api.review.service.CommunityPostCommandService;
import com.youthguide.api.review.service.CommunityPostQueryService;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/community/posts")
public class CommunityPostQueryController {

    private final CommunityPostQueryService communityPostQueryService;
    private final CommunityPostCommandService communityPostCommandService;

    public CommunityPostQueryController(
            CommunityPostQueryService communityPostQueryService,
            CommunityPostCommandService communityPostCommandService
    ) {
        this.communityPostQueryService = communityPostQueryService;
        this.communityPostCommandService = communityPostCommandService;
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
    public CommunityPostDetailResponse getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        if (loginMember != null) {
            communityPostCommandService.incrementViewCount(postId);
        }
        return communityPostQueryService.getPost(postId);
    }
}
