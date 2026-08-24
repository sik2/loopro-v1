package com.back.domain.post.repository;

/**
 * "대상 하나당 몇 건" 집계 결과. 목록에서 항목마다 count 쿼리를 날리지 않기 위해 쓴다.
 */
public interface IdCount {

	Long getTargetId();

	long getCount();
}
