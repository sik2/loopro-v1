package com.back.domain.member.entity;

/**
 * Member가 가지는 시스템 권한 등급. 이 둘뿐이다.
 */
public enum Role {
	USER,
	ADMIN;

	public boolean isAdmin() {
		return this == ADMIN;
	}

	/** Spring Security의 권한 문자열 규약(ROLE_ 접두사)에 맞춘 이름. */
	public String authority() {
		return "ROLE_" + name();
	}
}
