/**
 * back이 내려주는 에러 응답의 형태. RFC 9457 ProblemDetail이며,
 * 검증 실패인 경우 항목명과 메시지의 쌍이 errors에 담긴다.
 */
export type FieldError = {
  field: string
  message: string
}

export type ProblemDetail = {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  errors?: FieldError[]
}

export function isProblemDetail(value: unknown): value is ProblemDetail {
  return typeof value === 'object' && value !== null
}
