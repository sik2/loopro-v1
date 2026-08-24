package com.back.domain.post.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Comment;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.CommentRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final PostService postService;
	private final MemberService memberService;

	@Transactional(readOnly = true)
	public List<Comment> getListByPost(long postId) {
		// 없는 Post의 댓글을 물으면 빈 목록이 아니라 404여야 한다.
		postService.findById(postId);
		return commentRepository.findByPost_IdOrderByCreateDateAscIdAsc(postId);
	}

	@Transactional
	public Comment write(MemberPrincipal actor, long postId, String content) {
		Post post = postService.findById(postId);
		Member author = memberService.findById(actor.id());

		return commentRepository.save(Comment.write(author, post, content));
	}

	/** 수정은 작성자 본인만 가능하다. */
	@Transactional
	public Comment modify(MemberPrincipal actor, long commentId, String content) {
		Comment comment = findById(commentId);

		if (!comment.isAuthor(actor.id())) {
			throw ServiceException.forbidden("자기 Comment만 수정할 수 있습니다.");
		}

		comment.update(content);
		return comment;
	}

	/** 삭제는 작성자 본인과 ADMIN이 할 수 있다. */
	@Transactional
	public void delete(MemberPrincipal actor, long commentId) {
		Comment comment = findById(commentId);

		if (!comment.isAuthor(actor.id()) && !actor.isAdmin()) {
			throw ServiceException.forbidden("자기 Comment만 삭제할 수 있습니다.");
		}

		commentRepository.delete(comment);
	}

	@Transactional(readOnly = true)
	public Comment findById(long id) {
		return commentRepository.findWithAuthorAndPostById(id)
				.orElseThrow(() -> ServiceException.notFound("존재하지 않는 Comment입니다."));
	}
}
