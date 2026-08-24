package com.back.domain.post.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.dto.PostDetailDto;
import com.back.domain.post.dto.PostListItemDto;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.CommentRepository;
import com.back.domain.post.repository.IdCount;
import com.back.domain.post.repository.PostRepository;
import com.back.global.dto.PageDto;
import com.back.global.exception.ServiceException;
import com.back.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Post와 그에 딸린 것들을 다룬다.
 *
 * <p>댓글수·추천수는 Post의 컬럼이 아니라 매번 세는 값이다. 목록에서는 항목마다 세지 않고
 * 한 번의 집계 쿼리로 모아 온다.
 */
@Service
@RequiredArgsConstructor
public class PostService {

	/** 목록은 최신순 고정이다. 정렬 옵션은 없다. */
	private static final Sort LATEST_FIRST = Sort.by(Sort.Direction.DESC, "createDate")
			.and(Sort.by(Sort.Direction.DESC, "id"));

	private static final int MAX_PAGE_SIZE = 50;

	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final MemberService memberService;

	/** {@code page}는 1부터 시작한다. 범위를 벗어난 값은 가장 가까운 유효값으로 맞춘다. */
	@Transactional(readOnly = true)
	public PageDto<PostListItemDto> getList(int page, int size) {
		int pageIndex = Math.max(page, 1) - 1;
		int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

		Page<Post> posts = postRepository.findAllBy(PageRequest.of(pageIndex, pageSize, LATEST_FIRST));
		List<Long> postIds = posts.map(Post::getId).toList();
		Map<Long, Long> commentCounts = countMap(commentRepository.countByPostIds(postIds));

		return PageDto.of(posts.map(post -> PostListItemDto.of(post, commentCounts.getOrDefault(post.getId(), 0L))));
	}

	/**
	 * 상세 조회. ViewCount를 1 올리므로 쓰기 트랜잭션이다 — 열 때마다 UPDATE가 한 번 나간다.
	 * 트래픽이 늘면 가장 먼저 문제가 될 지점이다.
	 */
	@Transactional
	public PostDetailDto readDetail(long id) {
		Post post = findById(id);
		post.increaseViewCount();
		return toDetail(post);
	}

	@Transactional
	public PostDetailDto write(MemberPrincipal actor, String title, String content) {
		Member author = memberService.findById(actor.id());
		return toDetail(postRepository.save(Post.write(author, title, content)));
	}

	/** 수정은 작성자 본인만 가능하다. ADMIN도 남의 글은 수정할 수 없다 — 삭제는 관리지만 수정은 위조다. */
	@Transactional
	public PostDetailDto modify(MemberPrincipal actor, long postId, String title, String content) {
		Post post = findById(postId);

		if (!post.isAuthor(actor.id())) {
			throw ServiceException.forbidden("자기 Post만 수정할 수 있습니다.");
		}

		post.update(title, content);
		return toDetail(post);
	}

	/** 삭제는 작성자 본인과 ADMIN이 할 수 있다. */
	@Transactional
	public void delete(MemberPrincipal actor, long postId) {
		Post post = findById(postId);

		if (!post.isAuthor(actor.id()) && !actor.isAdmin()) {
			throw ServiceException.forbidden("자기 Post만 삭제할 수 있습니다.");
		}

		postRepository.delete(post);
	}

	@Transactional(readOnly = true)
	public Post findById(long id) {
		return postRepository.findWithAuthorById(id)
				.orElseThrow(() -> ServiceException.notFound("존재하지 않는 Post입니다."));
	}

	private PostDetailDto toDetail(Post post) {
		return PostDetailDto.of(post, commentRepository.countByPost_Id(post.getId()));
	}

	private Map<Long, Long> countMap(List<IdCount> counts) {
		return counts.stream().collect(Collectors.toMap(IdCount::getTargetId, IdCount::getCount, Long::sum));
	}
}
