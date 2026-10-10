package com.youthguide.api.user.dto;

import jakarta.validation.constraints.NotBlank;

/** 인증번호 확인 요청 */
public record EmailCodeConfirmRequest(
		@NotBlank(message = "이메일을 입력해 주세요.")
		String email,

		@NotBlank(message = "인증번호를 입력해 주세요.")
		String code
) {
}
