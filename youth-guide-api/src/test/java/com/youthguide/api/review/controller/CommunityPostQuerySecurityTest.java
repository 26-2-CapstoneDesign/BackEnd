package com.youthguide.api.review.controller;

import com.youthguide.api.global.config.SecurityConfig;
import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.global.exception.GlobalExceptionHandler;
import com.youthguide.api.global.security.MemberUserDetailsService;
import com.youthguide.api.global.security.SecurityErrorResponder;
import com.youthguide.api.global.security.LoginMember;
import com.youthguide.api.review.dto.CommunityPostAuthorResponse;
import com.youthguide.api.review.dto.CommunityPostDetailResponse;
import com.youthguide.api.review.dto.CommunityPostPageResponse;
import com.youthguide.api.review.service.CommunityPostCommandService;
import com.youthguide.api.review.service.CommunityPostQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        controllers = CommunityPostQueryController.class,
        properties = {
                "app.auth.remember-me-key=review-query-security-test-key",
                "app.cors.allowed-origins=http://localhost:5173"
        }
)
@Import({SecurityConfig.class, SecurityErrorResponder.class, GlobalExceptionHandler.class})
class CommunityPostQuerySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommunityPostQueryService service;

    @MockitoBean
    private CommunityPostCommandService commandService;

    @MockitoBean
    private MemberUserDetailsService memberUserDetailsService;

    @Test
    void allowsAnonymousListRequest() throws Exception {
        when(service.getPosts(null, 0, 20, null))
                .thenReturn(new CommunityPostPageResponse(List.of(), 0, 20, 0L, 0, false));

        mockMvc.perform(get("/api/v1/community/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(service).getPosts(null, 0, 20, null);
        verifyNoInteractions(commandService);
    }

    @Test
    void allowsAnonymousSearchRequest() throws Exception {
        when(service.getPosts("정보처리", 1, 5, null))
                .thenReturn(new CommunityPostPageResponse(List.of(), 1, 5, 0L, 0, false));

        mockMvc.perform(get("/api/v1/community/posts")
                        .queryParam("q", "정보처리")
                        .queryParam("page", "1")
                        .queryParam("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5));

        verify(service).getPosts("정보처리", 1, 5, null);
        verifyNoInteractions(commandService);
    }

    @Test
    void allowsAnonymousDetailRequest() throws Exception {
        CommunityPostDetailResponse response = new CommunityPostDetailResponse(
                "101",
                "201",
                "정보처리기사",
                "합격 후기",
                new CommunityPostAuthorResponse("작성자"),
                Instant.parse("2026-10-10T01:02:03Z"),
                Instant.parse("2026-10-10T01:02:03Z"),
                "상세 본문",
                "0",
                12L,
                3L,
                4L
        );
        when(service.getPost(101L, null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/community/posts/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value("101"));

        verify(service).getPost(101L, null);
        verifyNoInteractions(commandService);
    }

    @Test
    void returnsNotFoundToAnonymousDetailRequest() throws Exception {
        when(service.getPost(999L, null)).thenThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

        mockMvc.perform(get("/api/v1/community/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));

        verifyNoInteractions(commandService);
    }

    @Test
    void incrementsViewCountForEveryRepeatedAuthenticatedDetailRequest() throws Exception {
        when(service.getPost(101L, 100L)).thenReturn(detailResponse(15L));
        LoginMember member = loginMember(100L, "member-100");

        for (int requestCount = 0; requestCount < 3; requestCount++) {
            mockMvc.perform(get("/api/v1/community/posts/101").with(user(member)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.viewCount").value(15));
        }

        InOrder invocationOrder = inOrder(commandService, service);
        for (int requestCount = 0; requestCount < 3; requestCount++) {
            invocationOrder.verify(commandService).incrementVisibleViewCount(101L, 100L);
            invocationOrder.verify(service).getPost(101L, 100L);
        }
    }

    @Test
    void incrementsViewCountForDifferentAuthenticatedMembersIncludingAuthor() throws Exception {
        when(service.getPost(101L, 100L)).thenReturn(detailResponse(14L));
        when(service.getPost(101L, 101L)).thenReturn(detailResponse(15L));

        mockMvc.perform(get("/api/v1/community/posts/101")
                        .with(user(loginMember(100L, "post-author"))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/community/posts/101")
                        .with(user(loginMember(101L, "other-member"))))
                .andExpect(status().isOk());

        verify(commandService).incrementVisibleViewCount(101L, 100L);
        verify(commandService).incrementVisibleViewCount(101L, 101L);
        verify(service).getPost(101L, 100L);
        verify(service).getPost(101L, 101L);
    }

    @Test
    void returnsNotFoundBeforeDetailQueryWhenAuthenticatedTargetIsInvalid() throws Exception {
        doThrow(new BusinessException(ErrorCode.POST_NOT_FOUND))
                .when(commandService).incrementVisibleViewCount(999L, 100L);

        mockMvc.perform(get("/api/v1/community/posts/999")
                        .with(user(loginMember(100L, "member-100"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));

        verify(service, never()).getPost(999L, 100L);
    }

    @Test
    void keepsAuthenticatedListAndSearchFromIncrementingViewCount() throws Exception {
        when(service.getPosts("정보처리", 0, 20, 100L))
                .thenReturn(new CommunityPostPageResponse(List.of(), 0, 20, 0L, 0, false));

        mockMvc.perform(get("/api/v1/community/posts")
                        .with(user(loginMember(100L, "member-100")))
                        .queryParam("q", "정보처리"))
                .andExpect(status().isOk());

        verify(service).getPosts("정보처리", 0, 20, 100L);
        verifyNoInteractions(commandService);
    }

    @Test
    void keepsAnonymousPostRequestAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/community/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void keepsAnonymousPutRequestAuthenticated() throws Exception {
        mockMvc.perform(put("/api/v1/community/posts/101")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void keepsAnonymousDeleteRequestAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/community/posts/101")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void keepsOtherAnonymousCommunityGetRequestAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/community/posts/101/likes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    private CommunityPostDetailResponse detailResponse(long viewCount) {
        return new CommunityPostDetailResponse(
                "101",
                "201",
                "정보처리기사",
                "합격 후기",
                new CommunityPostAuthorResponse("작성자"),
                Instant.parse("2026-10-10T01:02:03Z"),
                Instant.parse("2026-10-10T01:02:03Z"),
                "상세 본문",
                "0",
                viewCount,
                3L,
                4L
        );
    }

    private LoginMember loginMember(Long memberId, String loginNameKey) {
        return new LoginMember(memberId, loginNameKey, "password");
    }
}
