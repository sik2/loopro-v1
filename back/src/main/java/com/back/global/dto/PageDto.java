package com.back.global.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Spring의 Page를 그대로 직렬화하지 않기 위한 얇은 래퍼.
 * {@code page}는 1부터 시작한다.
 */
public record PageDto<T>(
		List<T> items,
		int page,
		int size,
		long totalItems,
		int totalPages
) {

	public static <T> PageDto<T> of(Page<T> page) {
		return new PageDto<>(
				page.getContent(),
				page.getNumber() + 1,
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages()
		);
	}
}
