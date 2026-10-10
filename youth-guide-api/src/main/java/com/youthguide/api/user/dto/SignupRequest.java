package com.youthguide.api.user.dto;

import com.youthguide.api.user.domain.Gender;
import com.youthguide.api.user.domain.MemberInputPolicy;
import com.youthguide.api.user.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * MEM-01 POST /api/v1/members 요청 본문 (REG-01~07, 09~11).
 * 이메일은 먼저 /api/v1/members/email-verifications로 인증을 끝내야 한다.
 * 약관 동의(REG-08)는 팀 회의 후 저장 방식이 정해지면 추가한다.
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
		String email,

		@NotBlank(message = "이름을 입력해 주세요.")
		@Size(max = MemberInputPolicy.NAME_MAX_LENGTH, message = "이름은 20자 이하로 입력해 주세요.")
		String name,

		@NotNull(message = "성별을 선택해 주세요.")
		Gender gender,

		@NotNull(message = "생년월일을 입력해 주세요.")
		@Past(message = "생년월일이 올바르지 않습니다.")
		LocalDate birthDate
) {

	/** 로그·디버깅 출력에 비밀번호와 개인정보가 찍히지 않게 한다. */
	@Override
	public String toString() {
		return "SignupRequest[loginName=" + loginName + ", nickname=" + nickname + "]";
	}
}
