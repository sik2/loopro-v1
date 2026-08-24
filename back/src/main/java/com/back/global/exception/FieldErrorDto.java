package com.back.global.exception;

/**
 * 검증 실패 한 건. 항목명과 메시지의 쌍이며, front의 폼 검증과 1:1로 맞물린다.
 */
public record FieldErrorDto(String field, String message) {
}
