package com.youthguide.api.global.response;

import com.youthguide.api.global.exception.ErrorCode;

import java.util.List;

/**
 * 모든 오류 응답의 공통 형식.
 * <pre>
 * { "code": "DUPLICATE_LOGIN_NAME", "message": "이미 사용 중인 아이디입니다.", "fieldErrors": [] }
 * </pre>
 * 성공 응답은 감싸지 않고 DTO를 그대로 반환한다. 본문이 없는 성공은 204를 쓴다.
 */
public record ErrorResponse(String code, String message, List<FieldErrorDetail> fieldErrors) {

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage(), List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorDetail> fieldErrors) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage(), List.copyOf(fieldErrors));
	}

	public record FieldErrorDetail(String field, String message) {
	}
}
