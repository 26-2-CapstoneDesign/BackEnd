package com.youthguide.api.review.service;

import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.dto.CommunityPostCreateRequest;
import com.youthguide.api.review.dto.CommunityPostUpdateRequest;
import com.youthguide.api.review.repository.CommunityPostCommandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommunityPostCommandService {

    private final CommunityPostCommandRepository repository;

    public CommunityPostCommandService(CommunityPostCommandRepository repository) {
        this.repository = repository;
    }

    public Long createPost(Long authorMemberId, String postKind, CommunityPostCreateRequest request) {
        Long certificateId = Long.valueOf(request.certificateId());
        CommunityPost post = CommunityPost.create(
                authorMemberId,
                certificateId,
                postKind,
                request.title(),
                request.body()
        );

        return repository.save(post).getPostId();
    }

    public UpdateResult updatePost(
            Long postId,
            Long authorMemberId,
            String postKind,
            CommunityPostUpdateRequest request
    ) {
        Long requestedVersion = Long.valueOf(request.version());
        CommunityPost post = repository.findByPostIdAndPostKind(postId, postKind).orElse(null);
        if (post == null) {
            return new UpdateResult(UpdateStatus.TARGET_NOT_FOUND, postId, requestedVersion, null);
        }

        Long currentVersion = post.getLockVersion();
        if (!post.isAuthoredBy(authorMemberId)) {
            return new UpdateResult(UpdateStatus.AUTHOR_MISMATCH, postId, requestedVersion, currentVersion);
        }

        if (!requestedVersion.equals(currentVersion)) {
            return new UpdateResult(UpdateStatus.VERSION_CONFLICT, postId, requestedVersion, currentVersion);
        }

        post.updateContent(
                Long.valueOf(request.certificateId()),
                request.title(),
                request.body()
        );
        return new UpdateResult(UpdateStatus.UPDATED, postId, requestedVersion, currentVersion);
    }

    public enum UpdateStatus {
        UPDATED,
        TARGET_NOT_FOUND,
        AUTHOR_MISMATCH,
        VERSION_CONFLICT
    }

    public record UpdateResult(
            UpdateStatus status,
            Long postId,
            Long requestedVersion,
            Long currentVersion
    ) {
    }
}
