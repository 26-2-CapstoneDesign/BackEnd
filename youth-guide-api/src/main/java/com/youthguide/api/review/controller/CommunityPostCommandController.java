package com.youthguide.api.review.controller;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.global.security.LoginMember;
import com.youthguide.api.review.dto.CommunityPostCommandResponse;
import com.youthguide.api.review.dto.CommunityPostCreateRequest;
import com.youthguide.api.review.dto.CommunityPostLikeResponse;
import com.youthguide.api.review.dto.CommunityPostUpdateRequest;
import com.youthguide.api.review.service.CommunityPostCommandService;
import com.youthguide.api.review.service.CommunityPostCommandService.DeleteResult;
import com.youthguide.api.review.service.CommunityPostCommandService.UpdateResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/community/posts")
public class CommunityPostCommandController {

    private final CommunityPostCommandService communityPostCommandService;

    public CommunityPostCommandController(CommunityPostCommandService communityPostCommandService) {
        this.communityPostCommandService = communityPostCommandService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityPostCommandResponse createPost(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody CommunityPostCreateRequest request
    ) {
        Long postId = communityPostCommandService.createPost(loginMember.getMemberId(), request);
        return CommunityPostCommandResponse.from(postId);
    }

    @PutMapping("/{postId}")
    public CommunityPostCommandResponse updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody CommunityPostUpdateRequest request
    ) {
        UpdateResult result = communityPostCommandService.updatePost(
                postId,
                loginMember.getMemberId(),
                request
        );

        return switch (result.status()) {
            case UPDATED -> CommunityPostCommandResponse.from(result.postId());
            case TARGET_NOT_FOUND -> throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            case AUTHOR_MISMATCH -> throw new BusinessException(ErrorCode.ACCESS_DENIED);
            case VERSION_CONFLICT -> throw new BusinessException(ErrorCode.VERSION_CONFLICT);
        };
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @PathVariable Long postId,
            @RequestHeader("X-Expected-Post-Version") Long expectedVersion,
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        DeleteResult result = communityPostCommandService.deletePost(
                postId,
                loginMember.getMemberId(),
                expectedVersion
        );

        switch (result.status()) {
            case DELETED -> {
                return;
            }
            case TARGET_NOT_FOUND -> throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            case AUTHOR_MISMATCH -> throw new BusinessException(ErrorCode.ACCESS_DENIED);
            case VERSION_CONFLICT -> throw new BusinessException(ErrorCode.VERSION_CONFLICT);
        }
    }

    @PutMapping("/{postId}/likes/me")
    public CommunityPostLikeResponse addMyLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        return communityPostCommandService.addLike(postId, loginMember.getMemberId());
    }

    @DeleteMapping("/{postId}/likes/me")
    public CommunityPostLikeResponse removeMyLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        return communityPostCommandService.removeLike(postId, loginMember.getMemberId());
    }
}
