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
		long viewCount,
		long commentCount,
		long likeCount,
		/** 요청이 인증된 경우에만 참일 수 있다. 비로그인 요청에는 항상 거짓이다. */
		boolean likedByMe
) {

	public static PostDetailDto of(Post post, long commentCount, long likeCount, boolean likedByMe) {
		return new PostDetailDto(
				post.getId(),
				post.getTitle(),
				post.getContent(),
				post.getAuthor().getId(),
				post.getAuthor().getNickname(),
				post.getCreateDate(),
				post.getModifyDate(),
				post.getViewCount(),
				commentCount,
				likeCount,
				likedByMe
		);
	}
}
