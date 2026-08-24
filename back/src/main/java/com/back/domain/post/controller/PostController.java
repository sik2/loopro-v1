package com.back.domain.post.controller;

import com.back.domain.post.dto.PostDetailDto;
import com.back.domain.post.dto.PostListItemDto;
import com.back.domain.post.dto.PostUpdateRequest;
import com.back.domain.post.dto.PostWriteRequest;
import com.back.domain.post.service.PostService;
import com.back.global.dto.PageDto;
import com.back.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "글 목록·상세·작성")
public class PostController {

	private final PostService postService;

	@GetMapping
	@Operation(summary = "글 목록", description = "최신순 고정. page는 1부터 시작한다.")
	public PageDto<PostListItemDto> list(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		return PageDto.of(postService.getList(page, size).map(PostListItemDto::from));
	}

	@GetMapping("/{id}")
	@Operation(summary = "글 상세", description = "열 때마다 ViewCount가 1 오른다.")
	public PostDetailDto detail(@PathVariable long id) {
		return PostDetailDto.from(postService.readDetail(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "글 작성", description = "본문은 마크다운 텍스트다. back은 해석하지 않는다.")
	public PostDetailDto write(
			@AuthenticationPrincipal MemberPrincipal actor,
			@Valid @RequestBody PostWriteRequest request
	) {
		return PostDetailDto.from(postService.write(actor.id(), request.title(), request.content()));
	}

	@PutMapping("/{id}")
	@Operation(summary = "글 수정", description = "작성자 본인만 할 수 있다. ADMIN도 남의 글은 수정할 수 없다.")
	public PostDetailDto modify(
			@AuthenticationPrincipal MemberPrincipal actor,
			@PathVariable long id,
			@Valid @RequestBody PostUpdateRequest request
	) {
		return PostDetailDto.from(postService.modify(actor, id, request.title(), request.content()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "글 삭제", description = "작성자 본인과 ADMIN이 할 수 있다.")
	public void delete(@AuthenticationPrincipal MemberPrincipal actor, @PathVariable long id) {
		postService.delete(actor, id);
	}
}
