package com.back.domain.member.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인할 수 있는 사람. 수강생인지 저자인지 같은 신분이 아니라 인증 주체를 뜻한다.
 * 이메일 필드는 없다.
 */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

	/** 로그인 식별자. 유일하며 화면에 노출하지 않는다. */
	@Column(unique = true, nullable = false, length = 30)
	private String username;

	/** 해시된 비밀번호. 평문이나 가역 암호화를 쓰지 않는다. */
	@Column(nullable = false)
	private String password;

	/** 화면에서 사람을 식별하는 표시용 이름. 유일하다. Member의 공개 식별자는 이것뿐이다. */
	@Column(unique = true, nullable = false, length = 30)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Role role;

	private Member(String username, String password, String nickname, Role role) {
		this.username = username;
		this.password = password;
		this.nickname = nickname;
		this.role = role;
	}

	/** 가입. 비밀번호는 이미 해시된 값을 받는다. */
	public static Member join(String username, String encodedPassword, String nickname) {
		return new Member(username, encodedPassword, nickname, Role.USER);
	}

	/** 초기 데이터용. 가입 경로로는 ADMIN이 될 수 없다. */
	public static Member joinAsAdmin(String username, String encodedPassword, String nickname) {
		return new Member(username, encodedPassword, nickname, Role.ADMIN);
	}

	public boolean isAdmin() {
		return role.isAdmin();
	}
}
