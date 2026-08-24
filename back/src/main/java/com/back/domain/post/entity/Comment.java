package com.back.domain.post.entity;

import com.back.domain.member.entity.Member;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Post에 달린 Member의 답글.
 *
 * <p>Comment를 가리키는 필드가 없다 — Comment에는 Comment를 달 수 없다.
 * 대댓글을 만들려면 여기에 자기 참조를 넣어야 하므로, 없다는 사실이 구조로 드러난다.
 */
@Entity
@Table(name = "post_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

	/**
	 * 긴 텍스트. {@code @Lob}을 쓰면 PostgreSQL에서 oid(large object) 컬럼으로 떨어져
	 * 트랜잭션 밖 읽기가 깨진다. LONGVARCHAR로 못 박으면 PostgreSQL은 text,
	 * H2는 VARCHAR가 되어 두 DB에서 같은 뜻이 된다.
	 */
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(nullable = false)
	private String content;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	private Member author;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	private Comment(String content, Member author, Post post) {
		this.content = content;
		this.author = author;
		this.post = post;
	}

	public static Comment write(Member author, Post post, String content) {
		return new Comment(content, author, post);
	}

	public void update(String content) {
		this.content = content;
	}

	public boolean isAuthor(Long memberId) {
		return author.getId().equals(memberId);
	}
}
