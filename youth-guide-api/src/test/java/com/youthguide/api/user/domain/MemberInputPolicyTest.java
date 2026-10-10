package com.youthguide.api.user.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class MemberInputPolicyTest {

	@ParameterizedTest
	@ValueSource(strings = {
			"Abcdefg1",              // 대문자+소문자+숫자, 8자
			"abcdefg1!",             // 소문자+숫자+특수
			"ABCDEFG1!",             // 대문자+숫자+특수
			"Abcdefgh!",             // 대문자+소문자+특수
			"Aa1!Aa1!Aa1!Aa1!Aa1!"   // 20자, 4종류
	})
	void 비밀번호_규칙에_맞으면_통과한다(String password) {
		assertThat(MemberInputPolicy.isValidPassword(password)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Abcde1!",                // 7자: 너무 짧음
			"Aa1!Aa1!Aa1!Aa1!Aa1!A",  // 21자: 너무 김
			"abcdefgh1",              // 소문자+숫자: 2종류
			"abcdefgh!",              // 소문자+특수: 2종류
			"ABCDEFGH",               // 1종류
			"Abcdef 1!",              // 공백 포함
			"Abcdefg1한"              // 한글 포함
	})
	void 비밀번호_규칙에_어긋나면_거부한다(String password) {
		assertThat(MemberInputPolicy.isValidPassword(password)).isFalse();
	}

	@Test
	void 비밀번호_null은_거부한다() {
		assertThat(MemberInputPolicy.isValidPassword(null)).isFalse();
	}

	@Test
	void 비교키는_대소문자와_앞뒤공백을_무시한다() {
		assertThat(MemberInputPolicy.loginNameKey("  Youth01 ")).isEqualTo("youth01");
		assertThat(MemberInputPolicy.emailKey("User@Example.COM")).isEqualTo("user@example.com");
	}

	@Test
	void 비교키는_전각문자를_반각으로_맞춘다() {
		// 전각 'Ａ' (U+FF21) → 'a'
		assertThat(MemberInputPolicy.nicknameKey("Ａbc")).isEqualTo("abc");
	}

	@Test
	void 아이디와_닉네임_형식을_검사한다() {
		assertThat(MemberInputPolicy.isValidLoginName("youth01")).isTrue();
		assertThat(MemberInputPolicy.isValidLoginName("ab")).isFalse();
		assertThat(MemberInputPolicy.isValidLoginName("youth_01")).isFalse();
		assertThat(MemberInputPolicy.isValidNickname("청년길잡이")).isTrue();
		assertThat(MemberInputPolicy.isValidNickname("a")).isFalse();
		assertThat(MemberInputPolicy.isValidEmail("user@example.com")).isTrue();
		assertThat(MemberInputPolicy.isValidEmail("user@example")).isFalse();
	}
}
