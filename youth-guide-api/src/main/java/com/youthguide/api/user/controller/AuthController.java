package com.youthguide.api.user.controller;

import com.youthguide.api.global.security.LoginMember;
import com.youthguide.api.global.security.SessionLoginHandler;
import com.youthguide.api.user.dto.LoginRequest;
import com.youthguide.api.user.dto.MeResponse;
import com.youthguide.api.user.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AUTH-01 로그인. AUTH-02 로그아웃(POST /api/v1/auth/logout)은 SecurityConfig의 logout 설정이 처리한다.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final SessionLoginHandler loginHandler;
	private final MemberService memberService;

	public AuthController(SessionLoginHandler loginHandler, MemberService memberService) {
		this.loginHandler = loginHandler;
		this.memberService = memberService;
	}

	/** 성공: 200 + 내 정보. 실패: 401 LOGIN_FAILED (아이디 없음/비밀번호 틀림 구분 없음). */
	@PostMapping("/login")
	public MeResponse login(@Valid @RequestBody LoginRequest loginRequest,
			HttpServletRequest request, HttpServletResponse response) {
		LoginMember loginMember = loginHandler.login(loginRequest.loginName(), loginRequest.password(),
				loginRequest.isRememberMe(), request, response);
		return memberService.getMe(loginMember.getMemberId());
	}
}
