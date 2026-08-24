import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/auth/use-auth'
import { paths } from '@/routes/paths'

/** 로그인해야 볼 수 있는 화면을 감싼다. */
export function RequireAuth({ children }: { children: ReactNode }) {
  const { member, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <p className="text-sm text-muted-foreground">불러오는 중…</p>
  }

  if (!member) {
    return <Navigate to={paths.login} replace state={{ from: location.pathname }} />
  }

  return children
}
