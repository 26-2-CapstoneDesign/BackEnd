package com.youthguide.api.global.exception;

import org.springframework.http.HttpStatus;

/**
 * API 공통 오류 코드. 프론트는 message 대신 code로 분기한다.
 */
public enum ErrorCode {

	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
	PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호 확인이 일치하지 않습니다."),

	EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증을 먼저 완료해 주세요."),
	EMAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었거나 발송 기록이 없습니다. 인증번호를 다시 받아 주세요."),
	EMAIL_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
	EMAIL_CODE_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "인증번호 입력 횟수를 초과했습니다. 인증번호를 다시 받아 주세요."),
	EMAIL_RESEND_TOO_SOON(HttpStatus.TOO_MANY_REQUESTS, "인증번호는 1분 뒤에 다시 받을 수 있습니다."),
	EMAIL_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "인증 메일을 보내지 못했습니다. 잠시 후 다시 시도해 주세요."),

	DUPLICATE_LOGIN_NAME(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
	VERSION_CONFLICT(HttpStatus.CONFLICT, "데이터 버전이 일치하지 않습니다."),

	// 아이디가 없는 경우와 비밀번호가 틀린 경우를 구분하지 않는다 (WBS 06.04 "동일 실패 응답").
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
	UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "요청을 처리할 권한이 없습니다."),

	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
	CERTIFICATE_NOT_FOUND(HttpStatus.NOT_FOUND, "자격증을 찾을 수 없습니다."),
	COMMENT_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "댓글 답글 깊이는 1단계까지만 허용됩니다."),

	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
}
