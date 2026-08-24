package com.back.domain.member.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	/**
	 * Username이 없든 비밀번호가 틀리든 같은 답을 준다.
	 * 응답만 보고 "그 Username은 존재한다"를 알아낼 수 없어야 한다.
	 *
	 * <p>이 메시지는 특정 입력 칸에 붙지 않아 화면이 이름을 대신 정해줄 수 없다.
	 * 그래서 여기만 예외로 화면 표기(`아이디`)를 직접 쓴다.
	 */
	private static final String LOGIN_FAILED = "아이디 또는 비밀번호가 올바르지 않습니다.";

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;

	@Transactional(readOnly = true)
	public Login login(String username, String rawPassword) {
		Member member = memberRepository.findByUsername(username)
				.orElseThrow(() -> ServiceException.unauthorized(LOGIN_FAILED));

		if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
			throw ServiceException.unauthorized(LOGIN_FAILED);
		}

		return new Login(jwtProvider.createAccessToken(member), member);
	}

	public record Login(String accessToken, Member member) {
	}
}
