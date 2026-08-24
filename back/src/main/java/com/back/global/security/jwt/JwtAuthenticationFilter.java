package com.back.global.security.jwt;

import com.back.domain.member.entity.Role;
import com.back.global.security.MemberPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 요청 헤더의 access token을 읽어 보안 컨텍스트를 채운다.
 *
 * <p>토큰이 없거나 읽을 수 없으면(서명 불일치, 만료) 익명 상태로 통과시킨다.
 * 인증이 필요한 경로였다면 그 다음에 entry point가 401을 낸다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		readBearerToken(request)
				.flatMap(jwtProvider::readClaims)
				.map(this::toPrincipal)
				.ifPresent(this::authenticate);

		filterChain.doFilter(request, response);
	}

	private java.util.Optional<String> readBearerToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			return java.util.Optional.empty();
		}
		String token = header.substring(BEARER_PREFIX.length()).trim();
		return token.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(token);
	}

	private MemberPrincipal toPrincipal(Claims claims) {
		return new MemberPrincipal(
				Long.valueOf(claims.getSubject()),
				claims.get("nickname", String.class),
				Role.valueOf(claims.get("role", String.class))
		);
	}

	private void authenticate(MemberPrincipal principal) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(principal, null, principal.authorities()));
	}
}
