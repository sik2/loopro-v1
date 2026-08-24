package com.back.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "필수 항목입니다.")
		String username,

		@NotBlank(message = "필수 항목입니다.")
		String password
) {
}
