package com.back.domain.post.repository;

import com.back.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

	/**
	 * OSIV가 꺼져 있으므로 작성자 Nickname은 조회 시점에 함께 읽어와야 한다.
	 * 목록에서 작성자별로 추가 쿼리가 나가는 것도 막는다.
	 */
	@EntityGraph(attributePaths = "author")
	Page<Post> findAllBy(Pageable pageable);

	@EntityGraph(attributePaths = "author")
	Optional<Post> findWithAuthorById(Long id);
}
