package com.back.domain.post.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Comment;
import com.back.domain.post.entity.CommentLike;
import com.back.domain.post.repository.CommentLikeRepository;
import com.back.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

	private final CommentLikeRepository commentLikeRepository;
	private final CommentService commentService;
	private final MemberService memberService;

	/** 이미 추천한 댓글을 다시 추천해도 기록이 늘지 않는다. */
	@Transactional
	public void like(MemberPrincipal actor, long commentId) {
		if (commentLikeRepository.existsByComment_IdAndMember_Id(commentId, actor.id())) {
			return;
		}

		Comment comment = commentService.findById(commentId);
		Member member = memberService.findById(actor.id());
		commentLikeRepository.save(CommentLike.of(comment, member));
	}

	@Transactional
	public void cancel(MemberPrincipal actor, long commentId) {
		commentService.findById(commentId);
		commentLikeRepository.findByComment_IdAndMember_Id(commentId, actor.id())
				.ifPresent(commentLikeRepository::delete);
	}
}
