package com.back.domain.post;

import com.back.domain.member.entity.Member;
import com.back.global.testsupport.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 추천 API가 200을 냈다는 것만으로 통과시키지 않는다.
 * 모든 검증은 추천 이후의 <b>재조회</b> 응답으로 한다.
 */
@DisplayName("PostLike API")
class PostLikeApiTest extends ApiTestSupport {

	@Test
	@DisplayName("추천하면 재조회했을 때 추천수가 오르고 내가 추천함으로 보인다")
	void 추천() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");
		long postId = writePost(author);

		like(reader, postId).andExpect(status().isNoContent());

		detailAs(reader, postId)
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.likedByMe").value(true));
	}

	@Test
	@DisplayName("추천하지 않은 사람과 비로그인 요청에는 내가 추천함이 거짓이다")
	void 남의_추천은_내_추천이_아니다() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");
		long postId = writePost(author);

		like(reader, postId).andExpect(status().isNoContent());

		detailAs(author, postId)
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.likedByMe").value(false));

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.likedByMe").value(false));
	}

	@Test
	@DisplayName("이미 추천한 글을 다시 추천해도 기록이 쌓이지 않는다")
	void 중복_추천이_쌓이지_않는다() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");
		long postId = writePost(author);

		like(reader, postId).andExpect(status().isNoContent());
		like(reader, postId).andExpect(status().isNoContent());
		like(reader, postId).andExpect(status().isNoContent());

		detailAs(reader, postId)
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.likedByMe").value(true));
	}

	@Test
	@DisplayName("취소하면 재조회했을 때 추천수가 줄고 내가 추천함이 거짓이 된다")
	void 취소() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");
		Member other = joinMember("other", "다른사람");
		long postId = writePost(author);

		like(reader, postId).andExpect(status().isNoContent());
		like(other, postId).andExpect(status().isNoContent());

		detailAs(reader, postId).andExpect(jsonPath("$.likeCount").value(2));

		mockMvc.perform(delete("/api/posts/{id}/like", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(reader)))
				.andExpect(status().isNoContent());

		detailAs(reader, postId)
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.likedByMe").value(false));
	}

	@Test
	@DisplayName("목록 응답에도 추천수와 내 추천 여부가 담긴다")
	void 목록에도_담긴다() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");
		long postId = writePost(author);

		like(reader, postId).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/posts").header(HttpHeaders.AUTHORIZATION, bearer(reader)))
				.andExpect(jsonPath("$.items[0].likeCount").value(1))
				.andExpect(jsonPath("$.items[0].likedByMe").value(true));

		mockMvc.perform(get("/api/posts"))
				.andExpect(jsonPath("$.items[0].likeCount").value(1))
				.andExpect(jsonPath("$.items[0].likedByMe").value(false));
	}

	@Test
	@DisplayName("토큰 없이 추천하려 하면 401이다")
	void 추천은_인증을_요구한다() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author);

		mockMvc.perform(put("/api/posts/{id}/like", postId))
				.andExpect(status().isUnauthorized());
	}

	private long writePost(Member author) throws Exception {
		String body = mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(author))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", "추천받을 글", "content", "본문", "published", true))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}

	private org.springframework.test.web.servlet.ResultActions like(Member actor, long postId) throws Exception {
		return mockMvc.perform(put("/api/posts/{id}/like", postId)
				.header(HttpHeaders.AUTHORIZATION, bearer(actor)));
	}

	private org.springframework.test.web.servlet.ResultActions detailAs(Member viewer, long postId) throws Exception {
		return mockMvc.perform(get("/api/posts/{id}", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(viewer)))
				.andExpect(status().isOk());
	}
}
