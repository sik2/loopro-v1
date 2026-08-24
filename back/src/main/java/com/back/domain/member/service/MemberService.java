package com.back.domain.member.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public Member join(String username, String rawPassword, String nickname) {
		if (memberRepository.existsByUsername(username)) {
			throw ServiceException.conflictOnField("username", "이미 사용 중인 Username입니다.");
		}
		if (memberRepository.existsByNickname(nickname)) {
			throw ServiceException.conflictOnField("nickname", "이미 사용 중인 Nickname입니다.");
		}

		return memberRepository.save(Member.join(username, passwordEncoder.encode(rawPassword), nickname));
	}

	@Transactional(readOnly = true)
	public Member findById(long id) {
		return memberRepository.findById(id)
				.orElseThrow(() -> ServiceException.notFound("존재하지 않는 Member입니다."));
	}
}
