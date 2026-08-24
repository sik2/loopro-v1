package com.back.global.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 공통 상위 클래스. 식별자와 작성일·수정일을 함께 갖는다.
 * 작성일·수정일은 {@code @EnableJpaAuditing}이 켜져 있어야 채워진다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createDate;

	@LastModifiedDate
	private LocalDateTime modifyDate;

	/**
	 * 식별자 기준으로 비교한다. {@code getClass()} 비교를 쓰지 않는 이유는
	 * Hibernate lazy proxy의 실제 클래스가 엔티티 클래스의 하위 타입이라
	 * 같은 행을 가리키는 두 인스턴스가 서로 다르다고 판정되기 때문이다.
	 */
	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BaseEntity other)) return false;
		return id != null && id.equals(other.getId());
	}

	/**
	 * 상수를 반환한다. 식별자는 영속화 시점에 채워지므로,
	 * 식별자로 해시를 만들면 컬렉션에 담은 뒤 해시가 바뀌어 원소를 잃는다.
	 */
	@Override
	public final int hashCode() {
		return 31;
	}
}
