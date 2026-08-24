import type { FieldError, ProblemDetail } from '@/lib/problem-detail'

/**
 * 실패 응답을 담는 예외. 상태 코드와 ProblemDetail 본문을 그대로 들고 다녀서
 * 화면이 "무엇이 왜 틀렸는지"를 항목 단위로 꺼내 쓸 수 있다.
 */
export class ApiError extends Error {
  readonly status: number
  readonly problem: ProblemDetail

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `요청이 실패했습니다. (HTTP ${status})`)
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
  }

  get fieldErrors(): FieldError[] {
    return this.problem.errors ?? []
  }

  /** react-hook-form의 setError에 그대로 흘려넣기 좋은 형태. */
  toFieldErrorMap(): Record<string, string> {
    return Object.fromEntries(this.fieldErrors.map(({ field, message }) => [field, message]))
  }
}
