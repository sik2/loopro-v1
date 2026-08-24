package com.back.domain.post.dto;

import com.back.domain.post.entity.Comment;

import java.time.LocalDateTime;

public record CommentDto(
		Long id,
		String content,
		Long authorId,
		String authorNickname,
		LocalDateTime createDate,
		LocalDateTime modifyDate
) {

	public static CommentDto from(Comment comment) {
		return new CommentDto(
				comment.getId(),
				comment.getContent(),
				comment.getAuthor().getId(),
				comment.getAuthor().getNickname(),
				comment.getCreateDate(),
				comment.getModifyDate()
		);
	}
}
