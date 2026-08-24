package com.back.domain.post;

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

@DisplayName("ViewCount API")
class ViewCountApiTest extends ApiTestSupport {

	@Test
	@DisplayName("상세를 열 때마다 1씩 오르고, 목록에도 그 값이 담긴다")
	void 상세_조회마다_증가한다() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author);

		// 갓 쓴 글은 아직 아무도 열지 않았다.
		mockMvc.perform(get("/api/posts"))
				.andExpect(jsonPath("$.items[0].viewCount").value(0));

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.viewCount").value(1));

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(jsonPath("$.viewCount").value(2));

		// 목록은 ViewCount를 올리지 않는다. 두 번 연 만큼만 남아 있어야 한다.
		mockMvc.perform(get("/api/posts"))
				.andExpect(jsonPath("$.items[0].viewCount").value(2));

		mockMvc.perform(get("/api/posts"))
				.andExpect(jsonPath("$.items[0].viewCount").value(2));
	}

	@Test
	@DisplayName("같은 사람이 반복해서 열어도 계속 오른다. 고유 독자 수가 아니다")
	void 같은_사람의_반복_조회도_센다() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author);

		for (int i = 1; i <= 3; i++) {
			mockMvc.perform(get("/api/posts/{id}", postId)
							.header(HttpHeaders.AUTHORIZATION, bearer(author)))
					.andExpect(jsonPath("$.viewCount").value(i));
		}
	}

	private long writePost(Member author) throws Exception {
		String body = mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(author))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", "조회수 확인용", "content", "본문"))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return objectMapper.readTree(body).path("id").asLong();
	}
}
