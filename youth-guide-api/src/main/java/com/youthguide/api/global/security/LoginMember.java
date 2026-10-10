package com.youthguide.api.global.security;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 인증된 회원 정보. 컨트롤러에서 {@code @AuthenticationPrincipal LoginMember}로 받는다.
 * 세션에 저장되므로 최소 정보만 담는다. 비밀번호 해시는 인증 직후 지운다.
 */
public class LoginMember implements UserDetails, CredentialsContainer {

	private static final List<GrantedAuthority> AUTHORITIES = List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));

	private final Long memberId;
	private final String loginNameKey;
	private String passwordHash;

	public LoginMember(Long memberId, String loginNameKey, String passwordHash) {
		this.memberId = memberId;
		this.loginNameKey = loginNameKey;
		this.passwordHash = passwordHash;
	}

	public Long getMemberId() {
		return memberId;
	}

	/** Spring Security의 username = login_name_key (정규화된 아이디) */
	@Override
	public String getUsername() {
		return loginNameKey;
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AUTHORITIES;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void eraseCredentials() {
		this.passwordHash = null;
	}
}
