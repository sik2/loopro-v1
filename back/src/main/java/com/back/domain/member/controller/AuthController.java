package com.back.domain.member.controller;

import com.back.domain.member.dto.LoginRequest;
import com.back.domain.member.dto.LoginResponse;
import com.back.domain.member.dto.MemberDto;
import com.back.domain.member.service.AuthService;
import com.back.global.security.LoginAttemptGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "로그인")
public class AuthController {

	private final AuthService authService;
	private final LoginAttemptGuard loginAttemptGuard;

	/**
	 * 로그아웃 API는 없다. 로그아웃은 클라이언트가 저장된 토큰을 버리는 것이 전부다(ADR-0002).
	 */
	@PostMapping("/login")
	@Operation(summary = "로그인", description = "access token을 낸다. 유효기간은 7일이며 서버는 이 토큰을 무효화하지 않는다.")
	public LoginResponse login(HttpServletRequest request, @Valid @RequestBody LoginRequest body) {
		String clientIp = request.getRemoteAddr();

		loginAttemptGuard.checkNotLocked(body.username(), clientIp);

		try {
			AuthService.Login login = authService.login(body.username(), body.password());
			loginAttemptGuard.recordSuccess(body.username(), clientIp);
			return new LoginResponse(login.accessToken(), MemberDto.from(login.member()));
		} catch (RuntimeException e) {
			loginAttemptGuard.recordFailure(body.username(), clientIp);
			throw e;
		}
	}
}
