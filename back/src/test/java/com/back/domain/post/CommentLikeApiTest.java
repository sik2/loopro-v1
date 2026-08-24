package com.back.domain.post;

import com.back.domain.member.entity.Member;
import com.back.global.testsupport.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** PostLike와 마찬가지로, 모든 검증은 추천 이후의 재조회 응답으로 한다. */
@DisplayName("CommentLike API")
class CommentLikeApiTest extends ApiTestSupport {

	@Test
	@DisplayName("댓글을 추천하면 재조회했을 때 추천수가 오르고 내가 추천함으로 보인다")
	void 추천() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");
		long postId = writePost(author);
		long commentId = writeComment(author, postId);

		like(reader, commentId).andExpect(status().isNoContent());

		commentsAs(reader, postId)
				.andExpect(jsonPath("$[0].likeCount").value(1))
				.andExpect(jsonPath("$[0].likedByMe").value(true));

		// 추천하지 않은 사람에게는 거짓이다.
		commentsAs(author, postId)
				.andExpect(jsonPath("$[0].likeCount").value(1))
				.andExpect(jsonPath("$[0].likedByMe").value(false));
	}

	@Test
	@DisplayName("이미 추천한 댓글을 다시 추천해도 기록이 쌓이지 않는다")
	void 중복_추천이_쌓이지_않는다() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");
		long postId = writePost(author);
		long commentId = writeComment(author, postId);

		like(reader, commentId).andExpect(status().isNoContent());
		like(reader, commentId).andExpect(status().isNoContent());

		commentsAs(reader, postId).andExpect(jsonPath("$[0].likeCount").value(1));
	}

	@Test
	@DisplayName("취소하면 재조회했을 때 추천수가 줄고 내가 추천함이 거짓이 된다")
	void 취소() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");
		long postId = writePost(author);
		long commentId = writeComment(author, postId);

		like(reader, commentId).andExpect(status().isNoContent());
		commentsAs(reader, postId).andExpect(jsonPath("$[0].likeCount").value(1));

		mockMvc.perform(delete("/api/comments/{id}/like", commentId)
						.header(HttpHeaders.AUTHORIZATION, bearer(reader)))
				.andExpect(status().isNoContent());

		commentsAs(reader, postId)
				.andExpect(jsonPath("$[0].likeCount").value(0))
				.andExpect(jsonPath("$[0].likedByMe").value(false));
	}

	@Test
	@DisplayName("토큰 없이 댓글을 추천하려 하면 401이다")
	void 추천은_인증을_요구한다() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author);
		long commentId = writeComment(author, postId);

		mockMvc.perform(put("/api/comments/{id}/like", commentId))
				.andExpect(status().isUnauthorized());
	}

	private long writePost(Member author) throws Exception {
		String body = mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(author))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", "글", "content", "본문"))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}

	private long writeComment(Member author, long postId) throws Exception {
		String body = mockMvc.perform(post("/api/posts/{postId}/comments", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(author))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("content", "추천받을 댓글"))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}

	private ResultActions like(Member actor, long commentId) throws Exception {
		return mockMvc.perform(put("/api/comments/{id}/like", commentId)
				.header(HttpHeaders.AUTHORIZATION, bearer(actor)));
	}

	private ResultActions commentsAs(Member viewer, long postId) throws Exception {
		return mockMvc.perform(get("/api/posts/{postId}/comments", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(viewer)))
				.andExpect(status().isOk());
	}
}
