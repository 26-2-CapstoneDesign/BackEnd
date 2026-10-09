package com.youthguide.api.review.service;

import com.youthguide.api.review.dto.CommunityPostDetailResponse;
import com.youthguide.api.review.dto.CommunityPostPageResponse;
import com.youthguide.api.review.dto.CommunityPostSummaryResponse;
import com.youthguide.api.review.repository.CommunityPostQueryRepository;
import com.youthguide.api.review.repository.projection.CommunityPostDetailProjection;
import com.youthguide.api.review.repository.projection.CommunityPostSummaryProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityPostQueryServiceTest {

    private static final String TEST_POST_KIND = "TEST_ONLY_POST_KIND";

    @Mock
    private CommunityPostQueryRepository repository;

    @InjectMocks
    private CommunityPostQueryService service;

    @Test
    void getsPageWithEscapedSearchAndUnsortedPageRequest() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 10, 1, 2, 3);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 10, 11, 4, 5, 6);
        CommunityPostSummaryProjection first = summaryProjection(
                101L, 201L, "정보처리기사", "첫 번째 후기", "작성자",
                createdAt, updatedAt, 12L, 3L
        );
        CommunityPostSummaryProjection second = summaryProjection(
                100L, 202L, "정보처리산업기사", "두 번째 후기", "다른 작성자",
                createdAt.minusDays(1), updatedAt.minusDays(1), 0L, 0L
        );
        PageRequest expectedPageRequest = PageRequest.of(1, 2);
        when(repository.findPage(TEST_POST_KIND, "%100\\%\\_C:\\\\temp%", expectedPageRequest))
                .thenReturn(new PageImpl<>(List.of(first, second), expectedPageRequest, 5));

        CommunityPostPageResponse response = service.getPosts(
                TEST_POST_KIND, "100%_C:\\temp", 1, 2
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findPage(
                org.mockito.ArgumentMatchers.eq(TEST_POST_KIND),
                org.mockito.ArgumentMatchers.eq("%100\\%\\_C:\\\\temp%"),
                pageableCaptor.capture()
        );
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(2);
        assertThat(pageable.getSort().isUnsorted()).isTrue();

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.items()).hasSize(2);

        CommunityPostSummaryResponse item = response.items().getFirst();
        assertThat(item.postId()).isEqualTo("101");
        assertThat(item.certificateId()).isEqualTo("201");
        assertThat(item.certificateName()).isEqualTo("정보처리기사");
        assertThat(item.title()).isEqualTo("첫 번째 후기");
        assertThat(item.author().nickname()).isEqualTo("작성자");
        assertThat(item.createdAt()).isEqualTo(createdAt.toInstant(ZoneOffset.UTC));
        assertThat(item.updatedAt()).isEqualTo(updatedAt.toInstant(ZoneOffset.UTC));
        assertThat(item.viewCount()).isEqualTo(12L);
        assertThat(item.likeCount()).isEqualTo(3L);
    }

    @Test
    void convertsMissingSearchValuesToNullPattern() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(repository.findPage(TEST_POST_KIND, null, pageRequest))
                .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

        service.getPosts(TEST_POST_KIND, null, 0, 20);
        service.getPosts(TEST_POST_KIND, "", 0, 20);
        service.getPosts(TEST_POST_KIND, "   ", 0, 20);

        verify(repository, org.mockito.Mockito.times(3))
                .findPage(TEST_POST_KIND, null, pageRequest);
    }

    @Test
    void preservesNullableCertificateInSummaryResponse() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 10, 1, 2, 3);
        CommunityPostSummaryProjection projection = summaryProjection(
                101L, null, null, "자격증 없는 글", "작성자",
                timestamp, timestamp, 0L, 0L
        );
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(repository.findPage(TEST_POST_KIND, null, pageRequest))
                .thenReturn(new PageImpl<>(List.of(projection), pageRequest, 1));

        CommunityPostSummaryResponse response = service
                .getPosts(TEST_POST_KIND, null, 0, 20)
                .items()
                .getFirst();

        assertThat(response.certificateId()).isNull();
        assertThat(response.certificateName()).isNull();
        assertThat(response.viewCount()).isZero();
        assertThat(response.likeCount()).isZero();
    }

    @Test
    void getsDetailAndConvertsProjectionToResponse() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 10, 1, 2, 3);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 10, 11, 4, 5, 6);
        CommunityPostDetailProjection projection = org.mockito.Mockito.mock(CommunityPostDetailProjection.class);
        when(projection.getPostId()).thenReturn(101L);
        when(projection.getCertificateId()).thenReturn(null);
        when(projection.getCertificateName()).thenReturn(null);
        when(projection.getTitle()).thenReturn("상세 후기");
        when(projection.getBody()).thenReturn("상세 본문");
        when(projection.getNickname()).thenReturn("작성자");
        when(projection.getCreatedAt()).thenReturn(createdAt);
        when(projection.getUpdatedAt()).thenReturn(updatedAt);
        when(projection.getLockVersion()).thenReturn(7L);
        when(projection.getViewCount()).thenReturn(20L);
        when(projection.getLikeCount()).thenReturn(4L);
        when(repository.findDetail(101L, TEST_POST_KIND)).thenReturn(Optional.of(projection));

        Optional<CommunityPostDetailResponse> result = service.getPost(TEST_POST_KIND, 101L);

        assertThat(result).hasValueSatisfying(response -> {
            assertThat(response.postId()).isEqualTo("101");
            assertThat(response.certificateId()).isNull();
            assertThat(response.certificateName()).isNull();
            assertThat(response.title()).isEqualTo("상세 후기");
            assertThat(response.body()).isEqualTo("상세 본문");
            assertThat(response.author().nickname()).isEqualTo("작성자");
            assertThat(response.createdAt()).isEqualTo(createdAt.toInstant(ZoneOffset.UTC));
            assertThat(response.updatedAt()).isEqualTo(updatedAt.toInstant(ZoneOffset.UTC));
            assertThat(response.version()).isEqualTo("7");
            assertThat(response.viewCount()).isEqualTo(20L);
            assertThat(response.likeCount()).isEqualTo(4L);
        });
    }

    @Test
    void returnsEmptyWhenDetailDoesNotExist() {
        when(repository.findDetail(999L, TEST_POST_KIND)).thenReturn(Optional.empty());

        Optional<CommunityPostDetailResponse> result = service.getPost(TEST_POST_KIND, 999L);

        assertThat(result).isEmpty();
    }

    private CommunityPostSummaryProjection summaryProjection(
            Long postId,
            Long certificateId,
            String certificateName,
            String title,
            String nickname,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long viewCount,
            Long likeCount
    ) {
        CommunityPostSummaryProjection projection = org.mockito.Mockito.mock(CommunityPostSummaryProjection.class);
        when(projection.getPostId()).thenReturn(postId);
        when(projection.getCertificateId()).thenReturn(certificateId);
        when(projection.getCertificateName()).thenReturn(certificateName);
        when(projection.getTitle()).thenReturn(title);
        when(projection.getNickname()).thenReturn(nickname);
        when(projection.getCreatedAt()).thenReturn(createdAt);
        when(projection.getUpdatedAt()).thenReturn(updatedAt);
        when(projection.getViewCount()).thenReturn(viewCount);
        when(projection.getLikeCount()).thenReturn(likeCount);
        return projection;
    }
}
