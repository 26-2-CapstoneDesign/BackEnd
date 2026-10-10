package com.youthguide.api.user.service;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.user.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

	private static final String EMAIL = "User@Example.com";

	@Mock
	MemberRepository memberRepository;

	@Mock
	VerificationMailSender mailSender;

	MutableClock clock;
	EmailVerificationService service;
	MockHttpSession session;

	@BeforeEach
	void setUp() {
		clock = new MutableClock(Instant.parse("2026-10-10T00:00:00Z"));
		service = new EmailVerificationService(memberRepository, mailSender, clock);
		session = new MockHttpSession();
	}

	/** 메일로 보낸 인증번호를 가로챈다 */
	private String sendAndCaptureCode() {
		service.sendCode(EMAIL, session);
		ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
		verify(mailSender).sendVerificationCode(eq("User@Example.com"), codeCaptor.capture());
		return codeCaptor.getValue();
	}

	private static ErrorCode errorCodeOf(Throwable e) {
		return ((BusinessException) e).getErrorCode();
	}

	@Test
	void 인증번호를_맞히면_세션에_인증된_이메일키가_남는다() {
		String code = sendAndCaptureCode();
		assertThat(code).matches("\\d{6}");

		service.confirmCode("user@example.com", code, session);

		assertThat(service.findVerifiedEmailKey(session)).isEqualTo("user@example.com");
	}

	@Test
	void 이미_가입된_이메일은_인증번호를_보내지_않는다() {
		when(memberRepository.existsByEmailKey("user@example.com")).thenReturn(true);

		assertThatThrownBy(() -> service.sendCode(EMAIL, session))
				.extracting(EmailVerificationServiceTest::errorCodeOf)
				.isEqualTo(ErrorCode.DUPLICATE_EMAIL);
		verify(mailSender, never()).sendVerificationCode(anyString(), anyString());
	}

	@Test
	void 일분_안에_같은_이메일로_다시_요청하면_거부하고_일분_뒤엔_허용한다() {
		service.sendCode(EMAIL, session);

		assertThatThrownBy(() -> service.sendCode(EMAIL, session))
				.extracting(EmailVerificationServiceTest::errorCodeOf)
				.isEqualTo(ErrorCode.EMAIL_RESEND_TOO_SOON);

		clock.advance(Duration.ofSeconds(61));
		service.sendCode(EMAIL, session);
		verify(mailSender, times(2)).sendVerificationCode(anyString(), anyString());
	}

	@Test
	void 틀린_인증번호는_거부하고_인증되지_않는다() {
		String code = sendAndCaptureCode();
		String wrong = code.equals("000000") ? "111111" : "000000";

		assertThatThrownBy(() -> service.confirmCode(EMAIL, wrong, session))
				.extracting(EmailVerificationServiceTest::errorCodeOf)
				.isEqualTo(ErrorCode.EMAIL_CODE_MISMATCH);
		assertThat(service.findVerifiedEmailKey(session)).isNull();
	}

	@Test
	void 다섯번_틀리면_인증번호가_폐기된다() {
		String code = sendAndCaptureCode();
		String wrong = code.equals("000000") ? "111111" : "000000";
		for (int i = 0; i < 4; i++) {
			assertThatThrownBy(() -> service.confirmCode(EMAIL, wrong, session))
					.extracting(EmailVerificationServiceTest::errorCodeOf)
					.isEqualTo(ErrorCode.EMAIL_CODE_MISMATCH);
		}
		assertThatThrownBy(() -> service.confirmCode(EMAIL, wrong, session))
				.extracting(EmailVerificationServiceTest::errorCodeOf)
				.isEqualTo(ErrorCode.EMAIL_CODE_ATTEMPTS_EXCEEDED);

		// 폐기 후에는 맞는 번호를 넣어도 다시 받아야 한다
		assertThatThrownBy(() -> service.confirmCode(EMAIL, code, session))
				.extracting(EmailVerificationServiceTest::errorCodeOf)
				.isEqualTo(ErrorCode.EMAIL_CODE_EXPIRED);
	}

	@Test
	void 오분이_지나면_인증번호가_만료된다() {
		String code = sendAndCaptureCode();
		clock.advance(Duration.ofMinutes(5));

		assertThatThrownBy(() -> service.confirmCode(EMAIL, code, session))
				.extracting(EmailVerificationServiceTest::errorCodeOf)
				.isEqualTo(ErrorCode.EMAIL_CODE_EXPIRED);
	}

	@Test
	void 다른_이메일로_확인하면_거부한다() {
		String code = sendAndCaptureCode();

		assertThatThrownBy(() -> service.confirmCode("other@example.com", code, session))
				.extracting(EmailVerificationServiceTest::errorCodeOf)
				.isEqualTo(ErrorCode.EMAIL_CODE_EXPIRED);
	}

	@Test
	void 인증_완료는_삼십분_뒤_만료되고_clear하면_지워진다() {
		String code = sendAndCaptureCode();
		service.confirmCode(EMAIL, code, session);

		clock.advance(Duration.ofMinutes(29));
		assertThat(service.findVerifiedEmailKey(session)).isEqualTo("user@example.com");
		clock.advance(Duration.ofMinutes(1));
		assertThat(service.findVerifiedEmailKey(session)).isNull();

		service.clear(session);
		assertThat(session.getAttribute(EmailVerificationService.VERIFIED_ATTRIBUTE)).isNull();
	}

	@Test
	void 형식이_틀린_이메일은_거부한다() {
		assertThatThrownBy(() -> service.sendCode("not-an-email", session))
				.extracting(EmailVerificationServiceTest::errorCodeOf)
				.isEqualTo(ErrorCode.INVALID_INPUT);
	}

	@Test
	void 로그에는_이메일_앞_두글자만_남긴다() {
		assertThat(VerificationMailSender.maskEmail("youth01@example.com")).isEqualTo("yo***@example.com");
		assertThat(VerificationMailSender.maskEmail("a@b.com")).isEqualTo("a***@b.com");
	}

	/** 테스트에서 시간을 앞으로 돌리기 위한 시계 */
	static final class MutableClock extends Clock {
		private Instant now;

		MutableClock(Instant now) {
			this.now = now;
		}

		void advance(Duration duration) {
			now = now.plus(duration);
		}

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(java.time.ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}
}
