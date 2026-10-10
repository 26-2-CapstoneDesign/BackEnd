package com.youthguide.api.user.domain;

/**
 * 회원가입 화면의 성별 선택지. member_profile.gender_response에 이름(문자열)으로 저장한다.
 * UNDISCLOSED(비공개)는 "응답하지 않음"을 선택한 것이고, 값이 없는 것(null)과 구분한다.
 */
public enum Gender {
	FEMALE,
	MALE,
	UNDISCLOSED
}
