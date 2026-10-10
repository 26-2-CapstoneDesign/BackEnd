package com.youthguide.api.global.security;

import com.youthguide.api.user.domain.Member;
import com.youthguide.api.user.domain.MemberInputPolicy;
import com.youthguide.api.user.domain.MemberPassword;
import com.youthguide.api.user.repository.MemberPasswordRepository;
import com.youthguide.api.user.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 아이디로 회원과 비밀번호 해시를 읽는다. 로그인과 로그인 상태 유지(remember-me) 쿠키 검증에 쓰인다.
 * 입력값은 항상 정규화해서 login_name_key로 찾는다 (이미 정규화된 값을 다시 정규화해도 결과가 같다).
 */
@Service
public class MemberUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;
	private final MemberPasswordRepository memberPasswordRepository;

	public MemberUserDetailsService(MemberRepository memberRepository,
			MemberPasswordRepository memberPasswordRepository) {
		this.memberRepository = memberRepository;
		this.memberPasswordRepository = memberPasswordRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
		String key = MemberInputPolicy.loginNameKey(loginName);
		Member member = memberRepository.findByLoginNameKey(key)
				.orElseThrow(() -> new UsernameNotFoundException("member not found"));
		MemberPassword password = memberPasswordRepository.findById(member.getId())
				.orElseThrow(() -> new UsernameNotFoundException("password not found"));
		return new LoginMember(member.getId(), member.getLoginNameKey(), password.getPasswordHash());
	}
}
