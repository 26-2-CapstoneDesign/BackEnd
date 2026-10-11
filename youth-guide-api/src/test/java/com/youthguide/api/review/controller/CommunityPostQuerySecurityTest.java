package com.youthguide.api.review.controller;

import com.youthguide.api.global.config.SecurityConfig;
import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.global.exception.GlobalExceptionHandler;
import com.youthguide.api.global.security.MemberUserDetailsService;
import com.youthguide.api.global.security.SecurityErrorResponder;
import com.youthguide.api.review.dto.CommunityPostAuthorResponse;
import com.youthguide.api.review.dto.CommunityPostDetailResponse;
import com.youthguide.api.review.dto.CommunityPostPageResponse;
import com.youthguide.api.review.service.CommunityPostQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    private MemberUserDetailsService memberUserDetailsService;

    @Test
    void allowsAnonymousListRequest() throws Exception {
        when(service.getPosts(null, 0, 20))
                .thenReturn(new CommunityPostPageResponse(List.of(), 0, 20, 0L, 0, false));

        mockMvc.perform(get("/api/v1/community/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(service).getPosts(null, 0, 20);
    }

    @Test
    void allowsAnonymousSearchRequest() throws Exception {
        when(service.getPosts("정보처리", 1, 5))
                .thenReturn(new CommunityPostPageResponse(List.of(), 1, 5, 0L, 0, false));

        mockMvc.perform(get("/api/v1/community/posts")
                        .queryParam("q", "정보처리")
                        .queryParam("page", "1")
                        .queryParam("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5));

        verify(service).getPosts("정보처리", 1, 5);
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
                3L
        );
        when(service.getPost(101L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/community/posts/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value("101"));

        verify(service).getPost(101L);
    }

    @Test
    void returnsNotFoundToAnonymousDetailRequest() throws Exception {
        when(service.getPost(999L)).thenThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

        mockMvc.perform(get("/api/v1/community/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
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
}
