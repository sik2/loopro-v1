package com.back.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT 서명 키와 유효기간. 개발 프로파일에 기본값이 있고 환경변수로 덮어쓸 수 있다.
 * (예: {@code APP_JWT_SECRET})
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration) {
}
