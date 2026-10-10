package com.youthguide.api.user.controller;

import com.youthguide.api.user.dto.EmailCodeConfirmRequest;
import com.youthguide.api.user.dto.EmailCodeSendRequest;
import com.youthguide.api.user.service.EmailVerificationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원가입 이메일 인증 (GAP-04, REG-05/06) [주소 제안].
 * 인증 상태는 세션 쿠키에 묶이므로 프론트는 credentials: 'include'로 호출해야 한다.
 */
@RestController
@RequestMapping("/api/v1/members/email-verifications")
public class EmailVerificationController {

	private final EmailVerificationService emailVerificationService;

	public EmailVerificationController(EmailVerificationService emailVerificationService) {
		this.emailVerificationService = emailVerificationService;
	}

	/** 인증번호 발송. 성공 시 204. */
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void send(@Valid @RequestBody EmailCodeSendRequest request, HttpSession session) {
		emailVerificationService.sendCode(request.email(), session);
	}

	/** 인증번호 확인. 성공 시 204. 이후 같은 브라우저에서 30분 안에 가입해야 한다. */
	@PostMapping("/confirm")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void confirm(@Valid @RequestBody EmailCodeConfirmRequest request, HttpSession session) {
		emailVerificationService.confirmCode(request.email(), request.code(), session);
	}
}
