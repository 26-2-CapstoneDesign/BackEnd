package com.youthguide.api.user.controller;

import com.youthguide.api.global.security.LoginMember;
import com.youthguide.api.user.dto.MeResponse;
import com.youthguide.api.user.service.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** MEM-02 내 정보. 로그인 안 된 요청은 SecurityConfig에서 401 UNAUTHENTICATED로 막힌다. */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

	private final MemberService memberService;

	public MeController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping
	public MeResponse me(@AuthenticationPrincipal LoginMember loginMember) {
		return memberService.getMe(loginMember.getMemberId());
	}
}
