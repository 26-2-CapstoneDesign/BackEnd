package com.youthguide.api.review.controller;

import com.youthguide.api.global.config.SecurityConfig;
import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.global.exception.GlobalExceptionHandler;
import com.youthguide.api.global.security.LoginMember;
import com.youthguide.api.global.security.MemberUserDetailsService;
import com.youthguide.api.global.security.SecurityErrorResponder;
import com.youthguide.api.review.dto.CommunityPostCommentCreateRequest;
import com.youthguide.api.review.dto.CommunityPostCommentPageResponse;
import com.youthguide.api.review.dto.CommunityPostCommentReplyResponse;
import com.youthguide.api.review.dto.CommunityPostCommentResponse;
import com.youthguide.api.review.dto.CommunityPostCommentUpdateRequest;
import com.youthguide.api.review.service.CommunityPostCommentCommandService;
import com.youthguide.api.review.service.CommunityPostCommentQueryService;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CommunityPostCommentController.class,
        properties = {
                "app.auth.remember-me-key=review-comment-test-key",
                "app.cors.allowed-origins=http://localhost:5173"
        }
)
@Import({SecurityConfig.class, SecurityErrorResponder.class, GlobalExceptionHandler.class})
class CommunityPostCommentControllerTest {

    private static final LoginMember LOGIN_MEMBER = new LoginMember(20L, "commenter", "unused");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommunityPostCommentQueryService queryService;

    @MockitoBean
    private CommunityPostCommentCommandService commandService;

    @MockitoBean
    private MemberUserDetailsService memberUserDetailsService;

    @Test
    void allowsAnonymousCommentListAndUsesTopLevelPaginationDefaults() throws Exception {
        CommunityPostCommentResponse item = new CommunityPostCommentResponse(
                "101",
                null,
                null,
                null,
                "작성자",
                "댓글",
                Instant.parse("2026-10-12T01:00:00Z"),
                Instant.parse("2026-10-12T01:00:00Z"),
                "0",
                false,
                false,
                List.of(new CommunityPostCommentReplyResponse(
                        "102",
                        "101",
                        "99",
                        "답글 대상",
                        "대댓글 작성자",
                        "답글",
                        Instant.parse("2026-10-12T01:01:00Z"),
                        Instant.parse("2026-10-12T01:01:00Z"),
                        "0",
                        false,
                        false
                ))
        );
        when(queryService.getComments(10L, 0, 20, null))
                .thenReturn(new CommunityPostCommentPageResponse(
                        List.of(item), 0, 20, 1L, 1, false
                ));

        mockMvc.perform(get("/api/v1/community/posts/10/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].commentId").value("101"))
                .andExpect(jsonPath("$.items[0].replyToCommentId").isEmpty())
                .andExpect(jsonPath("$.items[0].replies[0].parentCommentId").value("101"))
                .andExpect(jsonPath("$.items[0].replies[0].replyToCommentId").value("99"))
                .andExpect(jsonPath("$.items[0].replies[0].replyToNickname").value("답글 대상"))
                .andExpect(jsonPath("$.items[0].mine").value(false))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(queryService).getComments(10L, 0, 20, null);
    }

