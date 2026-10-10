package com.youthguide.api.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * 로그인 성공 후 "로그인 상태를 어떻게 유지할지"를 담당하는 유일한 클래스.
 * 현재는 세션(JSESSIONID 쿠키) + 선택 시 remember-me 쿠키 방식이다.
 * 나중에 JWT로 바꾸면 이 클래스와 SecurityConfig만 교체하면 된다.
 */
@Component
public class SessionLoginHandler {

	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final RememberMeServices rememberMeServices;
	private final SecurityContextHolderStrategy contextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

	public SessionLoginHandler(AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository,
			RememberMeServices rememberMeServices) {
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
		this.rememberMeServices = rememberMeServices;
	}

	/**
	 * 아이디·비밀번호를 검증하고 로그인 상태를 저장한다.
	 * 실패하면 AuthenticationException을 던지고, GlobalExceptionHandler가 LOGIN_FAILED(401)로 바꾼다.
	 *
	 * @return 인증된 회원
	 */
	public LoginMember login(String loginName, String password, boolean rememberMe,
			HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(loginName, password));

		// 세션 고정 공격 방지: 로그인 전에 쓰던 세션이 있으면 ID를 새로 발급한다.
		if (request.getSession(false) != null) {
			request.changeSessionId();
		}

		SecurityContext context = contextHolderStrategy.createEmptyContext();
		context.setAuthentication(authentication);
		contextHolderStrategy.setContext(context);
		securityContextRepository.saveContext(context, request, response);

		if (rememberMe) {
			request.setAttribute(JsonRememberMeServices.REMEMBER_ME_ATTRIBUTE, Boolean.TRUE);
			rememberMeServices.loginSuccess(request, response, authentication);
		}

		return (LoginMember) authentication.getPrincipal();
	}
}
