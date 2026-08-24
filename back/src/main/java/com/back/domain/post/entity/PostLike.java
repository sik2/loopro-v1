package com.back.domain.post.entity;

import com.back.domain.member.entity.Member;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Member가 Post에 표시한 추천.
 *
 * <p>값을 세는 counter가 아니라 <b>누가 무엇을 추천했는지의 기록</b>이다.
 * 그래서 "내가 이 글을 추천했는지"에 답할 수 있고, 취소가 기록의 삭제로 표현된다.
 */
@Entity
@Table(
		name = "post_like",
		uniqueConstraints = @UniqueConstraint(name = "uk_post_like_post_member", columnNames = {"post_id", "member_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	private PostLike(Post post, Member member) {
		this.post = post;
		this.member = member;
	}

	public static PostLike of(Post post, Member member) {
		return new PostLike(post, member);
	}
}
