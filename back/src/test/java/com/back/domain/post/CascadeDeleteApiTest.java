package com.back.domain.post;

import com.back.domain.member.entity.Member;
import com.back.global.testsupport.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 삭제 API가 204를 냈다는 것만으로 통과시키지 않는다.
 * 삭제 뒤에 무엇이 남았는지를 확인한다.
 */
@DisplayName("삭제 연쇄 정리")
class CascadeDeleteApiTest extends ApiTestSupport {

	@Test
	@DisplayName("Post를 지우면 딸린 Comment와 두 종류의 추천 기록이 함께 사라진다")
	void Post_삭제가_전부_정리한다() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");

		long postId = writePost(author);
		long commentA = writeComment(author, postId, "첫 댓글");
		long commentB = writeComment(reader, postId, "둘째 댓글");

		likePost(reader, postId);
		likePost(author, postId);
		likeComment(reader, commentA);
		likeComment(author, commentB);

		// 지우기 전 상태를 확인해 둔다. 그래야 "원래 없었다"로 통과할 수 없다.
		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(jsonPath("$.commentCount").value(2))
				.andExpect(jsonPath("$.likeCount").value(2));

		mockMvc.perform(delete("/api/posts/{id}", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(author)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/posts/{id}", postId)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/posts/{postId}/comments", postId)).andExpect(status().isNotFound());

		assertThat(commentRepository.findIdsByPostId(postId)).isEmpty();
		assertThat(postLikeRepository.countByPost_IdIn(List.of(postId))).isZero();
		assertThat(commentLikeRepository.countByComment_IdIn(List.of(commentA, commentB))).isZero();
	}

	@Test
	@DisplayName("Comment를 지우면 그 Comment의 추천 기록이 사라진다")
	void Comment_삭제가_추천을_정리한다() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");

		long postId = writePost(author);
		long commentId = writeComment(author, postId, "지울 댓글");
		likeComment(reader, commentId);

		mockMvc.perform(get("/api/posts/{postId}/comments", postId))
				.andExpect(jsonPath("$[0].likeCount").value(1));

		mockMvc.perform(delete("/api/comments/{id}", commentId)
						.header(HttpHeaders.AUTHORIZATION, bearer(author)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/posts/{postId}/comments", postId))
				.andExpect(jsonPath("$.length()").value(0));

		assertThat(commentLikeRepository.countByComment_IdIn(List.of(commentId))).isZero();
	}

	private long writePost(Member author) throws Exception {
		String body = mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(author))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", "지울 글", "content", "본문", "published", true))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}

	private long writeComment(Member actor, long postId, String content) throws Exception {
		String body = mockMvc.perform(post("/api/posts/{postId}/comments", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(actor))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("content", content))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}

	private void likePost(Member actor, long postId) throws Exception {
		mockMvc.perform(put("/api/posts/{id}/like", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(actor)))
				.andExpect(status().isNoContent());
	}

	private void likeComment(Member actor, long commentId) throws Exception {
		mockMvc.perform(put("/api/comments/{id}/like", commentId)
						.header(HttpHeaders.AUTHORIZATION, bearer(actor)))
				.andExpect(status().isNoContent());
	}
}
