import { useContext } from 'react'
import { AuthContext, type AuthContextValue } from '@/auth/auth-context'

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (!value) {
    throw new Error('useAuth는 AuthProvider 안에서만 쓸 수 있습니다.')
  }
  return value
}
