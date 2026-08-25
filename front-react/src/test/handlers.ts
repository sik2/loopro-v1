import { HttpResponse, http } from 'msw'
import { comment, GUREUM, listItem, postDetail } from '@/test/fixtures'

const API = 'http://localhost:8080'

/**
 * 기본 응답. 개별 테스트는 `server.use(...)`로 필요한 것만 덮어쓴다.
 *
 * <p>여기서 흉내 내는 것은 back의 <b>계약</b>이다 — 상태 코드와 ProblemDetail 모양.
 * 그 계약이 실제로 지켜지는지는 back의 HTTP seam 테스트가 본다.
 */
export const handlers = [
  http.get(`${API}/api/posts`, () =>
    HttpResponse.json({ items: [listItem()], page: 1, size: 10, totalItems: 1, totalPages: 1 }),
  ),
  http.get(`${API}/api/posts/:id`, () => HttpResponse.json(postDetail())),
  http.get(`${API}/api/posts/:id/comments`, () => HttpResponse.json([comment()])),
  http.get(`${API}/api/members/me`, () => HttpResponse.json(GUREUM)),
]

/** 에러 응답은 back과 같은 RFC 9457 모양이어야 front가 읽을 수 있다. */
export function problem(status: number, detail: string, errors?: { field: string; message: string }[]) {
  return HttpResponse.json(
    { status, title: 'Error', detail, ...(errors ? { errors } : {}) },
    { status },
  )
}
