import { apiFetch } from '@/lib/api-client'

export type Role = 'USER' | 'ADMIN'

/** Member의 공개 표현. Username은 화면에 노출하지 않으므로 담기지 않는다. */
export type MemberDto = {
  id: number
  nickname: string
  role: Role
  createDate: string
}

export type SignupRequest = {
  username: string
  password: string
  nickname: string
}

export function signup(request: SignupRequest) {
  return apiFetch<MemberDto>('/api/members', { method: 'POST', body: request })
}
