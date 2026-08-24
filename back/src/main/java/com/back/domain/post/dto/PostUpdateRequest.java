package com.back.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
		@NotBlank(message = "제목을 입력해 주세요.")
		@Size(max = 200, message = "제목은 200자 이하여야 합니다.")
		String title,

		@NotBlank(message = "본문을 입력해 주세요.")
		@Size(max = 100_000, message = "10만자 이하여야 합니다.")
		String content,

		/** 수정으로 발행/비발행을 전환한다. */
		Boolean published
) {

	/** 값이 오지 않으면 비발행으로 본다 — 실수로 공개되는 쪽보다 안전하다. */
	public PostUpdateRequest {
		published = published != null && published;
	}
}
