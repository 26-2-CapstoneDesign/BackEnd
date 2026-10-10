package com.youthguide.api.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

/**
 * 로그인 상태 유지(LOG-06). 기본 구현은 폼 파라미터 "remember-me"를 읽지만,
 * 이 API는 JSON 본문의 rememberMe 값을 쓰므로 요청 속성으로 전달받는다.
 */
public class JsonRememberMeServices extends TokenBasedRememberMeServices {

	static final String REMEMBER_ME_ATTRIBUTE = JsonRememberMeServices.class.getName() + ".REQUESTED";

	public JsonRememberMeServices(String key, UserDetailsService userDetailsService) {
		super(key, userDetailsService);
	}

	@Override
	protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
		return Boolean.TRUE.equals(request.getAttribute(REMEMBER_ME_ATTRIBUTE));
	}
}
