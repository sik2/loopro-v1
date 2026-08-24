package com.back.domain.post.dto;

import com.back.domain.post.entity.Post;

import java.time.LocalDateTime;

/** 글 상세. content는 마크다운 원문 그대로다. */
public record PostDetailDto(
		Long id,
		String title,
		String content,
		Long authorId,
		String authorNickname,
		LocalDateTime createDate,
		LocalDateTime modifyDate,
		long viewCount
) {

	public static PostDetailDto from(Post post) {
		return new PostDetailDto(
				post.getId(),
				post.getTitle(),
				post.getContent(),
				post.getAuthor().getId(),
				post.getAuthor().getNickname(),
				post.getCreateDate(),
				post.getModifyDate(),
				post.getViewCount()
		);
	}
}
