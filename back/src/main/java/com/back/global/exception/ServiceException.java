package com.back.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 서비스 계층이 던지는 전용 예외. 상태 코드를 함께 담고,
 * {@link GlobalExceptionHandler}가 RFC 9457 ProblemDetail로 변환한다.
 */
@Getter
public class ServiceException extends RuntimeException {

	private final HttpStatus status;

	public ServiceException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public static ServiceException badRequest(String message) {
		return new ServiceException(HttpStatus.BAD_REQUEST, message);
	}

	public static ServiceException unauthorized(String message) {
		return new ServiceException(HttpStatus.UNAUTHORIZED, message);
	}

	public static ServiceException forbidden(String message) {
		return new ServiceException(HttpStatus.FORBIDDEN, message);
	}

	public static ServiceException notFound(String message) {
		return new ServiceException(HttpStatus.NOT_FOUND, message);
	}

	public static ServiceException conflict(String message) {
		return new ServiceException(HttpStatus.CONFLICT, message);
	}
}
