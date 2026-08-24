package com.back.domain.post.entity;

import com.back.domain.member.entity.Member;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Member가 작성한 글. 교안도 Post다 — 교안과 일반 글을 구분하는 별도 개념은 없다(ADR-0001).
 */
@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

	@Column(nullable = false, length = 200)
	private String title;

	/** 마크다운 텍스트. back은 해석하지 않는다. 렌더링은 front의 책임이다. */
	@Lob
	@Column(nullable = false)
	private String content;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	private Member author;

	private Post(String title, String content, Member author) {
		this.title = title;
		this.content = content;
		this.author = author;
	}

	public static Post write(Member author, String title, String content) {
		return new Post(title, content, author);
	}

	public boolean isAuthor(Long memberId) {
		return author.getId().equals(memberId);
	}
}
