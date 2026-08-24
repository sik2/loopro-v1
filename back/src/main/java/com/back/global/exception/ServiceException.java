package com.back.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * 서비스 계층이 던지는 전용 예외. 상태 코드를 함께 담고,
 * {@link GlobalExceptionHandler}가 RFC 9457 ProblemDetail로 변환한다.
 *
 * <p>거부 사유가 특정 입력 항목에 붙는 경우(중복된 Username 등) {@code errors}에 항목명을 담는다.
 * 그래야 front가 형식 검증 실패와 똑같은 방식으로 해당 입력칸에 메시지를 붙일 수 있다.
 */
@Getter
public class ServiceException extends RuntimeException {

	private final HttpStatus status;
	private final List<FieldErrorDto> errors;

	public ServiceException(HttpStatus status, String message) {
		this(status, message, List.of());
	}

	public ServiceException(HttpStatus status, String message, List<FieldErrorDto> errors) {
		super(message);
		this.status = status;
		this.errors = List.copyOf(errors);
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

	/** 특정 입력 항목 때문에 거부하는 경우. */
	public static ServiceException conflictOnField(String field, String message) {
		return new ServiceException(HttpStatus.CONFLICT, message, List.of(new FieldErrorDto(field, message)));
	}
}
