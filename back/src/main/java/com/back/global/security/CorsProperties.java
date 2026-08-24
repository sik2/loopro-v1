package com.back.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * CORS 허용 출처. 코드에 하드코딩하지 않고 프로파일 설정에서 주입한다.
 *
 * <p>YAML 목록으로도, 쉼표로 구분한 한 줄로도 받는다. 배포 환경에서는 값이
 * 환경변수 하나로 들어오기 때문이다.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

	public CorsProperties {
		allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
	}
}
