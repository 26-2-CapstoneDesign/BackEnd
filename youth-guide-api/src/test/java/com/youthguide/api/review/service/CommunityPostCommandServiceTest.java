package com.youthguide.api.review.service;

import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.dto.CommunityPostCreateRequest;
import com.youthguide.api.review.repository.CommunityPostCommandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityPostCommandServiceTest {

    private static final String TEST_POST_KIND = "TEST_ONLY_POST_KIND";

    @Mock
    private CommunityPostCommandRepository repository;

    @InjectMocks
    private CommunityPostCommandService service;

    @Test
    void createsPostWithScalarIdsAndUtcTimestamps() {
        CommunityPostCreateRequest request = new CommunityPostCreateRequest(
                "200",
                "정보처리기사 후기",
                "시험 준비 과정과 응시 경험입니다."
        );
        LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC);
        when(repository.save(any(CommunityPost.class))).thenAnswer(invocation -> {
            CommunityPost post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "postId", 300L);
            return post;
        });

        Long postId = service.createPost(100L, TEST_POST_KIND, request);
        LocalDateTime after = LocalDateTime.now(ZoneOffset.UTC);

        ArgumentCaptor<CommunityPost> captor = ArgumentCaptor.forClass(CommunityPost.class);
        verify(repository).save(captor.capture());
        CommunityPost savedPost = captor.getValue();

        assertThat(postId).isEqualTo(300L);
        assertThat(ReflectionTestUtils.getField(savedPost, "authorMemberId")).isEqualTo(100L);
        assertThat(ReflectionTestUtils.getField(savedPost, "certificateId")).isEqualTo(200L);
        assertThat(ReflectionTestUtils.getField(savedPost, "postKind")).isEqualTo(TEST_POST_KIND);
        assertThat(ReflectionTestUtils.getField(savedPost, "title")).isEqualTo("정보처리기사 후기");
        assertThat(ReflectionTestUtils.getField(savedPost, "body")).isEqualTo("시험 준비 과정과 응시 경험입니다.");

        LocalDateTime createdAt = (LocalDateTime) ReflectionTestUtils.getField(savedPost, "createdAt");
        LocalDateTime updatedAt = (LocalDateTime) ReflectionTestUtils.getField(savedPost, "updatedAt");
        assertThat(createdAt).isBetween(before, after);
        assertThat(updatedAt).isEqualTo(createdAt);
        assertThat(ReflectionTestUtils.getField(savedPost, "lockVersion")).isNull();
    }
}
