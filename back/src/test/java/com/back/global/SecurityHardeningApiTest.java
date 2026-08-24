package com.back.global;

import com.back.domain.member.entity.Member;
import com.back.global.testsupport.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("보안 점검에서 나온 결함들")
class SecurityHardeningApiTest extends ApiTestSupport {

	@Test
	@DisplayName("본문을 읽을 수 없으면 400이다. 401이면 front가 세션 만료로 읽어 로그인한 사람을 쫓아낸다")
	void 깨진_본문은_400() throws Exception {
		Member member = joinMember("gureum", "구름");

		mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{broken"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").isNotEmpty());
	}

	@Test
	@DisplayName("경로 값의 형식이 틀리면 400이다")
	void 타입_불일치는_400() throws Exception {
		mockMvc.perform(get("/api/posts/{id}", "abc"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("본문이 너무 길면 DB가 아니라 검증이 막는다. 항목명과 함께 400이 나온다")
	void 긴_본문은_검증에서_400() throws Exception {
		Member member = joinMember("gureum", "구름");

		mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"title", "긴 글",
								"content", "가".repeat(100_001),
								"published", true))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[?(@.field == 'content')]").isNotEmpty());
	}

	@Test
	@DisplayName("10만자까지는 저장된다. DB가 3만자쯤에서 자르지 않는다")
	void 십만자는_저장된다() throws Exception {
		Member member = joinMember("gureum", "구름");
		String content = "가".repeat(100_000);

		String body = mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", "긴 글", "content", content, "published", true))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		long id = objectMapper.readTree(body).path("id").asLong();

		// 잘리지 않고 그대로 돌아와야 한다.
		mockMvc.perform(get("/api/posts/{id}", id))
				.andExpect(jsonPath("$.content").value(content));
	}

	@Test
	@DisplayName("로그인을 반복해서 틀리면 429로 막힌다")
	void 무차별_대입은_막힌다() throws Exception {
		joinMember("gureum", "구름");

		// 앞의 열 번은 평범한 인증 실패다.
		for (int i = 0; i < 10; i++) {
			login("gureum", "wrong" + i).andExpect(status().isUnauthorized());
		}

		// 그다음부터는 비밀번호가 맞든 틀리든 잠긴다.
		login("gureum", "wrong-again").andExpect(status().isTooManyRequests());
		login("gureum", PASSWORD).andExpect(status().isTooManyRequests());
	}

	private org.springframework.test.web.servlet.ResultActions login(String username, String password)
			throws Exception {
		return mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("username", username, "password", password))));
	}
}
