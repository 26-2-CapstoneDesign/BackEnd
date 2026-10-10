package com.youthguide.api.review.controller;

import com.youthguide.api.global.config.SecurityConfig;
import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.global.exception.GlobalExceptionHandler;
import com.youthguide.api.global.security.LoginMember;
import com.youthguide.api.global.security.MemberUserDetailsService;
import com.youthguide.api.global.security.SecurityErrorResponder;
import com.youthguide.api.review.dto.CommunityPostCreateRequest;
import com.youthguide.api.review.dto.CommunityPostUpdateRequest;
import com.youthguide.api.review.service.CommunityPostCommandService;
import com.youthguide.api.review.service.CommunityPostCommandService.UpdateResult;
import com.youthguide.api.review.service.CommunityPostCommandService.UpdateStatus;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CommunityPostCommandController.class,
        properties = "app.auth.remember-me-key=test-remember-me-key"
)
@Import({SecurityConfig.class, SecurityErrorResponder.class, GlobalExceptionHandler.class})
class CommunityPostCommandControllerTest {

    private static final LoginMember LOGIN_MEMBER = new LoginMember(100L, "reviewer", "unused");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommunityPostCommandService service;

    @MockitoBean
    private MemberUserDetailsService memberUserDetailsService;

    @Test
    void rejectsUnauthenticatedPostAndPutRequests() throws Exception {
        mockMvc.perform(post("/api/v1/community/posts")
                        .with(csrf())
                        .contentType("application/json")
                        .content(createJson()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        mockMvc.perform(put("/api/v1/community/posts/300")
                        .with(csrf())
                        .contentType("application/json")
                        .content(updateJson()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void rejectsAuthenticatedPostAndPutWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/community/posts")
                        .with(user(LOGIN_MEMBER))
                        .contentType("application/json")
                        .content(createJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(put("/api/v1/community/posts/300")
                        .with(user(LOGIN_MEMBER))
                        .contentType("application/json")
                        .content(updateJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void createsPostWithAuthenticatedMemberAndIgnoresClientAuthorAndPostKind() throws Exception {
        CommunityPostCreateRequest expectedRequest = new CommunityPostCreateRequest(
                "200", "시험 후기", "시험 후기 본문"
        );
        when(service.createPost(100L, expectedRequest)).thenReturn(300L);

        mockMvc.perform(post("/api/v1/community/posts")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "certificateId": "200",
                                  "title": "시험 후기",
                                  "body": "시험 후기 본문",
                                  "authorMemberId": "999",
                                  "postKind": "OTHER_KIND"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value("300"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(service).createPost(100L, expectedRequest);
    }

    @Test
    void updatesPostWithAuthenticatedMemberAndCsrf() throws Exception {
        CommunityPostUpdateRequest expectedRequest = new CommunityPostUpdateRequest(
                "201", "수정 후기", "수정 후기 본문", "7"
        );
        when(service.updatePost(300L, 100L, expectedRequest))
                .thenReturn(new UpdateResult(UpdateStatus.UPDATED, 300L, 7L, 7L));

        mockMvc.perform(put("/api/v1/community/posts/300")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content(updateJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value("300"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(service).updatePost(300L, 100L, expectedRequest);
    }

    @Test
    void mapsUpdateStatusesToCommonErrors() throws Exception {
        CommunityPostUpdateRequest request = new CommunityPostUpdateRequest(
                "201", "수정 후기", "수정 후기 본문", "7"
        );

        assertUpdateError(request, UpdateStatus.TARGET_NOT_FOUND, "POST_NOT_FOUND", 404);
        assertUpdateError(request, UpdateStatus.AUTHOR_MISMATCH, "ACCESS_DENIED", 403);
        assertUpdateError(request, UpdateStatus.VERSION_CONFLICT, "VERSION_CONFLICT", 409);
    }

    @Test
    void mapsMissingCertificateToCommonNotFoundResponse() throws Exception {
        when(service.createPost(eq(100L), any(CommunityPostCreateRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND));

        mockMvc.perform(post("/api/v1/community/posts")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content(createJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CERTIFICATE_NOT_FOUND"));
    }

    @Test
    void rejectsInvalidTitleAndBodyBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/community/posts")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "certificateId": "200",
                                  "title": "가나다라마바사아자차카타파하가나다라마바사아",
                                  "body": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verifyNoInteractions(service);
    }

    @Test
    void mapsJpaOptimisticLockingFailureToVersionConflict() throws Exception {
        when(service.updatePost(eq(300L), eq(100L), any(CommunityPostUpdateRequest.class)))
                .thenThrow(new JpaOptimisticLockingFailureException(new OptimisticLockException()));

        mockMvc.perform(put("/api/v1/community/posts/300")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content(updateJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VERSION_CONFLICT"));
    }

    private void assertUpdateError(
            CommunityPostUpdateRequest request,
            UpdateStatus status,
            String expectedCode,
            int expectedHttpStatus
    ) throws Exception {
        when(service.updatePost(300L, 100L, request))
                .thenReturn(new UpdateResult(status, 300L, 7L, 7L));

        mockMvc.perform(put("/api/v1/community/posts/300")
                        .with(user(LOGIN_MEMBER))
                        .with(csrf())
                        .contentType("application/json")
                        .content(updateJson()))
                .andExpect(status().is(expectedHttpStatus))
                .andExpect(jsonPath("$.code").value(expectedCode));
    }

    private static String createJson() {
        return """
                {
                  "certificateId": "200",
                  "title": "시험 후기",
                  "body": "시험 후기 본문"
                }
                """;
    }

    private static String updateJson() {
        return """
                {
                  "certificateId": "201",
                  "title": "수정 후기",
                  "body": "수정 후기 본문",
                  "version": "7"
                }
                """;
    }
}
