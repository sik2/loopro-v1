package com.back.domain.post.dto;

import com.back.domain.post.entity.Post;

import java.time.LocalDateTime;

/** 목록 한 줄. 본문은 담지 않는다. */
public record PostListItemDto(
		Long id,
		String title,
		String authorNickname,
		LocalDateTime createDate,
		long viewCount,
		long commentCount,
		long likeCount,
		/** 요청이 인증된 경우에만 참일 수 있다. 비로그인 요청에는 항상 거짓이다. */
		boolean likedByMe,
		/** 거짓이면 작성자 본인에게만 보이는 글이다. */
		boolean published
) {

	public static PostListItemDto of(Post post, long commentCount, long likeCount, boolean likedByMe) {
		return new PostListItemDto(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getNickname(),
				post.getCreateDate(),
				post.getViewCount(),
				commentCount,
				likeCount,
				likedByMe,
				post.isPublished()
		);
	}
}
