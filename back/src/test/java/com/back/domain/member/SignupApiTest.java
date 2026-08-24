package com.back.domain.member;

import com.back.global.testsupport.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("회원가입 API")
class SignupApiTest extends ApiTestSupport {

	@Test
	@DisplayName("가입하면 201과 공개 정보를 돌려준다. Username은 담기지 않는다")
	void 가입_성공() throws Exception {
		signup("newbie", "password123", "새내기")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nickname").value("새내기"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.createDate").isNotEmpty())
				.andExpect(jsonPath("$.username").doesNotExist())
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	@DisplayName("이미 쓰이는 Username이면 409로 거부하고 username 항목을 짚어준다")
	void username_중복_거부() throws Exception {
		signup("taken", "password123", "먼저온사람").andExpect(status().isCreated());

		signup("taken", "password123", "나중에온사람")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errors[0].field").value("username"))
				.andExpect(jsonPath("$.errors[0].message").isNotEmpty());
	}

	@Test
	@DisplayName("이미 쓰이는 Nickname이면 409로 거부하고 nickname 항목을 짚어준다")
	void nickname_중복_거부() throws Exception {
		signup("first", "password123", "겹치는이름").andExpect(status().isCreated());

		signup("second", "password123", "겹치는이름")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errors[0].field").value("nickname"));
	}

	@Test
	@DisplayName("형식이 틀리면 400과 항목명·메시지의 쌍을 돌려준다")
	void 검증_실패_응답_형태() throws Exception {
		signup("a", "short", "")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").isNotEmpty())
				.andExpect(jsonPath("$.detail").isNotEmpty())
				.andExpect(jsonPath("$.errors.length()").value(4))
				.andExpect(jsonPath("$.errors[?(@.field == 'username')]").isNotEmpty())
				.andExpect(jsonPath("$.errors[?(@.field == 'password')]").isNotEmpty())
				.andExpect(jsonPath("$.errors[?(@.field == 'nickname')]").isNotEmpty());
	}

	private org.springframework.test.web.servlet.ResultActions signup(String username, String password, String nickname)
			throws Exception {
		return mockMvc.perform(post("/api/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("username", username, "password", password, "nickname", nickname))));
	}
}
