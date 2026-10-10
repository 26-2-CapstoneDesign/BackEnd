package com.youthguide.api.user.dto;

import com.youthguide.api.user.domain.MemberInputPolicy;
import com.youthguide.api.user.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * MEM-01 POST /api/v1/members 요청 본문.
 * 이메일 인증·약관 동의·기본정보(REG-05~08)는 06.03에서 필드를 추가한다.
 */
public record SignupRequest(
		@NotBlank(message = "아이디를 입력해 주세요.")
		@Pattern(regexp = MemberInputPolicy.LOGIN_NAME_REGEX, message = MemberInputPolicy.LOGIN_NAME_MESSAGE)
		String loginName,

		@NotNull(message = "비밀번호를 입력해 주세요.")
		@ValidPassword
		String password,

		@NotNull(message = "비밀번호 확인을 입력해 주세요.")
		String passwordConfirm,

		@NotBlank(message = "닉네임을 입력해 주세요.")
		@Pattern(regexp = MemberInputPolicy.NICKNAME_REGEX, message = MemberInputPolicy.NICKNAME_MESSAGE)
		String nickname,

		@NotBlank(message = "이메일을 입력해 주세요.")
		@Size(max = MemberInputPolicy.EMAIL_MAX_LENGTH, message = MemberInputPolicy.EMAIL_MESSAGE)
		@Pattern(regexp = MemberInputPolicy.EMAIL_REGEX, message = MemberInputPolicy.EMAIL_MESSAGE)
		String email
) {

	/** 로그·디버깅 출력에 비밀번호가 찍히지 않게 한다. */
	@Override
	public String toString() {
		return "SignupRequest[loginName=" + loginName + ", nickname=" + nickname + ", email=" + email + "]";
	}
}
