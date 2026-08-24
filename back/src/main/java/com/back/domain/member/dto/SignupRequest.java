package com.back.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 가입 입력. 이 규칙이 front의 폼 스키마와 1:1로 맞물린다.
 */
public record SignupRequest(
		@NotBlank(message = "Username을 입력해 주세요.")
		@Size(min = 3, max = 30, message = "Username은 3자 이상 30자 이하여야 합니다.")
		@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username은 영문, 숫자, 밑줄만 쓸 수 있습니다.")
		String username,

		@NotBlank(message = "비밀번호를 입력해 주세요.")
		@Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
		String password,

		@NotBlank(message = "Nickname을 입력해 주세요.")
		@Size(min = 2, max = 30, message = "Nickname은 2자 이상 30자 이하여야 합니다.")
		String nickname
) {
}
