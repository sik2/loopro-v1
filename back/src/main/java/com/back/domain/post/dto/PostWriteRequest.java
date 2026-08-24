package com.back.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostWriteRequest(
		@NotBlank(message = "제목을 입력해 주세요.")
		@Size(max = 200, message = "제목은 200자 이하여야 합니다.")
		String title,

		@NotBlank(message = "본문을 입력해 주세요.")
		@Size(max = 100_000, message = "10만자 이하여야 합니다.")
		String content,

		/** 작성 시점의 발행 여부. */
		Boolean published
) {

	/** 값이 오지 않으면 비발행으로 본다 — 실수로 공개되는 쪽보다 안전하다. */
	public PostWriteRequest {
		published = published != null && published;
	}
}
