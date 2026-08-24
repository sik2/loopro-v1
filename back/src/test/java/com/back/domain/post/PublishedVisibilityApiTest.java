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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Published 공개 범위")
class PublishedVisibilityApiTest extends ApiTestSupport {

	@Test
	@DisplayName("비발행 글은 남의 목록에 뜨지 않고, 작성자 본인의 목록에는 뜬다")
	void 목록_가시성() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member stranger = joinMember("stranger", "낯선사람");

		writePost(author, "발행한 글", true);
		writePost(author, "숨겨둔 글", false);

		mockMvc.perform(get("/api/posts"))
				.andExpect(jsonPath("$.totalItems").value(1))
				.andExpect(jsonPath("$.items[0].title").value("발행한 글"));

		mockMvc.perform(get("/api/posts").header(HttpHeaders.AUTHORIZATION, bearer(stranger)))
				.andExpect(jsonPath("$.totalItems").value(1));

		mockMvc.perform(get("/api/posts").header(HttpHeaders.AUTHORIZATION, bearer(author)))
				.andExpect(jsonPath("$.totalItems").value(2))
				.andExpect(jsonPath("$.items[0].title").value("숨겨둔 글"))
				.andExpect(jsonPath("$.items[0].published").value(false));
	}

	@Test
	@DisplayName("비발행 글의 상세는 남에게 404다. 403이면 글이 있다는 사실이 드러난다")
	void 상세_가시성() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member stranger = joinMember("stranger", "낯선사람");
		long postId = writePost(author, "숨겨둔 글", false);

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/posts/{id}", postId).header(HttpHeaders.AUTHORIZATION, bearer(stranger)))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/posts/{id}", postId).header(HttpHeaders.AUTHORIZATION, bearer(author)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("숨겨둔 글"))
				.andExpect(jsonPath("$.published").value(false));
	}

	@Test
	@DisplayName("발행으로 바꾸면 남에게도 보이고, 도로 내리면 다시 안 보인다")
	void 발행_전환() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member stranger = joinMember("stranger", "낯선사람");
		long postId = writePost(author, "숨겨둔 글", false);

		modify(author, postId, "이제 올린 글", true).andExpect(status().isOk());

		mockMvc.perform(get("/api/posts/{id}", postId).header(HttpHeaders.AUTHORIZATION, bearer(stranger)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.published").value(true));

		modify(author, postId, "다시 내린 글", false).andExpect(status().isOk());

		mockMvc.perform(get("/api/posts/{id}", postId).header(HttpHeaders.AUTHORIZATION, bearer(stranger)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("비발행 글에는 작성자 본인만 댓글을 달 수 있다")
	void 비발행_글의_댓글() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member stranger = joinMember("stranger", "낯선사람");
		long postId = writePost(author, "숨겨둔 글", false);

		comment(stranger, postId, "남의 댓글").andExpect(status().isNotFound());
		comment(author, postId, "내 메모").andExpect(status().isCreated());

		// 댓글 목록 경로로도 존재가 드러나지 않는다.
		mockMvc.perform(get("/api/posts/{postId}/comments", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(stranger)))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/posts/{postId}/comments", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(author)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	@DisplayName("발행 여부를 안 보내면 비발행이다. 실수로 공개되지 않는 쪽이 안전하다")
	void 기본값은_비발행() throws Exception {
		Member author = joinMember("gureum", "구름");

		String body = mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(author))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", "값을 안 보낸 글", "content", "본문"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.published").value(false))
				.andReturn().getResponse().getContentAsString();

		long postId = objectMapper.readTree(body).path("id").asLong();
		mockMvc.perform(get("/api/posts/{id}", postId)).andExpect(status().isNotFound());
	}

	private long writePost(Member author, String title, boolean published) throws Exception {
		String body = mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(author))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", title, "content", "본문", "published", published))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}

	private org.springframework.test.web.servlet.ResultActions modify(
			Member actor, long postId, String title, boolean published) throws Exception {
		return mockMvc.perform(put("/api/posts/{id}", postId)
				.header(HttpHeaders.AUTHORIZATION, bearer(actor))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("title", title, "content", "본문", "published", published))));
	}

	private org.springframework.test.web.servlet.ResultActions comment(
			Member actor, long postId, String content) throws Exception {
		return mockMvc.perform(post("/api/posts/{postId}/comments", postId)
				.header(HttpHeaders.AUTHORIZATION, bearer(actor))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("content", content))));
	}
}
