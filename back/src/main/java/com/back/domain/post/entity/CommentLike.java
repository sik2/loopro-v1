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
 * Member가 Comment에 표시한 추천.
 *
 * <p>{@link PostLike}와 별도 엔티티다. 대상이 다르면 유일 제약도 외래키도 달라야 하므로,
 * 하나의 다형 테이블로 합치지 않는다.
 */
@Entity
@Table(
		name = "comment_like",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_comment_like_comment_member",
				columnNames = {"comment_id", "member_id"}
		)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "comment_id", nullable = false)
	private Comment comment;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	private CommentLike(Comment comment, Member member) {
		this.comment = comment;
		this.member = member;
	}

	public static CommentLike of(Comment comment, Member member) {
		return new CommentLike(comment, member);
	}
}
