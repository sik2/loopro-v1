import { cn } from '@/lib/utils'

/**
 * Member에는 프로필 이미지가 없다. Nickname의 첫 글자로 대신한다.
 *
 * <p>색을 여섯 가지쯤 돌려쓰면 화면이 알록달록해진다. 중립 배경에 옅은 테두리만 두고,
 * 사람을 구분하는 일은 옆에 적힌 Nickname에 맡긴다.
 */
export function Avatar({
  nickname,
  className,
}: {
  nickname: string
  className?: string
}) {
  return (
    <span
      aria-hidden
      className={cn(
        'inline-flex size-8 shrink-0 items-center justify-center rounded-full',
        'bg-surface-strong text-[13px] font-semibold text-muted-foreground',
        'ring-1 ring-border ring-inset',
        className,
      )}
    >
      {[...nickname][0] ?? '?'}
    </span>
  )
}
