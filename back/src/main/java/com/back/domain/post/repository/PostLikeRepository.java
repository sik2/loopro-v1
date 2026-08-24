package com.back.domain.post.repository;

import com.back.domain.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	Optional<PostLike> findByPost_IdAndMember_Id(Long postId, Long memberId);

	boolean existsByPost_IdAndMember_Id(Long postId, Long memberId);

	long countByPost_Id(Long postId);

	@Query("""
			select l.post.id as targetId, count(l) as count
			from PostLike l
			where l.post.id in :postIds
			group by l.post.id
			""")
	List<IdCount> countByPostIds(@Param("postIds") Collection<Long> postIds);

	/** 목록에서 "내가 추천한 글"을 한 번에 알아낸다. 항목마다 exists를 날리지 않기 위함이다. */
	@Query("select l.post.id from PostLike l where l.member.id = :memberId and l.post.id in :postIds")
	List<Long> findLikedPostIds(@Param("memberId") Long memberId, @Param("postIds") Collection<Long> postIds);

	List<PostLike> findByPost_Id(Long postId);
}
