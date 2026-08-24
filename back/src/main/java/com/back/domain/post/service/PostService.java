package com.back.domain.post.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

	/** 목록은 최신순 고정이다. 정렬 옵션은 없다. */
	private static final Sort LATEST_FIRST = Sort.by(Sort.Direction.DESC, "createDate").and(Sort.by(Sort.Direction.DESC, "id"));

	private static final int MAX_PAGE_SIZE = 50;

	private final PostRepository postRepository;
	private final MemberService memberService;

	@Transactional
	public Post write(long actorId, String title, String content) {
		Member author = memberService.findById(actorId);
		return postRepository.save(Post.write(author, title, content));
	}

	/** {@code page}는 1부터 시작한다. 범위를 벗어난 값은 가장 가까운 유효값으로 맞춘다. */
	@Transactional(readOnly = true)
	public Page<Post> getList(int page, int size) {
		int pageIndex = Math.max(page, 1) - 1;
		int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

		return postRepository.findAllBy(PageRequest.of(pageIndex, pageSize, LATEST_FIRST));
	}

	@Transactional(readOnly = true)
	public Post findById(long id) {
		return postRepository.findWithAuthorById(id)
				.orElseThrow(() -> ServiceException.notFound("존재하지 않는 Post입니다."));
	}
}
