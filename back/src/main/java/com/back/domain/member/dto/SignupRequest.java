package com.back.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 가입 입력. 이 규칙이 front의 폼 스키마와 1:1로 맞물린다.
 *
 * <p>메시지에 항목 이름을 넣지 않는다. 응답의 {@code errors[].field}가 어느 칸인지 이미
 * 알려주고, 그 칸을 화면에서 뭐라 부르는지는 화면이 정한다. 여기에 "Username"이라고 적으면
 * 표기를 바꿀 때마다 back을 열어야 한다.
 */
public record SignupRequest(
		@NotBlank(message = "필수 항목입니다.")
		@Size(min = 3, max = 30, message = "3자 이상 30자 이하여야 합니다.")
		@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "영문, 숫자, 밑줄만 쓸 수 있습니다.")
		String username,

		@NotBlank(message = "필수 항목입니다.")
		@Size(min = 8, max = 64, message = "8자 이상 64자 이하여야 합니다.")
		String password,

		@NotBlank(message = "필수 항목입니다.")
		@Size(min = 2, max = 30, message = "2자 이상 30자 이하여야 합니다.")
		String nickname
) {
}
