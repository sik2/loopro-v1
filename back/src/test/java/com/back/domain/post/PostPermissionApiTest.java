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

@DisplayName("Post 수정·삭제 권한 API")
class PostPermissionApiTest extends ApiTestSupport {

	@Test
	@DisplayName("작성자 본인은 자기 글을 고칠 수 있다")
	void 본인_수정_성공() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author, "원래 제목", "원래 본문");

		modify(author, postId, "고친 제목", "고친 본문")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("고친 제목"));

		// 후속 조회로 실제로 바뀌었는지 확인한다.
		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(jsonPath("$.title").value("고친 제목"))
				.andExpect(jsonPath("$.content").value("고친 본문"));
	}

	@Test
	@DisplayName("남의 글을 고치려 하면 403이다")
	void 남의_글_수정_거부() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member stranger = joinMember("stranger", "낯선사람");
		long postId = writePost(author, "원래 제목", "원래 본문");

		modify(stranger, postId, "가로챈 제목", "가로챈 본문")
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(jsonPath("$.title").value("원래 제목"));
	}

	@Test
	@DisplayName("ADMIN도 남의 글은 고칠 수 없다. 삭제는 관리지만 수정은 위조다")
	void ADMIN도_남의_글_수정_거부() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member admin = joinAdmin("admin", "관리자");
		long postId = writePost(author, "원래 제목", "원래 본문");

		modify(admin, postId, "관리자가 고친 제목", "관리자가 고친 본문")
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(jsonPath("$.title").value("원래 제목"));
	}

	@Test
	@DisplayName("작성자 본인은 자기 글을 지울 수 있다")
	void 본인_삭제_성공() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author, "지울 글", "본문");

		mockMvc.perform(delete("/api/posts/{id}", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(author)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("남의 글을 지우려 하면 403이고, 글은 그대로 남는다")
	void 남의_글_삭제_거부() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member stranger = joinMember("stranger", "낯선사람");
		long postId = writePost(author, "남의 글", "본문");

		mockMvc.perform(delete("/api/posts/{id}", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(stranger)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("ADMIN은 남의 글을 지울 수 있다")
	void ADMIN_삭제_허용() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member admin = joinAdmin("admin", "관리자");
		long postId = writePost(author, "문제되는 글", "본문");

		mockMvc.perform(delete("/api/posts/{id}", postId)
						.header(HttpHeaders.AUTHORIZATION, bearer(admin)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(status().isNotFound());
	}

	private long writePost(Member author, String title, String content) throws Exception {
		String body = mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(author))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", title, "content", content, "published", true))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return objectMapper.readTree(body).path("id").asLong();
	}

	private ResultActions modify(Member actor, long postId, String title, String content) throws Exception {
		return mockMvc.perform(put("/api/posts/{id}", postId)
				.header(HttpHeaders.AUTHORIZATION, bearer(actor))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("title", title, "content", content, "published", true))));
	}
}
