package com.youthguide.api.review.service;

import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.dto.CommunityPostUpdateRequest;
import com.youthguide.api.review.repository.CommunityPostCommandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.youthguide.api.review.service.CommunityPostCommandService.UpdateStatus.AUTHOR_MISMATCH;
import static com.youthguide.api.review.service.CommunityPostCommandService.UpdateStatus.TARGET_NOT_FOUND;
import static com.youthguide.api.review.service.CommunityPostCommandService.UpdateStatus.UPDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityPostUpdateServiceTest {

    private static final String TEST_POST_KIND = "TEST_ONLY_POST_KIND";

    @Mock
    private CommunityPostCommandRepository repository;

    @InjectMocks
    private CommunityPostCommandService service;

    @Test
    void updatesOwnedPostAndCarriesRequestedAndCurrentVersions() {
        CommunityPost post = existingPost(100L, 200L, 300L, 7L);
        LocalDateTime originalCreatedAt = field(post, "createdAt");
        LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC);
        when(repository.findByPostIdAndPostKind(300L, TEST_POST_KIND)).thenReturn(Optional.of(post));
        CommunityPostUpdateRequest request = new CommunityPostUpdateRequest(
                "201",
                "수정한 시험 후기",
                "수정한 시험 후기 본문",
                "7"
        );

        CommunityPostCommandService.UpdateResult result = service.updatePost(
                300L,
                100L,
                TEST_POST_KIND,
                request
        );
        LocalDateTime after = LocalDateTime.now(ZoneOffset.UTC);

        assertThat(result.status()).isEqualTo(UPDATED);
        assertThat(result.postId()).isEqualTo(300L);
        assertThat(result.requestedVersion()).isEqualTo(7L);
        assertThat(result.currentVersion()).isEqualTo(7L);
        assertThat(ReflectionTestUtils.getField(post, "certificateId")).isEqualTo(201L);
        assertThat(ReflectionTestUtils.getField(post, "title")).isEqualTo("수정한 시험 후기");
        assertThat(ReflectionTestUtils.getField(post, "body")).isEqualTo("수정한 시험 후기 본문");
        LocalDateTime updatedAt = field(post, "updatedAt");
        assertThat((LocalDateTime) ReflectionTestUtils.getField(post, "createdAt")).isEqualTo(originalCreatedAt);
        assertThat(updatedAt).isBetween(before, after);
        verify(repository).findByPostIdAndPostKind(300L, TEST_POST_KIND);
        verify(repository, never()).save(post);
    }

    @Test
    void detectsMissingTargetWithoutThrowingCustomException() {
        when(repository.findByPostIdAndPostKind(999L, TEST_POST_KIND)).thenReturn(Optional.empty());
        CommunityPostUpdateRequest request = new CommunityPostUpdateRequest("201", "수정 제목", "수정 본문", "7");

        CommunityPostCommandService.UpdateResult result = service.updatePost(
                999L,
                100L,
                TEST_POST_KIND,
                request
        );

        assertThat(result.status()).isEqualTo(TARGET_NOT_FOUND);
        assertThat(result.postId()).isEqualTo(999L);
        assertThat(result.requestedVersion()).isEqualTo(7L);
        assertThat(result.currentVersion()).isNull();
    }

    @Test
    void detectsAuthorMismatchWithoutChangingPost() {
        CommunityPost post = existingPost(100L, 200L, 300L, 7L);
        LocalDateTime originalUpdatedAt = field(post, "updatedAt");
        when(repository.findByPostIdAndPostKind(300L, TEST_POST_KIND)).thenReturn(Optional.of(post));
        CommunityPostUpdateRequest request = new CommunityPostUpdateRequest("201", "수정 제목", "수정 본문", "6");

        CommunityPostCommandService.UpdateResult result = service.updatePost(
                300L,
                999L,
                TEST_POST_KIND,
                request
        );

        assertThat(result.status()).isEqualTo(AUTHOR_MISMATCH);
        assertThat(result.requestedVersion()).isEqualTo(6L);
        assertThat(result.currentVersion()).isEqualTo(7L);
        assertThat(ReflectionTestUtils.getField(post, "certificateId")).isEqualTo(200L);
        assertThat(ReflectionTestUtils.getField(post, "title")).isEqualTo("기존 제목");
        assertThat(ReflectionTestUtils.getField(post, "body")).isEqualTo("기존 본문");
        assertThat((LocalDateTime) ReflectionTestUtils.getField(post, "updatedAt")).isEqualTo(originalUpdatedAt);
        verify(repository, never()).save(post);
    }

    private CommunityPost existingPost(Long authorMemberId, Long certificateId, Long postId, Long version) {
        CommunityPost post = CommunityPost.create(
                authorMemberId,
                certificateId,
                TEST_POST_KIND,
                "기존 제목",
                "기존 본문"
        );
        ReflectionTestUtils.setField(post, "postId", postId);
        ReflectionTestUtils.setField(post, "lockVersion", version);
        return post;
    }

    @SuppressWarnings("unchecked")
    private static <T> T field(CommunityPost post, String name) {
        return (T) ReflectionTestUtils.getField(post, name);
    }
}
