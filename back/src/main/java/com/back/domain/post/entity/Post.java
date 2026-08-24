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
	/**
	 * 긴 텍스트. {@code @Lob}을 쓰면 PostgreSQL에서 oid(large object) 컬럼으로 떨어져
	 * 트랜잭션 밖 읽기가 깨진다.
	 *
	 * <p>{@code LONGVARCHAR}는 PostgreSQL에서 {@code varchar(32600)}이 되어
	 * 3만 자쯤에서 DB가 거절한다. 그러면 사용자에게는 길이 문제가 아니라
	 * 제약 위반(409)으로 보인다. 길이 제한은 DB가 아니라 입력 검증이 해야 하므로
	 * 컬럼은 {@code LONG32VARCHAR}(PostgreSQL의 {@code text})로 열어둔다.
	 */
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
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

	/**
	 * 작성자 외의 사람에게 보이는 상태. 이 플랫폼의 유일한 공개 범위 개념이다 —
	 * 지정 공유나 링크 공유는 없다.
	 */
	@Column(nullable = false)
	private boolean published;

	private Post(String title, String content, Member author, boolean published) {
		this.title = title;
		this.content = content;
		this.author = author;
		this.published = published;
		this.viewCount = 0L;
	}

	public static Post write(Member author, String title, String content, boolean published) {
		return new Post(title, content, author, published);
	}

	/** 작성자 본인이 아니면 발행되지 않은 글은 없는 것으로 다룬다. */
	public boolean isVisibleTo(Long memberId) {
		return published || (memberId != null && isAuthor(memberId));
	}

	/**
	 * 목록 미리보기에 쓸 본문 앞부분. <b>마크다운 원문을 그대로 잘라서</b> 준다.
	 *
	 * <p>여기서 `#`이나 코드 울타리를 걷어내면 그게 곧 마크다운 해석이다.
	 * back은 해석하지 않는다(ADR-0003) — 기호를 벗기는 일은 그리는 쪽의 몫이다.
	 * 이 메서드가 하는 일은 문자열 자르기뿐이다.
	 *
	 * <p>글자 수는 코드 포인트로 센다. char로 자르면 이모지 같은 서로게이트 쌍이
	 * 반토막 나서 깨진 문자가 남는다.
	 */
	public String excerpt(int maxLength) {
		int length = content.codePointCount(0, content.length());
		if (length <= maxLength) {
			return content;
		}
		return content.substring(0, content.offsetByCodePoints(0, maxLength));
	}

	/** 상세를 열 때마다 무조건 1 올린다. 그래서 상세 조회가 쓰기 트랜잭션이 된다. */
	public void increaseViewCount() {
		this.viewCount++;
	}

	public void update(String title, String content, boolean published) {
		this.title = title;
		this.content = content;
		this.published = published;
	}

	public boolean isAuthor(Long memberId) {
		return author.getId().equals(memberId);
	}
}
