package com.back.domain.member.dto;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;

import java.time.LocalDateTime;

/**
 * Member의 공개 표현. Username은 화면에 노출하지 않으므로 담지 않는다.
 */
public record MemberDto(
		Long id,
		String nickname,
		Role role,
		LocalDateTime createDate
) {

	public static MemberDto from(Member member) {
		return new MemberDto(member.getId(), member.getNickname(), member.getRole(), member.getCreateDate());
	}
}
