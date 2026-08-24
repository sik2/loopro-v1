package com.back.global.exception;

import org.springframework.dao.DataIntegrityViolationException;
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
 * 항목별 오류가 있으면 {@code errors}에 항목명과 메시지의 쌍으로 담는다.
 */
@RestControllerAdvice
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
	 * 서비스 계층의 중복 검사와 DB 유일 제약 사이의 경합에서 지는 요청.
	 * 어느 항목이 부딪혔는지는 알 수 없으므로 항목별 오류 없이 409만 낸다.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		return problem(HttpStatus.CONFLICT, "이미 사용 중인 값입니다.", List.of());
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
