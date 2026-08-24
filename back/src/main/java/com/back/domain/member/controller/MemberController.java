package com.back.domain.member.controller;

import com.back.domain.member.dto.MemberDto;
import com.back.domain.member.dto.SignupRequest;
import com.back.domain.member.service.MemberService;
import com.back.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원가입과 내 정보")
public class MemberController {

	private final MemberService memberService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "회원가입")
	public MemberDto signup(@Valid @RequestBody SignupRequest request) {
		return MemberDto.from(memberService.join(request.username(), request.password(), request.nickname()));
	}

	@GetMapping("/me")
	@Operation(summary = "내 정보", description = "Nickname, Role, 가입일을 낸다.")
	public MemberDto me(@AuthenticationPrincipal MemberPrincipal actor) {
		return MemberDto.from(memberService.findById(actor.id()));
	}
}
