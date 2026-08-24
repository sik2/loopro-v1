import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'
import { ApiError } from '@/lib/api-error'

/**
 * 서버가 준 항목별 오류를 해당 입력칸에 붙인다.
 * 항목명이 폼에 없는 오류는 폼 전체 오류(root)로 모은다.
 *
 * @returns 어느 입력칸에도 붙지 못한 메시지. 없으면 undefined.
 */
export function applyApiFieldErrors<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
  knownFields: readonly Path<T>[],
): string | undefined {
  if (!(error instanceof ApiError)) {
    return '요청에 실패했습니다. 잠시 후 다시 시도해 주세요.'
  }

  const known = new Set<string>(knownFields)
  const unattached: string[] = []

  for (const { field, message } of error.fieldErrors) {
    if (known.has(field)) {
      setError(field as Path<T>, { type: 'server', message })
    } else {
      unattached.push(message)
    }
  }

  if (error.fieldErrors.length === 0) return error.message
  return unattached.length > 0 ? unattached.join('\n') : undefined
}
