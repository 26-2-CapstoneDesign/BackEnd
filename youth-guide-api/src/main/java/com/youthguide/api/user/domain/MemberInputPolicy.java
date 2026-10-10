package com.youthguide.api.user.domain;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 회원 입력 규칙과 고유 비교키(*_key) 정규화를 한 곳에 모은 클래스.
 * 규칙이 바뀌면 이 파일만 고친다.
 *
 * <ul>
 *   <li>비밀번호: 8~20자, 영문 대문자/영문 소문자/숫자/특수문자 중 3종류 이상 (팀 규칙, 2026-10-09 사용자 전달)</li>
 *   <li>아이디·닉네임: [가정] 팀 규칙 미전달. 아래 값은 임시이며 G02 확정 시 수정</li>
 *   <li>비교키 정규화: [가정] Q05 미확정. NFKC + 앞뒤 공백 제거 + 소문자</li>
 * </ul>
 */
public final class MemberInputPolicy {

	public static final int PASSWORD_MIN_LENGTH = 8;
	public static final int PASSWORD_MAX_LENGTH = 20;
	public static final int PASSWORD_MIN_CHARACTER_KINDS = 3;

	/** [가정] 영문·숫자 4~20자 */
	public static final String LOGIN_NAME_REGEX = "^[A-Za-z0-9]{4,20}$";
	public static final String LOGIN_NAME_MESSAGE = "아이디는 영문과 숫자로 4~20자여야 합니다.";

	/** [가정] 한글·영문·숫자 2~20자 */
	public static final String NICKNAME_REGEX = "^[가-힣A-Za-z0-9]{2,20}$";
	public static final String NICKNAME_MESSAGE = "닉네임은 한글, 영문, 숫자로 2~20자여야 합니다.";

	/** DB email 컬럼 varchar(320) */
	public static final int EMAIL_MAX_LENGTH = 320;
	public static final String EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
	public static final String EMAIL_MESSAGE = "이메일 형식이 올바르지 않습니다.";

	public static final String PASSWORD_MESSAGE =
			"비밀번호는 8~20자이며 영문 대문자, 영문 소문자, 숫자, 특수문자 중 3가지 이상을 포함해야 합니다.";

	private static final Pattern LOGIN_NAME_PATTERN = Pattern.compile(LOGIN_NAME_REGEX);
	private static final Pattern NICKNAME_PATTERN = Pattern.compile(NICKNAME_REGEX);
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	private MemberInputPolicy() {
	}

	public static boolean isValidLoginName(String value) {
		return value != null && LOGIN_NAME_PATTERN.matcher(value).matches();
	}

	public static boolean isValidNickname(String value) {
		return value != null && NICKNAME_PATTERN.matcher(value).matches();
	}

	public static boolean isValidEmail(String value) {
		return value != null && value.length() <= EMAIL_MAX_LENGTH && EMAIL_PATTERN.matcher(value).matches();
	}

	/**
	 * 허용 문자: 공백을 제외한 출력 가능한 ASCII(영문, 숫자, 특수문자).
	 * 한글·공백이 섞이면 거부한다.
	 */
	public static boolean isValidPassword(String value) {
		if (value == null || value.length() < PASSWORD_MIN_LENGTH || value.length() > PASSWORD_MAX_LENGTH) {
			return false;
		}
		boolean hasUpper = false;
		boolean hasLower = false;
		boolean hasDigit = false;
		boolean hasSpecial = false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c < '!' || c > '~') {
				return false;
			}
			if (c >= 'A' && c <= 'Z') {
				hasUpper = true;
			} else if (c >= 'a' && c <= 'z') {
				hasLower = true;
			} else if (c >= '0' && c <= '9') {
				hasDigit = true;
			} else {
				hasSpecial = true;
			}
		}
		int kinds = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
		return kinds >= PASSWORD_MIN_CHARACTER_KINDS;
	}

	/** login_name_key: 대소문자만 다른 아이디는 같은 아이디로 본다. */
	public static String loginNameKey(String loginName) {
		return normalize(loginName);
	}

	public static String nicknameKey(String nickname) {
		return normalize(nickname);
	}

	public static String emailKey(String email) {
		return normalize(email);
	}

	private static String normalize(String value) {
		if (value == null) {
			return null;
		}
		return Normalizer.normalize(value, Normalizer.Form.NFKC).strip().toLowerCase(Locale.ROOT);
	}
}
