package com.youthguide.api.user.repository;

import com.youthguide.api.user.domain.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {
}
