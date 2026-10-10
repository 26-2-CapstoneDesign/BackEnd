package com.youthguide.api.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * member 테이블 (all_in_one.sql 기준).
 * created_at / updated_at은 DB 기본값(CURRENT_TIMESTAMP(6), UTC)이 채우므로 매핑하지 않는다.
 */
@Entity
@Table(name = "member")
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;

	@Column(name = "login_name", nullable = false, length = 128)
	private String loginName;

	@Column(name = "email", nullable = false, length = 320)
	private String email;

	@Column(name = "nickname", nullable = false, length = 128)
	private String nickname;

	@Column(name = "login_name_key", nullable = false, length = 255)
	private String loginNameKey;

	@Column(name = "nickname_key", nullable = false, length = 255)
	private String nicknameKey;

	@Column(name = "email_key", nullable = false, length = 320)
	private String emailKey;

	protected Member() {
	}

	private Member(String loginName, String email, String nickname) {
		this.loginName = loginName;
		this.email = email;
		this.nickname = nickname;
		this.loginNameKey = MemberInputPolicy.loginNameKey(loginName);
		this.emailKey = MemberInputPolicy.emailKey(email);
		this.nicknameKey = MemberInputPolicy.nicknameKey(nickname);
	}

	/** 비교키는 항상 원문에서 계산한다. 따로 넘겨받지 않아 원문과 키가 어긋날 수 없다. */
	public static Member create(String loginName, String email, String nickname) {
		return new Member(loginName.strip(), email.strip(), nickname.strip());
	}

	public Long getId() {
		return id;
	}

	public String getLoginName() {
		return loginName;
	}

	public String getEmail() {
		return email;
	}

	public String getNickname() {
		return nickname;
	}

	public String getLoginNameKey() {
		return loginNameKey;
	}
}
