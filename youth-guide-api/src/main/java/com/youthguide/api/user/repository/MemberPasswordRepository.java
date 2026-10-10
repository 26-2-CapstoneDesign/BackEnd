package com.youthguide.api.user.repository;

import com.youthguide.api.user.domain.MemberPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPasswordRepository extends JpaRepository<MemberPassword, Long> {
}
