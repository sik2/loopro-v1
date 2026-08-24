package com.back.global.testsupport;

import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * seam은 하나다: HTTP API.
 * 전체 컨텍스트를 띄우고 테스트 프로파일(인메모리 DB) 위에서 MockMvc로 요청을 보낸다.
 * 각 테스트는 트랜잭션이 롤백되므로 서로 간섭하지 않는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ApiTestSupport {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	protected String json(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new IllegalStateException("테스트 요청 본문을 직렬화하지 못했습니다.", e);
		}
	}
}
