package com.back.domain.post.repository;

import com.back.domain.post.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	/** 페이징하지 않고 전부 낸다. 대댓글이 없고 스캐폴딩 단계이므로 한 번에 내려도 된다. */
	@EntityGraph(attributePaths = "author")
	List<Comment> findByPost_IdOrderByCreateDateAscIdAsc(Long postId);

	@EntityGraph(attributePaths = {"author", "post"})
	Optional<Comment> findWithAuthorAndPostById(Long id);

	long countByPost_Id(Long postId);

	@Query("""
			select c.post.id as targetId, count(c) as count
			from Comment c
			where c.post.id in :postIds
			group by c.post.id
			""")
	List<IdCount> countByPostIds(@Param("postIds") Collection<Long> postIds);

	@Query("select c.id from Comment c where c.post.id = :postId")
	List<Long> findIdsByPostId(@Param("postId") Long postId);

	/** Post가 사라질 때 딸린 Comment를 한 번에 지운다. 물리 삭제다. */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from Comment c where c.post.id = :postId")
	void deleteByPostId(@Param("postId") Long postId);
}
