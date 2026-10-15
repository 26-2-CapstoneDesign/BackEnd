package com.youthguide.api.review.controller;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.global.exception.GlobalExceptionHandler;
import com.youthguide.api.review.dto.CommunityPostAuthorResponse;
import com.youthguide.api.review.dto.CommunityPostDetailResponse;
import com.youthguide.api.review.dto.CommunityPostPageResponse;
import com.youthguide.api.review.dto.CommunityPostSummaryResponse;
import com.youthguide.api.review.service.CommunityPostQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class CommunityPostQueryControllerTest {

    @Mock
    private CommunityPostQueryService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new CommunityPostQueryController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void connectsQueryPageAndSizeAndReturnsDtoDirectly() throws Exception {
        CommunityPostSummaryResponse item = new CommunityPostSummaryResponse(
                "101",
                "201",
                "정보처리기사",
                "합격 후기",
                new CommunityPostAuthorResponse("작성자"),
                Instant.parse("2026-10-10T01:02:03Z"),
                Instant.parse("2026-10-10T01:02:03Z"),
                12L,
                3L,
                4L
        );
        CommunityPostPageResponse response = new CommunityPostPageResponse(
                List.of(item), 2, 5, 11L, 3, false
        );
        when(service.getPosts("정보처리", 2, 5)).thenReturn(response);

        mockMvc.perform(get("/api/v1/community/posts")
                        .queryParam("q", "정보처리")
                        .queryParam("page", "2")
                        .queryParam("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].postId").value("101"))
                .andExpect(jsonPath("$.items[0].author.nickname").value("작성자"))
                .andExpect(jsonPath("$.items[0].commentCount").value(4))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(service).getPosts("정보처리", 2, 5);
    }

    @Test
    void usesZeroAndTwentyAsDefaultPageValues() throws Exception {
        when(service.getPosts(null, 0, 20))
                .thenReturn(new CommunityPostPageResponse(List.of(), 0, 20, 0L, 0, false));

        mockMvc.perform(get("/api/v1/community/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(service).getPosts(null, 0, 20);
    }

    @Test
    void returnsBadRequestWhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/v1/community/posts")
                        .queryParam("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verifyNoInteractions(service);
    }

    @Test
    void returnsBadRequestWhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/api/v1/community/posts")
                        .queryParam("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verifyNoInteractions(service);
    }

    @Test
    void returnsDetailDtoDirectly() throws Exception {
        CommunityPostDetailResponse response = new CommunityPostDetailResponse(
                "101",
                "201",
                "정보처리기사",
                "합격 후기",
                new CommunityPostAuthorResponse("작성자"),
                Instant.parse("2026-10-10T01:02:03Z"),
                Instant.parse("2026-10-10T01:02:03Z"),
                "상세 본문",
                "7",
                12L,
                3L,
                4L
        );
        when(service.getPost(101L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/community/posts/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value("101"))
                .andExpect(jsonPath("$.certificateName").value("정보처리기사"))
                .andExpect(jsonPath("$.body").value("상세 본문"))
                .andExpect(jsonPath("$.version").value("7"))
                .andExpect(jsonPath("$.commentCount").value(4))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(service).getPost(101L);
    }

    @Test
    void returnsCommonNotFoundResponseWhenDetailDoesNotExist() throws Exception {
        when(service.getPost(999L)).thenThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

        mockMvc.perform(get("/api/v1/community/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}
