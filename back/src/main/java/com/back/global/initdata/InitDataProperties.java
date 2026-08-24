package com.back.global.initdata;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 샘플 계정의 비밀번호.
 *
 * <p>비어 있으면 샘플 데이터를 만들지 않는다. 기본값을 두면 배포 환경에서
 * 그 값이 그대로 살아나는데, 이 저장소는 공개돼 있어 그게 곧 뚫린 관리자 계정이 된다.
 */
@ConfigurationProperties(prefix = "app.init-data")
public record InitDataProperties(String password) {

	public boolean hasPassword() {
		return password != null && !password.isBlank();
	}
}
