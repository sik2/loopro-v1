import { createContext } from 'react'
import type { LoginRequest } from '@/api/auth'
import type { MemberDto } from '@/api/members'

export type AuthContextValue = {
  /** 로그인한 Member. 비로그인이거나 아직 확인 중이면 null. */
  member: MemberDto | null
  /** 토큰은 있는데 아직 내 정보를 못 받아온 상태. */
  isLoading: boolean
  login: (request: LoginRequest) => Promise<MemberDto>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
