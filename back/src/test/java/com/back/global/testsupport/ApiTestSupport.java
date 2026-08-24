package com.back.global.testsupport;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * seam은 하나다: HTTP API.
 * 전체 컨텍스트를 띄우고 테스트 프로파일(인메모리 DB) 위에서 MockMvc로 요청을 보낸다.
 * 각 테스트는 트랜잭션이 롤백되므로 서로 간섭하지 않는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ApiTestSupport {

	/** 테스트 계정의 비밀번호. 검증 규칙을 통과하는 아무 값이면 된다. */
	protected static final String PASSWORD = "password123";

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected MemberRepository memberRepository;

	@Autowired
	protected PasswordEncoder passwordEncoder;

	@Autowired
	protected JwtProvider jwtProvider;

	protected String json(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new IllegalStateException("테스트 요청 본문을 직렬화하지 못했습니다.", e);
		}
	}

	protected Member joinMember(String username, String nickname) {
		return memberRepository.save(Member.join(username, passwordEncoder.encode(PASSWORD), nickname));
	}

	protected Member joinAdmin(String username, String nickname) {
		return memberRepository.save(Member.joinAsAdmin(username, passwordEncoder.encode(PASSWORD), nickname));
	}

	/**
	 * 인증이 필요한 요청에 실을 Authorization 헤더 값.
	 * 각 테스트가 로그인 절차를 반복하지 않게 한다.
	 */
	protected String bearer(Member member) {
		return "Bearer " + jwtProvider.createAccessToken(member);
	}
}
