package com.back.domain.post;

import com.back.domain.member.entity.Member;
import com.back.global.testsupport.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Post 목록·상세·작성 API")
class PostApiTest extends ApiTestSupport {

	private static final String MARKDOWN = """
			# 제목

			- 항목 하나
			- 항목 둘

			```java
			System.out.println("hello");
			```
			""";

	@Test
	@DisplayName("로그인한 Member가 글을 쓰면 201과 상세를 돌려준다")
	void 작성() throws Exception {
		Member author = joinMember("gureum", "구름");

		write(author, "첫 글", MARKDOWN)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("첫 글"))
				// back은 마크다운을 해석하지 않는다. 넣은 그대로 나온다.
				.andExpect(jsonPath("$.content").value(MARKDOWN))
				.andExpect(jsonPath("$.authorNickname").value("구름"));
	}

	@Test
	@DisplayName("토큰 없이 글을 쓰려 하면 401이다")
	void 작성은_인증을_요구한다() throws Exception {
		mockMvc.perform(post("/api/posts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", "제목", "content", "본문", "published", true))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("목록은 최신순으로 페이지 단위로 넘어간다. page는 1부터다")
	void 목록_페이징() throws Exception {
		Member author = joinMember("gureum", "구름");
		for (int i = 1; i <= 5; i++) {
			write(author, "글 " + i, "본문 " + i).andExpect(status().isCreated());
		}

		mockMvc.perform(get("/api/posts").param("page", "1").param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.size").value(2))
				.andExpect(jsonPath("$.totalItems").value(5))
				.andExpect(jsonPath("$.totalPages").value(3))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].title").value("글 5"))
				.andExpect(jsonPath("$.items[1].title").value("글 4"))
				.andExpect(jsonPath("$.items[0].authorNickname").value("구름"))
				.andExpect(jsonPath("$.items[0].createDate").isNotEmpty())
				// 목록에는 본문을 담지 않는다.
				.andExpect(jsonPath("$.items[0].content").doesNotExist());

		mockMvc.perform(get("/api/posts").param("page", "3").param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].title").value("글 1"));
	}

	@Test
	@DisplayName("로그인하지 않아도 상세를 읽을 수 있다")
	void 상세_조회() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writtenPostId(author, "읽을 글", MARKDOWN);

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value((int) postId))
				.andExpect(jsonPath("$.title").value("읽을 글"))
				.andExpect(jsonPath("$.content").value(MARKDOWN))
				.andExpect(jsonPath("$.authorNickname").value("구름"));
	}

	@Test
	@DisplayName("없는 글을 열면 404다")
	void 없는_글은_404() throws Exception {
		mockMvc.perform(get("/api/posts/{id}", 9_999_999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").isNotEmpty());
	}

	private ResultActions write(Member author, String title, String content) throws Exception {
		return mockMvc.perform(post("/api/posts")
				.header(HttpHeaders.AUTHORIZATION, bearer(author))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("title", title, "content", content, "published", true))));
	}

	private long writtenPostId(Member author, String title, String content) throws Exception {
		String body = write(author, title, content).andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}
}
