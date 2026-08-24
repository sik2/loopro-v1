import type { MemberDto } from '@/api/members'
import { apiFetch } from '@/lib/api-client'

export type LoginRequest = {
  username: string
  password: string
}

export type LoginResponse = {
  accessToken: string
  member: MemberDto
}

/** 로그인 실패의 401은 "세션 만료"가 아니므로 전역 처리에 태우지 않는다. */
export function login(request: LoginRequest) {
  return apiFetch<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: request,
    anonymous: true,
  })
}

export function fetchMe() {
  return apiFetch<MemberDto>('/api/members/me')
}
