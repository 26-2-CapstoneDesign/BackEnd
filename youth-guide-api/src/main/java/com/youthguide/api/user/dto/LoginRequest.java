package com.youthguide.api.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * AUTH-01 POST /api/v1/auth/login 요청 본문.
 * rememberMe: 로그인 상태 유지(LOG-06). 생략하거나 null이면 false.
 * Jackson 3은 기본형(boolean) 필드가 비어 있으면 오류를 내므로 Boolean으로 받는다.
 * 로그인에서는 형식 검사를 하지 않는다. 형식 오류도 "아이디 또는 비밀번호가 올바르지 않습니다"로 같게 응답한다.
 */
public record LoginRequest(
		@NotBlank(message = "아이디를 입력해 주세요.")
		String loginName,

		@NotBlank(message = "비밀번호를 입력해 주세요.")
		String password,

		Boolean rememberMe
) {

	/** 값이 없으면 false로 본다. */
	public boolean isRememberMe() {
		return Boolean.TRUE.equals(rememberMe);
	}

	@Override
	public String toString() {
		return "LoginRequest[loginName=" + loginName + ", rememberMe=" + rememberMe + "]";
	}
}
