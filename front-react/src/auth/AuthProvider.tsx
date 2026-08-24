import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchMe, login as loginRequest, type LoginRequest } from '@/api/auth'
import { AuthContext, type AuthContextValue } from '@/auth/auth-context'
import { tokenStore } from '@/lib/token-store'
import { setUnauthorizedHandler } from '@/lib/unauthorized'
import { paths } from '@/routes/paths'

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [token, setToken] = useState<string | null>(() => tokenStore.read())

  useEffect(() => tokenStore.subscribe(setToken), [])

  // 401을 받으면 토큰은 이미 버려진 상태다. 남은 건 화면을 정리하는 일뿐이다.
  useEffect(() => {
    setUnauthorizedHandler(() => {
      queryClient.clear()
      navigate(paths.login, { replace: true })
    })
    return () => setUnauthorizedHandler(undefined)
  }, [navigate, queryClient])

  const meQuery = useQuery({
    queryKey: ['me', token],
    queryFn: fetchMe,
    enabled: token !== null,
    retry: false,
  })

  const login = useCallback(
    async (request: LoginRequest) => {
      const { accessToken, member } = await loginRequest(request)
      tokenStore.write(accessToken)
      queryClient.setQueryData(['me', accessToken], member)
      return member
    },
    [queryClient],
  )

  const logout = useCallback(() => {
    // 서버는 토큰을 무효화하지 않는다. 로그아웃은 저장된 토큰을 버리는 것이 전부다.
    tokenStore.clear()
    queryClient.clear()
    navigate(paths.postList, { replace: true })
  }, [navigate, queryClient])

  const value = useMemo<AuthContextValue>(
    () => ({
      member: meQuery.data ?? null,
      isLoading: token !== null && meQuery.isPending,
      login,
      logout,
    }),
    [login, logout, meQuery.data, meQuery.isPending, token],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