    @Test
    void passesAuthenticatedViewerToCommentList() throws Exception {
        when(queryService.getComments(10L, 2, 5, 20L))
                .thenReturn(new CommunityPostCommentPageResponse(
                        List.of(), 2, 5, 0L, 0, false
                ));

        mockMvc.perform(get("/api/v1/community/posts/10/comments")
                        .with(user(LOGIN_MEMBER))
                        .queryParam("page", "2")
                        .queryParam("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2));

        verify(queryService).getComments(10L, 2, 5, 20L);
    }

    @Test
    void rejectsInvalidCommentPageInputs() throws Exception {
        mockMvc.perform(get("/api/v1/community/posts/10/comments").queryParam("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        mockMvc.perform(get("/api/v1/community/posts/10/comments").queryParam("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        verifyNoInteractions(queryService);
    }

    @Test
    void createsTopLevelCommentWithLoginMemberAndCsrf() throws Exception {
        CommunityPostCommentCreateRequest request = new CommunityPostCommentCreateRequest("댓글");
        when(commandService.createComment(10L, 20L, request)).thenReturn(101L);

        mockMvc.perform(post("/api/v1/community/posts/10/comments")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {"body":"댓글","authorMemberId":"999","postKind":"OTHER"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").value("101"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(commandService).createComment(10L, 20L, request);
    }

    @Test
    void createsRepliesForRootOrReplyTarget() throws Exception {
        CommunityPostCommentCreateRequest request = new CommunityPostCommentCreateRequest("답글");
        when(commandService.createReply(10L, 101L, 20L, request)).thenReturn(102L);

        mockMvc.perform(post("/api/v1/community/posts/10/comments/101/replies")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"body\":\"답글\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").value("102"));

        when(commandService.createReply(10L, 102L, 20L, request)).thenReturn(103L);
        mockMvc.perform(post("/api/v1/community/posts/10/comments/102/replies")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"body\":\"답글\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").value("103"));

        verify(commandService).createReply(10L, 101L, 20L, request);
        verify(commandService).createReply(10L, 102L, 20L, request);
    }

    @Test
    void accepts255CharactersAndRejects256OrBlank() throws Exception {
        String maxBody = "가".repeat(255);
        CommunityPostCommentCreateRequest request = new CommunityPostCommentCreateRequest(maxBody);
        when(commandService.createComment(10L, 20L, request)).thenReturn(101L);

        mockMvc.perform(post("/api/v1/community/posts/10/comments")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"body\":\"" + maxBody + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/community/posts/10/comments")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"body\":\"" + "가".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        mockMvc.perform(post("/api/v1/community/posts/10/comments")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"body\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void updatesAndDeletesOwnedCommentWithVersion() throws Exception {
        CommunityPostCommentUpdateRequest request = new CommunityPostCommentUpdateRequest("수정", "3");
        when(commandService.updateComment(10L, 101L, 20L, request)).thenReturn(101L);

        mockMvc.perform(put("/api/v1/community/posts/10/comments/101")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"body\":\"수정\",\"version\":\"3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value("101"));

        mockMvc.perform(delete("/api/v1/community/posts/10/comments/101")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .header("X-Expected-Comment-Version", "3"))
                .andExpect(status().isNoContent());

        verify(commandService).updateComment(10L, 101L, 20L, request);
        verify(commandService).deleteComment(10L, 101L, 20L, 3L);
    }

    @Test
    void mapsCommentErrorsToCommonResponse() throws Exception {
        CommunityPostCommentUpdateRequest request = new CommunityPostCommentUpdateRequest("수정", "3");
        when(commandService.updateComment(10L, 999L, 20L, request))
                .thenThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        mockMvc.perform(put("/api/v1/community/posts/10/comments/999")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"body\":\"수정\",\"version\":\"3\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));

        doThrow(new BusinessException(ErrorCode.ACCESS_DENIED))
                .when(commandService).deleteComment(10L, 101L, 20L, 3L);
        mockMvc.perform(delete("/api/v1/community/posts/10/comments/101")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .header("X-Expected-Comment-Version", "3"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void mapsStaleAndJpaOptimisticLockingFailuresToVersionConflict() throws Exception {
        CommunityPostCommentUpdateRequest request = new CommunityPostCommentUpdateRequest("수정", "2");
        when(commandService.updateComment(10L, 101L, 20L, request))
                .thenThrow(new BusinessException(ErrorCode.VERSION_CONFLICT));

        mockMvc.perform(put("/api/v1/community/posts/10/comments/101")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"body\":\"수정\",\"version\":\"2\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VERSION_CONFLICT"));

        doThrow(new JpaOptimisticLockingFailureException(new OptimisticLockException()))
                .when(commandService).deleteComment(10L, 101L, 20L, 3L);
        mockMvc.perform(delete("/api/v1/community/posts/10/comments/101")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .header("X-Expected-Comment-Version", "3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VERSION_CONFLICT"));
    }

    @Test
    void keepsWritesAuthenticatedAndCsrfProtected() throws Exception {
        mockMvc.perform(post("/api/v1/community/posts/10/comments")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"body\":\"댓글\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        mockMvc.perform(post("/api/v1/community/posts/10/comments")
                        .with(user(LOGIN_MEMBER))
                        .contentType("application/json")
                        .content("{\"body\":\"댓글\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        verifyNoInteractions(commandService);
    }

    @Test
    void rejectsMissingMalformedOrNegativeDeleteVersionHeader() throws Exception {
        mockMvc.perform(delete("/api/v1/community/posts/10/comments/101")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        mockMvc.perform(delete("/api/v1/community/posts/10/comments/101")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .header("X-Expected-Comment-Version", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        mockMvc.perform(delete("/api/v1/community/posts/10/comments/101")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .header("X-Expected-Comment-Version", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        verifyNoInteractions(commandService);
    }
}
