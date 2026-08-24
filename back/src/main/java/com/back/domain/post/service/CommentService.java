package com.back.domain.post.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Comment;
import com.back.domain.post.entity.Post;
import com.back.domain.post.dto.CommentDto;
import com.back.domain.post.repository.CommentLikeRepository;
import com.back.domain.post.repository.CommentRepository;
import com.back.domain.post.repository.IdCount;
import com.back.global.exception.ServiceException;
import com.back.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final CommentLikeRepository commentLikeRepository;
	private final PostService postService;
	private final MemberService memberService;

	@Transactional(readOnly = true)
	public List<CommentDto> getListByPost(long postId, MemberPrincipal viewer) {
		// 없는 Post의 댓글을 물으면 빈 목록이 아니라 404여야 한다.
		postService.findById(postId);

		List<Comment> comments = commentRepository.findByPost_IdOrderByCreateDateAscIdAsc(postId);
		List<Long> commentIds = comments.stream().map(Comment::getId).toList();

		Map<Long, Long> likeCounts = countMap(commentLikeRepository.countByCommentIds(commentIds));
		Set<Long> likedByViewer = likedCommentIds(viewer, commentIds);

		return comments.stream()
				.map(comment -> CommentDto.of(
						comment,
						likeCounts.getOrDefault(comment.getId(), 0L),
						likedByViewer.contains(comment.getId())
				))
				.toList();
	}

	@Transactional
	public CommentDto write(MemberPrincipal actor, long postId, String content) {
		Post post = postService.findById(postId);
		Member author = memberService.findById(actor.id());

		return toDto(commentRepository.save(Comment.write(author, post, content)), actor);
	}

	/** 수정은 작성자 본인만 가능하다. */
	@Transactional
	public CommentDto modify(MemberPrincipal actor, long commentId, String content) {
		Comment comment = findById(commentId);

		if (!comment.isAuthor(actor.id())) {
			throw ServiceException.forbidden("자기 Comment만 수정할 수 있습니다.");
		}

		comment.update(content);
		return toDto(comment, actor);
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

	private CommentDto toDto(Comment comment, MemberPrincipal viewer) {
		long likeCount = countMap(commentLikeRepository.countByCommentIds(List.of(comment.getId())))
				.getOrDefault(comment.getId(), 0L);
		boolean likedByMe = viewer != null
				&& commentLikeRepository.existsByComment_IdAndMember_Id(comment.getId(), viewer.id());

		return CommentDto.of(comment, likeCount, likedByMe);
	}

	private Set<Long> likedCommentIds(MemberPrincipal viewer, List<Long> commentIds) {
		if (viewer == null || commentIds.isEmpty()) {
			return Set.of();
		}
		return Set.copyOf(commentLikeRepository.findLikedCommentIds(viewer.id(), commentIds));
	}

	private Map<Long, Long> countMap(List<IdCount> counts) {
		return counts.stream().collect(Collectors.toMap(IdCount::getTargetId, IdCount::getCount, Long::sum));
	}
}
