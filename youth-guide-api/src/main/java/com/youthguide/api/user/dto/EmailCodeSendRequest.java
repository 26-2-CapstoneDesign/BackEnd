package com.youthguide.api.user.dto;

import jakarta.validation.constraints.NotBlank;

/** 인증번호 발송 요청. 형식 검사는 서비스에서 한다. */
public record EmailCodeSendRequest(
		@NotBlank(message = "이메일을 입력해 주세요.")
		String email
) {
}
