package com.youthguide.api.user.service;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.user.domain.Member;
import com.youthguide.api.user.domain.MemberPassword;
import com.youthguide.api.user.dto.AvailabilityResponse;
import com.youthguide.api.user.dto.SignupRequest;
import com.youthguide.api.user.dto.SignupResponse;
import com.youthguide.api.user.repository.MemberPasswordRepository;
import com.youthguide.api.user.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	MemberRepository memberRepository;

	@Mock
	MemberPasswordRepository memberPasswordRepository;

	PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

	MemberService memberService;

	@BeforeEach
	void setUp() {
		memberService = new MemberService(memberRepository, memberPasswordRepository, passwordEncoder);
	}

	private SignupRequest validRequest() {
		return new SignupRequest("Youth01", "Abcdefg1!", "Abcdefg1!", "청년길잡이", "User@Example.com");
	}

	private void stubSaveAssigningId(long id) {
		when(memberRepository.saveAndFlush(any(Member.class))).thenAnswer(invocation -> {
			Member member = invocation.getArgument(0);
			ReflectionTestUtils.setField(member, "id", id);
			return member;
		});
	}

	@Test
	void 가입하면_회원과_해시된_비밀번호를_저장한다() {
		stubSaveAssigningId(7L);

		SignupResponse response = memberService.signup(validRequest());

		assertThat(response.memberId()).isEqualTo("7");
		assertThat(response.loginName()).isEqualTo("Youth01");

		ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
		verify(memberRepository).saveAndFlush(memberCaptor.capture());
		assertThat(memberCaptor.getValue().getLoginNameKey()).isEqualTo("youth01");

		ArgumentCaptor<MemberPassword> passwordCaptor = ArgumentCaptor.forClass(MemberPassword.class);
		verify(memberPasswordRepository).saveAndFlush(passwordCaptor.capture());
		MemberPassword saved = passwordCaptor.getValue();
		assertThat(saved.getMemberId()).isEqualTo(7L);
		assertThat(saved.getPasswordHash()).startsWith("{bcrypt}").isNotEqualTo("Abcdefg1!");
		assertThat(passwordEncoder.matches("Abcdefg1!", saved.getPasswordHash())).isTrue();
	}

	@Test
	void 비밀번호_확인이_다르면_저장하지_않는다() {
		SignupRequest request = new SignupRequest("Youth01", "Abcdefg1!", "Abcdefg1?", "청년길잡이", "u@e.com");

		assertThatThrownBy(() -> memberService.signup(request))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
		verify(memberRepository, never()).saveAndFlush(any());
	}

	@Test
	void 대소문자만_다른_아이디가_있으면_중복으로_거부한다() {
		when(memberRepository.existsByLoginNameKey("youth01")).thenReturn(true);

		assertThatThrownBy(() -> memberService.signup(validRequest()))
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.DUPLICATE_LOGIN_NAME);
		verify(memberRepository, never()).saveAndFlush(any());
	}

	@Test
	void 이메일이_중복이면_거부한다() {
		when(memberRepository.existsByEmailKey("user@example.com")).thenReturn(true);

		assertThatThrownBy(() -> memberService.signup(validRequest()))
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.DUPLICATE_EMAIL);
	}

	@Test
	void 동시가입으로_UNIQUE_제약에_걸리면_해당_중복_오류로_바꾼다() {
		SQLIntegrityConstraintViolationException cause = new SQLIntegrityConstraintViolationException(
				"Duplicate entry '청년길잡이' for key 'member.uq_member_2'");
		when(memberRepository.saveAndFlush(any(Member.class)))
				.thenThrow(new DataIntegrityViolationException("could not execute statement", cause));

		assertThatThrownBy(() -> memberService.signup(validRequest()))
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
		verify(memberPasswordRepository, never()).saveAndFlush(any());
	}

	@Test
	void 중복확인은_정규화된_키로_조회한다() {
		when(memberRepository.existsByLoginNameKey("youth01")).thenReturn(false);

		AvailabilityResponse response = memberService.checkAvailability("YOUTH01", null, null);

		assertThat(response.field()).isEqualTo("loginName");
		assertThat(response.available()).isTrue();
	}

	@Test
	void 중복확인은_값을_정확히_하나만_받는다() {
		assertThatThrownBy(() -> memberService.checkAvailability("youth01", "닉네임", null))
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
		assertThatThrownBy(() -> memberService.checkAvailability(null, null, null))
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
	}

	@Test
	void 중복확인에서_형식이_틀리면_조회하지_않고_거부한다() {
		assertThatThrownBy(() -> memberService.checkAvailability("ab", null, null))
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
		verify(memberRepository, never()).existsByLoginNameKey(any());
	}
}
