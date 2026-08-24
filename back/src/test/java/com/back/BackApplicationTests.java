package com.back;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("설정 스모크")
class BackApplicationTests {

	@Test
	@DisplayName("애플리케이션 컨텍스트가 뜬다")
	void contextLoads() {
	}
}
