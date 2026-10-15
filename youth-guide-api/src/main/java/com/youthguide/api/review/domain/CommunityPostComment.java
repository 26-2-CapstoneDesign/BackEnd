package com.youthguide.api.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(name = "community_post_comment")
public class CommunityPostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_member_id", nullable = false)
    private Long authorMemberId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "reply_to_comment_id")
    private Long replyToCommentId;

    @Column(name = "body", nullable = false, length = 255)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected CommunityPostComment() {
    }

    public static CommunityPostComment create(
            Long postId,
            Long authorMemberId,
            Long parentCommentId,
            Long replyToCommentId,
            String body
    ) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        CommunityPostComment comment = new CommunityPostComment();
        comment.postId = postId;
        comment.authorMemberId = authorMemberId;
        comment.parentCommentId = parentCommentId;
        comment.replyToCommentId = replyToCommentId;
        comment.body = body;
        comment.createdAt = now;
        comment.updatedAt = now;
        return comment;
    }

    public Long getCommentId() {
        return commentId;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public Long getReplyToCommentId() {
        return replyToCommentId;
    }

    public Long getLockVersion() {
        return lockVersion;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isAuthoredBy(Long memberId) {
        return Objects.equals(authorMemberId, memberId);
    }

    public void updateBody(String body) {
        this.body = body;
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void markDeleted() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.body = "";
        this.deletedAt = now;
        this.updatedAt = now;
    }
}
