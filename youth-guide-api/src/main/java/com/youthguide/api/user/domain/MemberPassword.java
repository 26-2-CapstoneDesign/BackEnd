package com.youthguide.api.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * member_password 테이블. member와 1:1, PK = FK(member_id).
 * password_hash에는 "{bcrypt}..."처럼 알고리즘 접두사가 붙은 값을 저장한다 (DelegatingPasswordEncoder).
 * changed_at은 DB 기본값이 채운다.
 */
@Entity
@Table(name = "member_password")
public class MemberPassword {

	@Id
	@Column(name = "member_id")
	private Long memberId;

	@Column(name = "password_hash", nullable = false, length = 512)
	private String passwordHash;

	protected MemberPassword() {
	}

	public MemberPassword(Long memberId, String passwordHash) {
		this.memberId = memberId;
		this.passwordHash = passwordHash;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}
}
