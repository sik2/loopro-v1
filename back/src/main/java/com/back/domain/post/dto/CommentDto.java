package com.back.domain.post.dto;

import com.back.domain.post.entity.Comment;

import java.time.LocalDateTime;

public record CommentDto(
		Long id,
		String content,
		Long authorId,
		String authorNickname,
		LocalDateTime createDate,
		LocalDateTime modifyDate,
		long likeCount,
		/** 요청이 인증된 경우에만 참일 수 있다. */
		boolean likedByMe
) {

	public static CommentDto of(Comment comment, long likeCount, boolean likedByMe) {
		return new CommentDto(
				comment.getId(),
				comment.getContent(),
				comment.getAuthor().getId(),
				comment.getAuthor().getNickname(),
				comment.getCreateDate(),
				comment.getModifyDate(),
				likeCount,
				likedByMe
		);
	}
}
