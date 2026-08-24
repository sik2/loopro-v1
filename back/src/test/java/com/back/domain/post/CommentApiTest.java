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

@DisplayName("Comment API")
class CommentApiTest extends ApiTestSupport {

	@Test
	@DisplayName("로그인하지 않아도 댓글 목록을 읽을 수 있고, 작성자와 작성일이 담긴다")
	void 목록_조회() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member reader = joinMember("reader", "읽는사람");
		long postId = writePost(author);

		writeComment(author, postId, "첫 댓글").andExpect(status().isCreated());
		writeComment(reader, postId, "둘째 댓글").andExpect(status().isCreated());

		mockMvc.perform(get("/api/posts/{postId}/comments", postId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].content").value("첫 댓글"))
				.andExpect(jsonPath("$[0].authorNickname").value("구름"))
				.andExpect(jsonPath("$[0].createDate").isNotEmpty())
				.andExpect(jsonPath("$[1].authorNickname").value("읽는사람"));
	}

	@Test
	@DisplayName("Post 목록과 상세 응답에 댓글수가 담긴다")
	void 댓글수가_Post_응답에_담긴다() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author);

		writeComment(author, postId, "하나").andExpect(status().isCreated());
		writeComment(author, postId, "둘").andExpect(status().isCreated());

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(jsonPath("$.commentCount").value(2));

		mockMvc.perform(get("/api/posts"))
				.andExpect(jsonPath("$.items[0].commentCount").value(2));
	}

	@Test
	@DisplayName("작성자 본인은 자기 댓글을 고칠 수 있다")
	void 수정() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author);
		long commentId = writtenCommentId(author, postId, "원래 내용");

		modifyComment(author, commentId, "고친 내용").andExpect(status().isOk());

		mockMvc.perform(get("/api/posts/{postId}/comments", postId))
				.andExpect(jsonPath("$[0].content").value("고친 내용"));
	}

	@Test
	@DisplayName("작성자 본인은 자기 댓글을 지울 수 있다")
	void 삭제() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author);
		long commentId = writtenCommentId(author, postId, "지울 댓글");

		mockMvc.perform(delete("/api/comments/{id}", commentId)
						.header(HttpHeaders.AUTHORIZATION, bearer(author)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/posts/{postId}/comments", postId))
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	@DisplayName("남의 댓글은 고칠 수 없다")
	void 남의_댓글_수정_거부() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member stranger = joinMember("stranger", "낯선사람");
		long postId = writePost(author);
		long commentId = writtenCommentId(author, postId, "원래 내용");

		modifyComment(stranger, commentId, "가로챈 내용").andExpect(status().isForbidden());

		mockMvc.perform(get("/api/posts/{postId}/comments", postId))
				.andExpect(jsonPath("$[0].content").value("원래 내용"));
	}

	@Test
	@DisplayName("ADMIN도 남의 댓글은 고칠 수 없다")
	void ADMIN도_남의_댓글_수정_거부() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member admin = joinAdmin("admin", "관리자");
		long postId = writePost(author);
		long commentId = writtenCommentId(author, postId, "원래 내용");

		modifyComment(admin, commentId, "관리자가 고침").andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("남의 댓글은 지울 수 없다")
	void 남의_댓글_삭제_거부() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member stranger = joinMember("stranger", "낯선사람");
		long postId = writePost(author);
		long commentId = writtenCommentId(author, postId, "남의 댓글");

		mockMvc.perform(delete("/api/comments/{id}", commentId)
						.header(HttpHeaders.AUTHORIZATION, bearer(stranger)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/posts/{postId}/comments", postId))
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	@DisplayName("ADMIN은 남의 댓글을 지울 수 있다")
	void ADMIN_삭제_허용() throws Exception {
		Member author = joinMember("gureum", "구름");
		Member admin = joinAdmin("admin", "관리자");
		long postId = writePost(author);
		long commentId = writtenCommentId(author, postId, "문제되는 댓글");

		mockMvc.perform(delete("/api/comments/{id}", commentId)
						.header(HttpHeaders.AUTHORIZATION, bearer(admin)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/posts/{postId}/comments", postId))
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	@DisplayName("토큰 없이 댓글을 달려 하면 401이다")
	void 작성은_인증을_요구한다() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author);

		mockMvc.perform(post("/api/posts/{postId}/comments", postId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("content", "익명 댓글"))))
				.andExpect(status().isUnauthorized());
	}

	private long writePost(Member author) throws Exception {
		String body = mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(author))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", "댓글 달 글", "content", "본문"))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}

	private ResultActions writeComment(Member actor, long postId, String content) throws Exception {
		return mockMvc.perform(post("/api/posts/{postId}/comments", postId)
				.header(HttpHeaders.AUTHORIZATION, bearer(actor))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("content", content))));
	}

	private long writtenCommentId(Member actor, long postId, String content) throws Exception {
		String body = writeComment(actor, postId, content)
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}

	private ResultActions modifyComment(Member actor, long commentId, String content) throws Exception {
		return mockMvc.perform(put("/api/comments/{id}", commentId)
				.header(HttpHeaders.AUTHORIZATION, bearer(actor))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("content", content))));
	}
}
