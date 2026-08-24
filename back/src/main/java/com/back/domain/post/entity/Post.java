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

	/**
	 * 상세 조회 요청이 발생한 횟수. 같은 사람의 반복 조회를 구분하지 않으므로
	 * 고유 독자 수가 아니다.
	 */
	@Column(nullable = false)
	private long viewCount;

	private Post(String title, String content, Member author) {
		this.title = title;
		this.content = content;
		this.author = author;
		this.viewCount = 0L;
	}

	public static Post write(Member author, String title, String content) {
		return new Post(title, content, author);
	}

	/** 상세를 열 때마다 무조건 1 올린다. 그래서 상세 조회가 쓰기 트랜잭션이 된다. */
	public void increaseViewCount() {
		this.viewCount++;
	}

	public void update(String title, String content) {
		this.title = title;
		this.content = content;
	}

	public boolean isAuthor(Long memberId) {
		return author.getId().equals(memberId);
	}
}
