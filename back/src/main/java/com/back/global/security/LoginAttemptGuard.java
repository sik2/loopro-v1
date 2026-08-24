package com.back.global.security;

import com.back.global.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 로그인 실패를 세어 무차별 대입을 늦춘다.
 *
 * <p>아이디와 IP를 <b>따로</b> 센다. 아이디만 세면 공격자가 아이디를 바꿔가며 흔한
 * 비밀번호를 뿌리는 방식(password spraying)이 그대로 통하고, IP만 세면 여러 곳에서
 * 한 계정을 노리는 경우를 놓친다.
 *
 * <p><b>이 구현의 한계</b>: 카운터가 프로세스 메모리에 있다. 인스턴스가 여러 개가 되면
 * 각자 세므로 실효 임계값이 인스턴스 수만큼 늘어나고, 재배포하면 초기화된다. 지금은
 * 단일 인스턴스라 성립하며, 늘릴 때는 Redis 같은 공용 저장소로 옮겨야 한다.
 */
@Component
@Slf4j
public class LoginAttemptGuard {

	/** 이 횟수만큼 연속 실패하면 잠근다. */
	private static final int MAX_FAILURES = 10;

	/** 마지막 실패로부터 이 시간이 지나면 카운터를 잊는다. */
	private static final Duration WINDOW = Duration.ofMinutes(10);

	/**
	 * 카운터 개수 상한. 공격자가 값을 계속 바꾸면 항목이 무한히 늘 수 있으므로,
	 * 넘어서면 만료된 것부터 비우고 그래도 넘치면 전부 버린다. 잠금을 잃는 것이
	 * 메모리를 잃는 것보다 낫다.
	 */
	private static final int MAX_ENTRIES = 20_000;

	private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

	public void checkNotLocked(String username, String clientIp) {
		if (isLocked(usernameKey(username)) || isLocked(ipKey(clientIp))) {
			throw new ServiceException(
					HttpStatus.TOO_MANY_REQUESTS,
					"로그인 시도가 너무 많습니다. 잠시 후 다시 시도해 주세요.");
		}
	}

	public void recordFailure(String username, String clientIp) {
		evictIfCrowded();
		count(usernameKey(username));
		count(ipKey(clientIp));
	}

	public void recordSuccess(String username, String clientIp) {
		attempts.remove(usernameKey(username));
		attempts.remove(ipKey(clientIp));
	}

	private boolean isLocked(String key) {
		Attempts a = attempts.get(key);
		if (a == null) {
			return false;
		}
		if (a.isExpired()) {
			attempts.remove(key);
			return false;
		}
		return a.count.get() >= MAX_FAILURES;
	}

	private void count(String key) {
		attempts.compute(key, (k, a) -> {
			if (a == null || a.isExpired()) {
				return new Attempts();
			}
			a.count.incrementAndGet();
			a.lastFailure = Instant.now();
			return a;
		});
	}

	/**
	 * 카운터를 전부 비운다.
	 *
	 * <p>이 상태는 DB가 아니라 프로세스에 있어서 트랜잭션 롤백으로 지워지지 않는다.
	 * 한 테스트의 로그인 실패가 다음 테스트로 새므로 테스트마다 호출한다.
	 */
	public void reset() {
		attempts.clear();
	}

	private void evictIfCrowded() {
		if (attempts.size() < MAX_ENTRIES) {
			return;
		}
		attempts.values().removeIf(Attempts::isExpired);
		if (attempts.size() >= MAX_ENTRIES) {
			log.warn("로그인 시도 카운터가 한도({})를 넘어 전부 비웁니다.", MAX_ENTRIES);
			attempts.clear();
		}
	}

	/** 아이디와 IP가 우연히 같은 문자열이어도 서로 섞이지 않게 접두사를 붙인다. */
	private String usernameKey(String username) {
		return "u:" + username;
	}

	private String ipKey(String clientIp) {
		return "i:" + clientIp;
	}

	private static final class Attempts {
		private final AtomicInteger count = new AtomicInteger(1);
		private volatile Instant lastFailure = Instant.now();

		boolean isExpired() {
			return lastFailure.plus(WINDOW).isBefore(Instant.now());
		}
	}
}
