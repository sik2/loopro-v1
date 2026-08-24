import type { ReactNode } from 'react'
import { ApiError } from '@/lib/api-error'

/** 목록·상세 화면이 공유하는 로딩/에러 표시. */
export function QueryState({
  isPending,
  error,
  children,
}: {
  isPending: boolean
  error: unknown
  children: ReactNode
}) {
  if (isPending) {
    return <p className="text-sm text-muted-foreground">불러오는 중…</p>
  }

  if (error) {
    const message =
      error instanceof ApiError ? error.message : '불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
    return (
      <p role="alert" className="text-sm text-destructive">
        {message}
      </p>
    )
  }

  return children
}
