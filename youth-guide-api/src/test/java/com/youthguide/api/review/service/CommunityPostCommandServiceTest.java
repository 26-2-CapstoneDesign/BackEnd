package com.youthguide.api.review.service;

import com.youthguide.api.certificate.repository.CertificateRepository;
import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.domain.CommunityPostKind;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityPostCommandServiceTest {

    @Mock
    private CommunityPostCommandRepository repository;

    @Mock
    private CertificateRepository certificateRepository;

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
        when(certificateRepository.existsById(200L)).thenReturn(true);
        when(repository.save(any(CommunityPost.class))).thenAnswer(invocation -> {
            CommunityPost post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "postId", 300L);
            return post;
        });

        Long postId = service.createPost(100L, request);
        LocalDateTime after = LocalDateTime.now(ZoneOffset.UTC);

        ArgumentCaptor<CommunityPost> captor = ArgumentCaptor.forClass(CommunityPost.class);
        verify(repository).save(captor.capture());
        CommunityPost savedPost = captor.getValue();

        assertThat(postId).isEqualTo(300L);
        assertThat(ReflectionTestUtils.getField(savedPost, "authorMemberId")).isEqualTo(100L);
        assertThat(ReflectionTestUtils.getField(savedPost, "certificateId")).isEqualTo(200L);
        assertThat(ReflectionTestUtils.getField(savedPost, "postKind"))
                .isEqualTo(CommunityPostKind.EXAM_REVIEW);
        assertThat(ReflectionTestUtils.getField(savedPost, "title")).isEqualTo("정보처리기사 후기");
        assertThat(ReflectionTestUtils.getField(savedPost, "body")).isEqualTo("시험 준비 과정과 응시 경험입니다.");

        LocalDateTime createdAt = (LocalDateTime) ReflectionTestUtils.getField(savedPost, "createdAt");
        LocalDateTime updatedAt = (LocalDateTime) ReflectionTestUtils.getField(savedPost, "updatedAt");
        assertThat(createdAt).isBetween(before, after);
        assertThat(updatedAt).isEqualTo(createdAt);
        assertThat(ReflectionTestUtils.getField(savedPost, "lockVersion")).isNull();
    }

    @Test
    void rejectsMissingCertificateBeforeSavingPost() {
        CommunityPostCreateRequest request = new CommunityPostCreateRequest("999", "시험 후기", "본문");
        when(certificateRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.createPost(100L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CERTIFICATE_NOT_FOUND);

        verify(repository, never()).save(any());
    }
}
