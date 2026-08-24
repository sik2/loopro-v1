package com.back.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI looproOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Loopro API")
				.description("블로그 기능을 기반으로 교안을 작성·공유하는 플랫폼")
				.version("v1"));
	}
}
