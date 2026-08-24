package com.back.global.security;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * 인증된 요청자. 토큰에 담긴 만큼만 들고 있으므로 요청마다 DB를 조회하지 않는다.
 */
public record MemberPrincipal(Long id, String nickname, Role role) {

	public static MemberPrincipal from(Member member) {
		return new MemberPrincipal(member.getId(), member.getNickname(), member.getRole());
	}

	public Collection<? extends GrantedAuthority> authorities() {
		return List.of(new SimpleGrantedAuthority(role.authority()));
	}

	public boolean isAdmin() {
		return role.isAdmin();
	}
}
