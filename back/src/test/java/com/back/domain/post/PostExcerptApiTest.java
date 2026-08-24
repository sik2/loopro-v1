package com.back.domain.post;

import com.back.domain.member.entity.Member;
import com.back.global.testsupport.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("목록 미리보기(excerpt) API")
class PostExcerptApiTest extends ApiTestSupport {

	@Test
	@DisplayName("짧은 글은 본문이 그대로 미리보기로 나온다")
	void 짧은_글() throws Exception {
		Member author = joinMember("gureum", "구름");
		writePost(author, "# 제목\n\n짧은 본문입니다.\n");

		mockMvc.perform(get("/api/posts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].excerpt").value("# 제목\n\n짧은 본문입니다.\n"));
	}

	@Test
	@DisplayName("긴 글은 앞부분만 잘려 나오고, 마크다운 기호는 그대로 남는다")
	void 긴_글은_잘린다() throws Exception {
		Member author = joinMember("gureum", "구름");
		String content = "# 제목\n\n```java\nSystem.out.println(1);\n```\n\n" + "가".repeat(1000);
		writePost(author, content);

		String excerpt = objectMapper
				.readTree(mockMvc.perform(get("/api/posts")).andReturn().getResponse().getContentAsString())
				.path("items").get(0).path("excerpt").asString();

		assertThat(excerpt).hasSize(300);
		// back은 마크다운을 해석하지 않는다. 자르기만 하므로 기호가 살아 있어야 한다.
		assertThat(excerpt).startsWith("# 제목\n\n```java");
		assertThat(content).startsWith(excerpt);
	}

	@Test
	@DisplayName("이모지가 경계에 걸려도 반토막 나지 않는다")
	void 이모지가_깨지지_않는다() throws Exception {
		Member author = joinMember("gureum", "구름");
		// 코드 포인트 300번째가 이모지 한가운데가 되도록 맞춘다.
		writePost(author, "가".repeat(299) + "🙂" + "나".repeat(50));

		String excerpt = objectMapper
				.readTree(mockMvc.perform(get("/api/posts")).andReturn().getResponse().getContentAsString())
				.path("items").get(0).path("excerpt").asString();

		assertThat(excerpt.codePointCount(0, excerpt.length())).isEqualTo(300);
		assertThat(excerpt).endsWith("🙂");
	}

	@Test
	@DisplayName("상세 응답에는 excerpt가 없다. 거기엔 본문 전체가 있다")
	void 상세에는_없다() throws Exception {
		Member author = joinMember("gureum", "구름");
		long postId = writePost(author, "# 제목\n\n본문\n");

		mockMvc.perform(get("/api/posts/{id}", postId))
				.andExpect(jsonPath("$.excerpt").doesNotExist())
				.andExpect(jsonPath("$.content").value("# 제목\n\n본문\n"));
	}

	private long writePost(Member author, String content) throws Exception {
		String body = mockMvc.perform(post("/api/posts")
						.header(HttpHeaders.AUTHORIZATION, bearer(author))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("title", "미리보기 확인용", "content", content, "published", true))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}
}
