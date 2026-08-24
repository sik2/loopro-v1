package com.back.global.initdata;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 시작 시 샘플 데이터를 만든다.
 *
 * <p>실제 작업은 별도 빈에 있다. 같은 클래스 안에서 부르면 프록시를 타지 않아
 * {@code @Transactional}이 걸리지 않기 때문이다.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class BaseInitDataRunner implements ApplicationRunner {

	private final BaseInitDataService baseInitDataService;

	@Override
	public void run(ApplicationArguments args) {
		baseInitDataService.run();
	}
}
