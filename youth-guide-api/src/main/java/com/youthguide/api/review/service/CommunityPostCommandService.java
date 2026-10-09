package com.youthguide.api.review.service;

import com.youthguide.api.review.domain.CommunityPost;
import com.youthguide.api.review.dto.CommunityPostCreateRequest;
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
}
