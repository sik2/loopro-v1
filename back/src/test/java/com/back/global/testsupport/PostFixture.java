package com.back.global.testsupport;

/** 여러 테스트가 함께 쓰는 마크다운 본문. 제목·목록·코드블록이 모두 들어 있다. */
public final class PostFixture {

	public static final String MARKDOWN = """
			# 제목

			- 항목 하나
			- 항목 둘

			```java
			System.out.println("hello");
			```
			""";

	private PostFixture() {
	}
}
