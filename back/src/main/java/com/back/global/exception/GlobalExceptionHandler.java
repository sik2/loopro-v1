package com.back.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.List;

/**
 * 모든 에러 응답을 RFC 9457 ProblemDetail로 통일한다.
 * 항목별 오류가 있으면 {@code errors}에 항목명과 메시지의 쌍으로 담는다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(ServiceException.class)
	public ProblemDetail handleServiceException(ServiceException ex) {
		return problem(ex.getStatus(), ex.getMessage(), ex.getErrors());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		List<FieldErrorDto> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(this::toFieldError)
				.sorted(Comparator.comparing(FieldErrorDto::field).thenComparing(FieldErrorDto::message))
				.toList();

		return problem(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다.", errors);
	}

	/**
	 * 요청 본문을 아예 읽을 수 없는 경우(깨진 JSON, 빈 본문).
	 *
	 * <p>이걸 직접 처리하지 않으면 Spring이 /error로 넘기고, 보안 필터가 그걸 인증 실패로
	 * 처리해 401이 나간다. front는 401을 세션 만료로 읽으므로 멀쩡히 로그인한 사람이
	 * 오타 한 번에 로그아웃된다.
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
		return problem(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다.", List.of());
	}

	/** 경로나 파라미터의 타입이 안 맞는 경우(`/api/posts/abc`). 위와 같은 이유로 직접 처리한다. */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return problem(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다.", List.of());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex) {
		return problem(HttpStatus.BAD_REQUEST, "필수 요청 값이 빠졌습니다.", List.of());
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ProblemDetail handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
		return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 형식입니다.", List.of());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		return problem(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않는 요청 방식입니다.", List.of());
	}

	/**
	 * DB 제약을 건드린 요청. 서비스 계층의 중복 검사와 유일 제약 사이의 경합이 대표적이다.
	 *
	 * <p>무엇이 부딪혔는지 응답으로 알려주지 않는다 — 제약 이름이나 컬럼명이 새면
	 * 스키마를 그려볼 재료가 된다. 원인은 서버 로그에만 남긴다.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		log.warn("DB 제약 위반", ex);
		return problem(HttpStatus.CONFLICT, "요청을 처리할 수 없습니다. 입력값을 확인해 주세요.", List.of());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ProblemDetail handleNoResource(NoResourceFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다.", List.of());
	}

	private FieldErrorDto toFieldError(FieldError fieldError) {
		String message = fieldError.getDefaultMessage();
		return new FieldErrorDto(fieldError.getField(), message == null ? "올바르지 않은 값입니다." : message);
	}

	private ProblemDetail problem(HttpStatus status, String detail, List<FieldErrorDto> errors) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(status.getReasonPhrase());
		if (!errors.isEmpty()) {
			problemDetail.setProperty("errors", errors);
		}
		return problemDetail;
	}
}
