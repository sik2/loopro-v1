import { ApiError } from '@/lib/api-error'
import { env } from '@/lib/env'
import { isProblemDetail, type ProblemDetail } from '@/lib/problem-detail'
import { tokenStore } from '@/lib/token-store'
import { notifyUnauthorized } from '@/lib/unauthorized'

type ApiRequest = Omit<RequestInit, 'body'> & {
  body?: unknown
  /**
   * 토큰을 싣지 않고, 401을 받아도 "세션이 끊겼다"고 보지 않는다.
   * 로그인 요청처럼 401이 정상적인 실패인 경우에 쓴다.
   */
  anonymous?: boolean
}

/**
 * 공통 API 클라이언트.
 * 성공하면 순수 DTO를 그대로 돌려주고, 실패하면 ProblemDetail을 읽어 ApiError로 던진다.
 * 성공/실패 판별은 래퍼 필드가 아니라 HTTP 상태 코드로 한다.
 */
export async function apiFetch<T>(
  path: string,
  { body, headers, anonymous = false, ...init }: ApiRequest = {},
): Promise<T> {
  const token = anonymous ? null : tokenStore.read()

  const response = await fetch(`${env.apiBaseUrl}${path}`, {
    ...init,
    headers: {
      ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (!response.ok) {
    // 만료됐거나 서명이 맞지 않는 토큰. 들고 있어봐야 계속 401이므로 버린다.
    if (response.status === 401 && !anonymous) {
      tokenStore.clear()
      notifyUnauthorized()
    }
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
