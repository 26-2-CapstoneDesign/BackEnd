package com.youthguide.api.user.controller;

import com.youthguide.api.user.dto.AvailabilityResponse;
import com.youthguide.api.user.dto.SignupRequest;
import com.youthguide.api.user.dto.SignupResponse;
import com.youthguide.api.user.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	/** MEM-01 회원가입. 성공 시 201. 로그인은 따로 해야 한다 (REG-10: 가입 후 로그인 화면으로 이동). */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
		return memberService.signup(request);
	}

	/**
	 * GAP-03 중복확인 [주소 제안]. 셋 중 하나만 보낸다.
	 * 예: GET /api/v1/members/availability?loginName=youth01
	 */
	@GetMapping("/availability")
	public AvailabilityResponse availability(
			@RequestParam(required = false) String loginName,
			@RequestParam(required = false) String nickname,
			@RequestParam(required = false) String email) {
		return memberService.checkAvailability(loginName, nickname, email);
	}
}
