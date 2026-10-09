package com.youthguide.api.review.repository;

import com.youthguide.api.review.domain.CommunityPost;
import org.springframework.data.repository.Repository;

public interface CommunityPostCommandRepository extends Repository<CommunityPost, Long> {

    CommunityPost save(CommunityPost post);
}
