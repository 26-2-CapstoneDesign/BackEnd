package com.youthguide.api.user.service;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import com.youthguide.api.user.domain.Member;
import com.youthguide.api.user.domain.MemberInputPolicy;
import com.youthguide.api.user.domain.MemberPassword;
import com.youthguide.api.user.domain.MemberProfile;
import com.youthguide.api.user.dto.AvailabilityResponse;
import com.youthguide.api.user.dto.MeResponse;
import com.youthguide.api.user.dto.SignupRequest;
import com.youthguide.api.user.dto.SignupResponse;
import com.youthguide.api.user.repository.MemberPasswordRepository;
import com.youthguide.api.user.repository.MemberProfileRepository;
import com.youthguide.api.user.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * 회원가입·중복확인·내 정보 조회. 인증 방식(세션/토큰)과 무관하게 동작한다.
 */
@Service
public class MemberService {

	// all_in_one.sql의 UNIQUE 제약 이름
	private static final String UQ_LOGIN_NAME = "uq_member_1";
	private static final String UQ_NICKNAME = "uq_member_2";
	private static final String UQ_EMAIL = "uq_member_3";

	private final MemberRepository memberRepository;
	private final MemberPasswordRepository memberPasswordRepository;
	private final MemberProfileRepository memberProfileRepository;
	private final PasswordEncoder passwordEncoder;

	public MemberService(MemberRepository memberRepository,
			MemberPasswordRepository memberPasswordRepository,
			MemberProfileRepository memberProfileRepository,
			PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.memberPasswordRepository = memberPasswordRepository;
		this.memberProfileRepository = memberProfileRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * member + member_password + member_profile을 한 트랜잭션으로 저장한다.
	 * 어느 하나라도 실패하면 전체 롤백.
	 * 사전 중복 검사를 통과해도 동시 가입은 DB UNIQUE 제약으로 한 번 더 막는다 (WBS 06.02).
	 *
	 * @param verifiedEmailKey 이 요청의 세션에서 인증을 마친 이메일의 비교키 (없으면 null)
	 */
	@Transactional
	public SignupResponse signup(SignupRequest request, String verifiedEmailKey) {
		if (!request.password().equals(request.passwordConfirm())) {
			throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
		}
		if (verifiedEmailKey == null || !verifiedEmailKey.equals(MemberInputPolicy.emailKey(request.email()))) {
			throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
		}

		Member member = Member.create(request.loginName(), request.email(), request.nickname());
		if (memberRepository.existsByLoginNameKey(MemberInputPolicy.loginNameKey(request.loginName()))) {
			throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_NAME);
		}
		if (memberRepository.existsByNicknameKey(MemberInputPolicy.nicknameKey(request.nickname()))) {
			throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
		}
		if (memberRepository.existsByEmailKey(MemberInputPolicy.emailKey(request.email()))) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}

		try {
			Member saved = memberRepository.saveAndFlush(member);
			Long memberId = saved.getId();
			memberPasswordRepository.saveAndFlush(
					new MemberPassword(memberId, passwordEncoder.encode(request.password())));
			memberProfileRepository.saveAndFlush(
					new MemberProfile(memberId, request.name(), request.birthDate(), request.gender()));
			return SignupResponse.from(saved);
		} catch (DataIntegrityViolationException e) {
			throw toDuplicateException(e);
		}
	}

	/**
	 * GAP-03 사전 중복확인. 세 값 중 정확히 하나만 받는다.
	 * 형식이 틀린 값은 중복 여부를 보지 않고 INVALID_INPUT으로 응답한다.
	 */
	@Transactional(readOnly = true)
	public AvailabilityResponse checkAvailability(String loginName, String nickname, String email) {
		int given = (loginName != null ? 1 : 0) + (nickname != null ? 1 : 0) + (email != null ? 1 : 0);
		if (given != 1) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}

		if (loginName != null) {
			requireValid(MemberInputPolicy.isValidLoginName(loginName));
			boolean exists = memberRepository.existsByLoginNameKey(MemberInputPolicy.loginNameKey(loginName));
			return new AvailabilityResponse("loginName", !exists);
		}
		if (nickname != null) {
			requireValid(MemberInputPolicy.isValidNickname(nickname));
			boolean exists = memberRepository.existsByNicknameKey(MemberInputPolicy.nicknameKey(nickname));
			return new AvailabilityResponse("nickname", !exists);
		}
		requireValid(MemberInputPolicy.isValidEmail(email));
		boolean exists = memberRepository.existsByEmailKey(MemberInputPolicy.emailKey(email));
		return new AvailabilityResponse("email", !exists);
	}

	/** 세션은 있는데 회원이 사라진 경우(예: DB 초기화)도 로그인이 필요한 상태로 본다. */
	@Transactional(readOnly = true)
	public MeResponse getMe(Long memberId) {
		return memberRepository.findById(memberId)
				.map(MeResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
	}

	private void requireValid(boolean valid) {
		if (!valid) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	/** 동시 가입으로 UNIQUE 제약에 걸린 경우, 제약 이름으로 어떤 값이 겹쳤는지 판단한다. */
	private BusinessException toDuplicateException(DataIntegrityViolationException e) {
		String detail = String.valueOf(e.getMostSpecificCause().getMessage()).toLowerCase(Locale.ROOT);
		if (detail.contains(UQ_LOGIN_NAME)) {
			return new BusinessException(ErrorCode.DUPLICATE_LOGIN_NAME, e);
		}
		if (detail.contains(UQ_NICKNAME)) {
			return new BusinessException(ErrorCode.DUPLICATE_NICKNAME, e);
		}
		if (detail.contains(UQ_EMAIL)) {
			return new BusinessException(ErrorCode.DUPLICATE_EMAIL, e);
		}
		throw e;
	}
}
