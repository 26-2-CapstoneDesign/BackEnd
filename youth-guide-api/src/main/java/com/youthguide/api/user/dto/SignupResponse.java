package com.youthguide.api.user.dto;

import com.youthguide.api.user.domain.Member;

/** memberId는 BIGINT지만 문자열로 내려준다 (식별자 설계 ID-01, WBS 01.03). */
public record SignupResponse(String memberId, String loginName, String nickname) {

	public static SignupResponse from(Member member) {
		return new SignupResponse(String.valueOf(member.getId()), member.getLoginName(), member.getNickname());
	}
}
