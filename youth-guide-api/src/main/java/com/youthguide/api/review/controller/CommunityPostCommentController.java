package com.youthguide.api.review.controller;

import com.youthguide.api.global.security.LoginMember;
import com.youthguide.api.review.dto.CommunityPostCommentCommandResponse;
import com.youthguide.api.review.dto.CommunityPostCommentCreateRequest;
import com.youthguide.api.review.dto.CommunityPostCommentPageResponse;
import com.youthguide.api.review.dto.CommunityPostCommentUpdateRequest;
import com.youthguide.api.review.service.CommunityPostCommentCommandService;
import com.youthguide.api.review.service.CommunityPostCommentQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/community/posts/{postId}/comments")
public class CommunityPostCommentController {

    private final CommunityPostCommentQueryService queryService;
    private final CommunityPostCommentCommandService commandService;

    public CommunityPostCommentController(
            CommunityPostCommentQueryService queryService,
            CommunityPostCommentCommandService commandService
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @GetMapping
    public CommunityPostCommentPageResponse getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.") int size,
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        Long viewerMemberId = loginMember == null ? null : loginMember.getMemberId();
        return queryService.getComments(postId, page, size, viewerMemberId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityPostCommentCommandResponse createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody CommunityPostCommentCreateRequest request
    ) {
        Long commentId = commandService.createComment(postId, loginMember.getMemberId(), request);
        return CommunityPostCommentCommandResponse.from(commentId);
    }

    @PostMapping("/{commentId}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityPostCommentCommandResponse createReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody CommunityPostCommentCreateRequest request
    ) {
        Long replyId = commandService.createReply(postId, commentId, loginMember.getMemberId(), request);
        return CommunityPostCommentCommandResponse.from(replyId);
    }

    @PutMapping("/{commentId}")
    public CommunityPostCommentCommandResponse updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody CommunityPostCommentUpdateRequest request
    ) {
        Long updatedCommentId = commandService.updateComment(
                postId,
                commentId,
                loginMember.getMemberId(),
                request
        );
        return CommunityPostCommentCommandResponse.from(updatedCommentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestHeader("X-Expected-Comment-Version") @Min(0) Long expectedVersion,
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        commandService.deleteComment(postId, commentId, loginMember.getMemberId(), expectedVersion);
    }
}
