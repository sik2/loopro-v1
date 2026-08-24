import { ApiError } from '@/lib/api-error'
import { env } from '@/lib/env'
import { isProblemDetail, type ProblemDetail } from '@/lib/problem-detail'

type ApiRequest = Omit<RequestInit, 'body'> & {
  body?: unknown
}

/**
 * 공통 API 클라이언트.
 * 성공하면 순수 DTO를 그대로 돌려주고, 실패하면 ProblemDetail을 읽어 ApiError로 던진다.
 * 성공/실패 판별은 래퍼 필드가 아니라 HTTP 상태 코드로 한다.
 */
export async function apiFetch<T>(path: string, { body, headers, ...init }: ApiRequest = {}): Promise<T> {
  const response = await fetch(`${env.apiBaseUrl}${path}`, {
    ...init,
    headers: {
      ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (!response.ok) {
    throw new ApiError(response.status, await readProblemDetail(response))
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

async function readProblemDetail(response: Response): Promise<ProblemDetail> {
  const fallback: ProblemDetail = {
    status: response.status,
    title: response.statusText,
    detail: `요청이 실패했습니다. (HTTP ${response.status})`,
  }

  try {
    const parsed: unknown = await response.json()
    return isProblemDetail(parsed) ? { ...fallback, ...parsed } : fallback
  } catch {
    // 본문이 비었거나 JSON이 아닌 경우(프록시 에러 페이지 등).
    return fallback
  }
}
