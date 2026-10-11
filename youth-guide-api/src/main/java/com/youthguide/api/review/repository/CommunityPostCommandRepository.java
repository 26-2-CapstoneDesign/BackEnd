package com.youthguide.api.review.repository;

import com.youthguide.api.review.domain.CommunityPost;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface CommunityPostCommandRepository extends Repository<CommunityPost, Long> {

    CommunityPost save(CommunityPost post);

    Optional<CommunityPost> findByPostIdAndPostKind(Long postId, String postKind);
}
