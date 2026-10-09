package com.youthguide.api.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(name = "community_post")
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "author_member_id", nullable = false)
    private Long authorMemberId;

    @Column(name = "certificate_id")
    private Long certificateId;

    @Column(name = "post_kind", nullable = false, length = 64)
    private String postKind;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Lob
    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected CommunityPost() {
    }

    public static CommunityPost create(
            Long authorMemberId,
            Long certificateId,
            String postKind,
            String title,
            String body
    ) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        CommunityPost post = new CommunityPost();
        post.authorMemberId = authorMemberId;
        post.certificateId = certificateId;
        post.postKind = postKind;
        post.title = title;
        post.body = body;
        post.createdAt = now;
        post.updatedAt = now;
        return post;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getLockVersion() {
        return lockVersion;
    }

    public boolean isAuthoredBy(Long memberId) {
        return Objects.equals(authorMemberId, memberId);
    }

    public void updateContent(Long certificateId, String title, String body) {
        this.certificateId = certificateId;
        this.title = title;
        this.body = body;
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
