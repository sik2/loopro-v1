package com.back.domain.member;

import com.back.domain.member.entity.Member;
import com.back.global.security.jwt.JwtProperties;
import com.back.global.testsupport.ApiTestSupport;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("로그인과 인증 API")
class AuthApiTest extends ApiTestSupport {

	@Autowired
	private JwtProperties jwtProperties;

	@Test
	@DisplayName("올바른 Username과 비밀번호면 access token을 낸다")
	void 로그인_성공() throws Exception {
		joinMember("gureum", "구름");

		login("gureum", PASSWORD)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.member.nickname").value("구름"))
				.andExpect(jsonPath("$.member.role").value("USER"));
	}

	@Test
	@DisplayName("비밀번호가 틀리면 401이고, 없는 Username과 답이 구별되지 않는다")
	void 잘못된_비밀번호_거부() throws Exception {
		joinMember("gureum", "구름");

		String wrongPassword = extractDetail(login("gureum", "wrong-password")
				.andExpect(status().isUnauthorized()));

		String unknownUsername = extractDetail(login("nobody", PASSWORD)
				.andExpect(status().isUnauthorized()));

		org.assertj.core.api.Assertions.assertThat(wrongPassword).isEqualTo(unknownUsername);
	}

	@Test
	@DisplayName("토큰을 실으면 내 Nickname·Role·가입일을 돌려준다")
	void 내_정보_조회() throws Exception {
		Member member = joinMember("gureum", "구름");

		mockMvc.perform(get("/api/members/me").header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nickname").value("구름"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.createDate").isNotEmpty())
				.andExpect(jsonPath("$.username").doesNotExist());
	}

	@Test
	@DisplayName("토큰 없이 내 정보를 요청하면 401이다")
	void 토큰_없는_요청_거부() throws Exception {
		mockMvc.perform(get("/api/members/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").isNotEmpty());
	}

	@Test
	@DisplayName("만료된 토큰은 거부된다")
	void 만료된_토큰_거부() throws Exception {
		Member member = joinMember("gureum", "구름");

		mockMvc.perform(get("/api/members/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredTokenFor(member)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("다른 키로 서명된 토큰은 거부된다")
	void 서명이_다른_토큰_거부() throws Exception {
		mockMvc.perform(get("/api/members/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + foreignToken()))
				.andExpect(status().isUnauthorized());
	}

	private ResultActions login(String username, String password) throws Exception {
		return mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("username", username, "password", password))));
	}

	private String extractDetail(ResultActions result) throws Exception {
		return objectMapper.readTree(result.andReturn().getResponse().getContentAsString())
				.path("detail").asString();
	}

	private String expiredTokenFor(Member member) {
		Instant expiredAt = Instant.now().minusSeconds(60);
		return Jwts.builder()
				.subject(String.valueOf(member.getId()))
				.claim("nickname", member.getNickname())
				.claim("role", member.getRole().name())
				.issuedAt(Date.from(expiredAt.minusSeconds(60)))
				.expiration(Date.from(expiredAt))
				.signWith(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)))
				.compact();
	}

	private String foreignToken() {
		return Jwts.builder()
				.subject("1")
				.claim("nickname", "가짜")
				.claim("role", "ADMIN")
				.expiration(Date.from(Instant.now().plusSeconds(3600)))
				.signWith(Keys.hmacShaKeyFor("some-other-signing-key-that-is-long-enough".getBytes(StandardCharsets.UTF_8)))
				.compact();
	}
}
