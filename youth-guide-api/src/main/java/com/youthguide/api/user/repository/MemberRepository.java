package com.youthguide.api.user.repository;

import com.youthguide.api.user.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByLoginNameKey(String loginNameKey);

	boolean existsByNicknameKey(String nicknameKey);

	boolean existsByEmailKey(String emailKey);

	Optional<Member> findByLoginNameKey(String loginNameKey);
}
