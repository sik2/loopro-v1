package com.back.domain.post.controller;

import com.back.domain.post.dto.CommentDto;
import com.back.domain.post.dto.CommentWriteRequest;
import com.back.domain.post.service.CommentLikeService;
import com.back.domain.post.service.CommentService;
import com.back.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comment", description = "글에 달린 답글")
public class CommentController {

	private final CommentService commentService;
	private final CommentLikeService commentLikeService;

	@GetMapping("/api/posts/{postId}/comments")
	@Operation(summary = "댓글 목록", description = "페이징하지 않고 전부 낸다.")
	public List<CommentDto> list(@AuthenticationPrincipal MemberPrincipal viewer, @PathVariable long postId) {
		return commentService.getListByPost(postId, viewer);
	}

	@PostMapping("/api/posts/{postId}/comments")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "댓글 작성")
	public CommentDto write(
			@AuthenticationPrincipal MemberPrincipal actor,
			@PathVariable long postId,
			@Valid @RequestBody CommentWriteRequest request
	) {
		return commentService.write(actor, postId, request.content());
	}

	@PutMapping("/api/comments/{id}")
	@Operation(summary = "댓글 수정", description = "작성자 본인만 할 수 있다.")
	public CommentDto modify(
			@AuthenticationPrincipal MemberPrincipal actor,
			@PathVariable long id,
			@Valid @RequestBody CommentWriteRequest request
	) {
		return commentService.modify(actor, id, request.content());
	}

	@DeleteMapping("/api/comments/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "댓글 삭제", description = "작성자 본인과 ADMIN이 할 수 있다.")
	public void delete(@AuthenticationPrincipal MemberPrincipal actor, @PathVariable long id) {
		commentService.delete(actor, id);
	}

	@PutMapping("/api/comments/{id}/like")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "댓글 추천", description = "이미 추천한 댓글을 다시 추천해도 기록이 늘지 않는다.")
	public void like(@AuthenticationPrincipal MemberPrincipal actor, @PathVariable long id) {
		commentLikeService.like(actor, id);
	}

	@DeleteMapping("/api/comments/{id}/like")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "댓글 추천 취소")
	public void cancelLike(@AuthenticationPrincipal MemberPrincipal actor, @PathVariable long id) {
		commentLikeService.cancel(actor, id);
	}
}
