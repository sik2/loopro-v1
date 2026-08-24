package com.back.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.List;

/**
 * 모든 에러 응답을 RFC 9457 ProblemDetail로 통일한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ServiceException.class)
	public ProblemDetail handleServiceException(ServiceException ex) {
		return problem(ex.getStatus(), ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		List<FieldErrorDto> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(this::toFieldError)
				.sorted(Comparator.comparing(FieldErrorDto::field).thenComparing(FieldErrorDto::message))
				.toList();

		ProblemDetail problemDetail = problem(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다.");
		problemDetail.setProperty("errors", errors);
		return problemDetail;
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ProblemDetail handleNoResource(NoResourceFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다.");
	}

	private FieldErrorDto toFieldError(FieldError fieldError) {
		String message = fieldError.getDefaultMessage();
		return new FieldErrorDto(fieldError.getField(), message == null ? "올바르지 않은 값입니다." : message);
	}

	private ProblemDetail problem(HttpStatus status, String detail) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(status.getReasonPhrase());
		return problemDetail;
	}
}
