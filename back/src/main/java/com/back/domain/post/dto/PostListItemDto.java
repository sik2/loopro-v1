package com.back.domain.post.dto;

import com.back.domain.post.entity.Post;

import java.time.LocalDateTime;

/** 목록 한 칸. 본문 전체 대신 앞부분만 담는다. */
public record PostListItemDto(
		Long id,
		String title,
		/** 본문 앞부분. 마크다운 원문 그대로이며, 기호를 벗기는 일은 front가 한다. */
		String excerpt,
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

	/** 미리보기 길이. 두 줄을 채우고 남을 만큼만 보낸다 — 목록에 본문 전체를 실을 이유가 없다. */
	private static final int EXCERPT_LENGTH = 300;

	public static PostListItemDto of(Post post, long commentCount, long likeCount, boolean likedByMe) {
		return new PostListItemDto(
				post.getId(),
				post.getTitle(),
				post.excerpt(EXCERPT_LENGTH),
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
