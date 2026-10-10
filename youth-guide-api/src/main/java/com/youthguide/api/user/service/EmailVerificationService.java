package com.youthguide.api.user.service;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.user.domain.MemberInputPolicy;
import com.youthguide.api.user.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * 회원가입 이메일 인증 (REG-05, REG-06).
 * 인증번호와 인증 완료 상태를 DB가 아니라 요청한 브라우저의 서버 세션에 저장한다.
 * 그래서 인증한 브라우저(같은 세션)에서만 가입할 수 있고, 서버가 재시작되면 진행 중인 인증은 사라진다.
 */
@Service
public class EmailVerificationService {

	static final String PENDING_ATTRIBUTE = EmailVerificationService.class.getName() + ".PENDING";
	static final String VERIFIED_ATTRIBUTE = EmailVerificationService.class.getName() + ".VERIFIED";

	static final Duration CODE_VALIDITY = Duration.ofMinutes(5);
	static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);
	static final Duration VERIFIED_VALIDITY = Duration.ofMinutes(30);
	static final int MAX_ATTEMPTS = 5;

	private final MemberRepository memberRepository;
	private final VerificationMailSender mailSender;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();

	@Autowired
	public EmailVerificationService(MemberRepository memberRepository, VerificationMailSender mailSender) {
		this(memberRepository, mailSender, Clock.systemUTC());
	}

	EmailVerificationService(MemberRepository memberRepository, VerificationMailSender mailSender, Clock clock) {
		this.memberRepository = memberRepository;
		this.mailSender = mailSender;
		this.clock = clock;
	}

	/**
	 * 인증번호(6자리 숫자)를 만들어 메일로 보낸다.
	 * 이미 가입된 이메일이면 보내지 않는다. 같은 이메일로 1분 안에 다시 요청하면 거부한다.
	 * 새로 요청하면 이전 인증번호와 인증 완료 상태는 무효가 된다.
	 */
	public void sendCode(String email, HttpSession session) {
		if (!MemberInputPolicy.isValidEmail(email)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		String emailKey = MemberInputPolicy.emailKey(email);
		if (memberRepository.existsByEmailKey(emailKey)) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}

		Instant now = clock.instant();
		PendingVerification previous = (PendingVerification) session.getAttribute(PENDING_ATTRIBUTE);
		if (previous != null && previous.emailKey().equals(emailKey)
				&& now.isBefore(previous.sentAt().plus(RESEND_COOLDOWN))) {
			throw new BusinessException(ErrorCode.EMAIL_RESEND_TOO_SOON);
		}

		String code = String.format("%06d", random.nextInt(1_000_000));
		mailSender.sendVerificationCode(email.strip(), code);

		session.setAttribute(PENDING_ATTRIBUTE,
				new PendingVerification(emailKey, hash(code), now, now.plus(CODE_VALIDITY), 0));
		session.removeAttribute(VERIFIED_ATTRIBUTE);
	}

	/**
	 * 사용자가 입력한 인증번호를 확인한다. 맞으면 이 세션에 "이 이메일은 인증됨"을 30분간 기록한다.
	 * 5번 틀리면 그 인증번호는 버리고 다시 받게 한다.
	 */
	public void confirmCode(String email, String code, HttpSession session) {
		String emailKey = MemberInputPolicy.emailKey(email);
		PendingVerification pending = (PendingVerification) session.getAttribute(PENDING_ATTRIBUTE);
		Instant now = clock.instant();

		if (pending == null || !pending.emailKey().equals(emailKey) || !now.isBefore(pending.expiresAt())) {
			session.removeAttribute(PENDING_ATTRIBUTE);
			throw new BusinessException(ErrorCode.EMAIL_CODE_EXPIRED);
		}

		if (!MessageDigest.isEqual(pending.codeHash(), hash(code == null ? "" : code.strip()))) {
			int attempts = pending.failedAttempts() + 1;
			if (attempts >= MAX_ATTEMPTS) {
				session.removeAttribute(PENDING_ATTRIBUTE);
				throw new BusinessException(ErrorCode.EMAIL_CODE_ATTEMPTS_EXCEEDED);
			}
			session.setAttribute(PENDING_ATTRIBUTE, pending.withFailedAttempts(attempts));
			throw new BusinessException(ErrorCode.EMAIL_CODE_MISMATCH);
		}

		session.removeAttribute(PENDING_ATTRIBUTE);
		session.setAttribute(VERIFIED_ATTRIBUTE, new VerifiedEmail(emailKey, now.plus(VERIFIED_VALIDITY)));
	}

	/** 이 세션에서 인증이 끝난 이메일의 비교키. 없거나 만료됐으면 null. */
	public String findVerifiedEmailKey(HttpSession session) {
		VerifiedEmail verified = (VerifiedEmail) session.getAttribute(VERIFIED_ATTRIBUTE);
		if (verified == null || !clock.instant().isBefore(verified.expiresAt())) {
			return null;
		}
		return verified.emailKey();
	}

	/** 가입이 끝나면 인증 기록을 지워서 같은 인증으로 두 번 가입하지 못하게 한다. */
	public void clear(HttpSession session) {
		session.removeAttribute(PENDING_ATTRIBUTE);
		session.removeAttribute(VERIFIED_ATTRIBUTE);
	}

	/** 세션에는 인증번호 원문 대신 해시만 둔다. */
	private static byte[] hash(String code) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8));
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	record PendingVerification(String emailKey, byte[] codeHash, Instant sentAt, Instant expiresAt,
			int failedAttempts) implements Serializable {

		PendingVerification withFailedAttempts(int attempts) {
			return new PendingVerification(emailKey, codeHash, sentAt, expiresAt, attempts);
		}
	}

	record VerifiedEmail(String emailKey, Instant expiresAt) implements Serializable {
	}
}
