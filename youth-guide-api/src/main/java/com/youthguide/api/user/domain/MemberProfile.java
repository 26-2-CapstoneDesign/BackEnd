package com.youthguide.api.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * member_profile 테이블 (all_in_one.sql 기준). member와 1:1, PK = FK(member_id).
 * updated_at은 DB 기본값이 채우므로 매핑하지 않는다.
 */
@Entity
@Table(name = "member_profile")
public class MemberProfile {

	@Id
	@Column(name = "member_id")
	private Long memberId;

	/** 실명 */
	@Column(name = "display_name", length = 128)
	private String name;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender_response", length = 64)
	private Gender gender;

	protected MemberProfile() {
	}

	public MemberProfile(Long memberId, String name, LocalDate birthDate, Gender gender) {
		this.memberId = memberId;
		this.name = name.strip();
		this.birthDate = birthDate;
		this.gender = gender;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getName() {
		return name;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public Gender getGender() {
		return gender;
	}
}
