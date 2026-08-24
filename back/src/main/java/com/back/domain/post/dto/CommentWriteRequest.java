package com.back.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentWriteRequest(
		@NotBlank(message = "내용을 입력해 주세요.")
		@Size(max = 2000, message = "댓글은 2000자 이하여야 합니다.")
		String content
) {
}
