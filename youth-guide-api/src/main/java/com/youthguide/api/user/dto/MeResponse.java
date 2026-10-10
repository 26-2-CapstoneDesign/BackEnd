package com.youthguide.api.user.dto;

import com.youthguide.api.user.domain.Member;

/** MEM-02 GET /api/v1/me, AUTH-01 로그인 성공 응답. */
public record MeResponse(String memberId, String loginName, String nickname, String email) {

	public static MeResponse from(Member member) {
		return new MeResponse(String.valueOf(member.getId()), member.getLoginName(), member.getNickname(),
				member.getEmail());
	}
}
