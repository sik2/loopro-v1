package com.back.domain.post.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.entity.PostLike;
import com.back.domain.post.repository.PostLikeRepository;
import com.back.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

	private final PostLikeRepository postLikeRepository;
	private final PostService postService;
	private final MemberService memberService;

	/** 이미 추천한 글을 다시 추천해도 기록이 늘지 않는다. DB 유일 제약이 마지막 방어선이다. */
	@Transactional
	public void like(MemberPrincipal actor, long postId) {
		if (postLikeRepository.existsByPost_IdAndMember_Id(postId, actor.id())) {
			return;
		}

		Post post = postService.findById(postId);
		Member member = memberService.findById(actor.id());
		postLikeRepository.save(PostLike.of(post, member));
	}

	/** 추천을 취소한다. 추천한 적이 없으면 아무 일도 일어나지 않는다. */
	@Transactional
	public void cancel(MemberPrincipal actor, long postId) {
		postService.findById(postId);
		postLikeRepository.findByPost_IdAndMember_Id(postId, actor.id()).ifPresent(postLikeRepository::delete);
	}
}
