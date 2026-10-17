package com.youthguide.api.review.service;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.review.domain.CommunityPostKind;
import com.youthguide.api.review.dto.CommunityPostCommentPageResponse;
import com.youthguide.api.review.dto.CommunityPostCommentResponse;
import com.youthguide.api.review.repository.CommunityPostCommentQueryRepository;
import com.youthguide.api.review.repository.CommunityPostQueryRepository;
import com.youthguide.api.review.repository.projection.CommunityPostCommentProjection;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityPostCommentQueryServiceTest {

    @Mock
    private CommunityPostQueryRepository postRepository;

    @Mock
    private CommunityPostCommentQueryRepository commentRepository;

    @InjectMocks
    private CommunityPostCommentQueryService service;

    @Test
    void returnsTopLevelPageWithNestedRepliesAndViewerOwnership() {
        when(postRepository.existsByPostIdAndPostKind(10L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(true);
        LocalDateTime firstTime = LocalDateTime.of(2026, 10, 12, 1, 0);
        CommunityPostCommentProjection first = projection(
                101L, null, null, null, 20L, "작성자", "첫 댓글", firstTime, null, 0L
        );
        CommunityPostCommentProjection second = projection(
                105L, null, null, null, 30L, "다른 사람", "둘째 댓글", firstTime.plusMinutes(4), null, 1L
        );
        PageRequest pageRequest = PageRequest.of(1, 2);
        when(commentRepository.findTopLevelPage(10L, pageRequest))
                .thenReturn(new PageImpl<>(List.of(first, second), pageRequest, 5));
        CommunityPostCommentProjection directReply = projection(
                102L, 101L, null, null, 30L, "B 작성자", "A에게 답한 B", firstTime.plusMinutes(1), null, 0L
        );
        CommunityPostCommentProjection replyToB = projection(
                103L, 101L, 102L, "B 작성자", 40L, "C 작성자", "B에게 답한 C", firstTime.plusMinutes(2), null, 0L
        );
        CommunityPostCommentProjection replyToC = projection(
                104L, 101L, 103L, "C 작성자", 50L, "D 작성자", "C에게 답한 D", firstTime.plusMinutes(3), null, 0L
        );
        when(commentRepository.findRepliesByParentCommentIds(List.of(101L, 105L)))
                .thenReturn(List.of(directReply, replyToB, replyToC));

        CommunityPostCommentPageResponse response = service.getComments(10L, 1, 2, 20L);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(commentRepository).findTopLevelPage(
                org.mockito.ArgumentMatchers.eq(10L),
                pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getSort().isUnsorted()).isTrue();
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.items()).extracting(CommunityPostCommentResponse::commentId)
                .containsExactly("101", "105");
        CommunityPostCommentResponse firstResponse = response.items().getFirst();
        assertThat(firstResponse.mine()).isTrue();
        assertThat(firstResponse.replies())
                .extracting(reply -> reply.commentId())
                .containsExactly("102", "103", "104");
        assertThat(firstResponse.replies())
                .extracting(reply -> reply.parentCommentId())
                .containsOnly("101");
        assertThat(firstResponse.replies().get(0).replyToCommentId()).isNull();
        assertThat(firstResponse.replies().get(0).replyToNickname()).isNull();
        assertThat(firstResponse.replies().get(1).replyToCommentId()).isEqualTo("102");
        assertThat(firstResponse.replies().get(1).replyToNickname()).isEqualTo("B 작성자");
        assertThat(firstResponse.replies().get(2).replyToCommentId()).isEqualTo("103");
        assertThat(firstResponse.replies().get(2).replyToNickname()).isEqualTo("C 작성자");
        assertThat(firstResponse.replies().getFirst().mine()).isFalse();
        assertThat(firstResponse.createdAt()).isEqualTo(firstTime.toInstant(ZoneOffset.UTC));
    }

    @Test
    void returnsAnonymousMineFalseAndTombstoneWithoutAuthorOrOriginalBody() {
        when(postRepository.existsByPostIdAndPostKind(10L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(true);
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 12, 1, 0);
        CommunityPostCommentProjection deleted = projection(
                101L, null, null, null, 20L, "숨겨질 작성자", "", timestamp, timestamp.plusHours(1), 2L
        );
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(commentRepository.findTopLevelPage(10L, pageRequest))
                .thenReturn(new PageImpl<>(List.of(deleted), pageRequest, 1));
        when(commentRepository.findRepliesByParentCommentIds(List.of(101L)))
                .thenReturn(List.of());

        CommunityPostCommentResponse response = service.getComments(10L, 0, 20, null)
                .items().getFirst();

        assertThat(response.authorNickname()).isNull();
        assertThat(response.body()).isEqualTo("삭제된 댓글입니다.");
        assertThat(response.mine()).isFalse();
        assertThat(response.deleted()).isTrue();
    }

    @Test
    void skipsReplyQueryWhenTopLevelPageIsEmpty() {
        when(postRepository.existsByPostIdAndPostKind(10L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(true);
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(commentRepository.findTopLevelPage(10L, pageRequest))
                .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

        CommunityPostCommentPageResponse response = service.getComments(10L, 0, 20, 20L);

        assertThat(response.items()).isEmpty();
        verify(commentRepository, never()).findRepliesByParentCommentIds(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void rejectsMissingOrNonExamReviewPostBeforeCommentQueries() {
        when(postRepository.existsByPostIdAndPostKind(999L, CommunityPostKind.EXAM_REVIEW))
                .thenReturn(false);

        assertThatThrownBy(() -> service.getComments(999L, 0, 20, null))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
        verify(commentRepository, never()).findTopLevelPage(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(Pageable.class)
        );
    }

    private CommunityPostCommentProjection projection(
            Long commentId,
            Long parentCommentId,
            Long replyToCommentId,
            String replyToNickname,
            Long authorMemberId,
            String authorNickname,
            String body,
            LocalDateTime createdAt,
            LocalDateTime deletedAt,
            Long version
    ) {
        CommunityPostCommentProjection projection = org.mockito.Mockito.mock(CommunityPostCommentProjection.class);
        org.mockito.Mockito.lenient().when(projection.getCommentId()).thenReturn(commentId);
        org.mockito.Mockito.lenient().when(projection.getParentCommentId()).thenReturn(parentCommentId);
        org.mockito.Mockito.lenient().when(projection.getReplyToCommentId()).thenReturn(replyToCommentId);
        org.mockito.Mockito.lenient().when(projection.getReplyToNickname()).thenReturn(replyToNickname);
        org.mockito.Mockito.lenient().when(projection.getAuthorMemberId()).thenReturn(authorMemberId);
        org.mockito.Mockito.lenient().when(projection.getAuthorNickname()).thenReturn(authorNickname);
        org.mockito.Mockito.lenient().when(projection.getBody()).thenReturn(body);
        org.mockito.Mockito.lenient().when(projection.getCreatedAt()).thenReturn(createdAt);
        org.mockito.Mockito.lenient().when(projection.getUpdatedAt()).thenReturn(createdAt.plusMinutes(1));
        org.mockito.Mockito.lenient().when(projection.getDeletedAt()).thenReturn(deletedAt);
        org.mockito.Mockito.lenient().when(projection.getLockVersion()).thenReturn(version);
        return projection;
    }
}
