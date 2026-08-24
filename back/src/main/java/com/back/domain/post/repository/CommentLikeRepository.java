package com.back.domain.post.repository;

import com.back.domain.post.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

	Optional<CommentLike> findByComment_IdAndMember_Id(Long commentId, Long memberId);

	boolean existsByComment_IdAndMember_Id(Long commentId, Long memberId);

	@Query("""
			select l.comment.id as targetId, count(l) as count
			from CommentLike l
			where l.comment.id in :commentIds
			group by l.comment.id
			""")
	List<IdCount> countByCommentIds(@Param("commentIds") Collection<Long> commentIds);

	@Query("""
			select l.comment.id
			from CommentLike l
			where l.member.id = :memberId and l.comment.id in :commentIds
			""")
	List<Long> findLikedCommentIds(@Param("memberId") Long memberId,
	                               @Param("commentIds") Collection<Long> commentIds);

	/** Comment가 사라질 때 그 Comment의 추천 기록을 한 번에 지운다. */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from CommentLike l where l.comment.id in :commentIds")
	void deleteByCommentIds(@Param("commentIds") Collection<Long> commentIds);

	long countByComment_IdIn(Collection<Long> commentIds);
}
