package com.back.global.security.jwt;

import com.back.domain.member.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * access token을 만들고 읽는다. refresh token은 없다(ADR-0002).
 * 서버는 토큰을 무효화하지 않으므로, 로그아웃해도 그 토큰은 만료 전까지 유효하다.
 */
@Component
public class JwtProvider {

	private final SecretKey key;
	private final JwtProperties properties;

	public JwtProvider(JwtProperties properties) {
		byte[] secret = properties.secret().getBytes(StandardCharsets.UTF_8);
		if (secret.length < 32) {
			throw new IllegalStateException("app.jwt.secret은 32바이트 이상이어야 합니다. (HS256)");
		}
		this.key = Keys.hmacShaKeyFor(secret);
		this.properties = properties;
	}

	public String createAccessToken(Member member) {
		Instant now = Instant.now();

		return Jwts.builder()
				.subject(String.valueOf(member.getId()))
				.claim("nickname", member.getNickname())
				.claim("role", member.getRole().name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(properties.expiration())))
				.signWith(key)
				.compact();
	}

	/** 서명이 맞고 아직 만료되지 않은 토큰이면 payload를, 아니면 빈 값을 준다. */
	public Optional<Claims> readClaims(String token) {
		try {
			return Optional.of(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
		} catch (JwtException | IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
