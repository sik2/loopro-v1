package com.back.domain.post.dto;

import com.back.domain.post.entity.Post;

import java.time.LocalDateTime;

/** 목록 한 줄. 본문은 담지 않는다. */
public record PostListItemDto(
		Long id,
		String title,
		String authorNickname,
		LocalDateTime createDate
) {

	public static PostListItemDto from(Post post) {
		return new PostListItemDto(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getNickname(),
				post.getCreateDate()
		);
	}
}
