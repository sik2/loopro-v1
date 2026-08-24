import { Heart } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

/**
 * 추천 버튼. 내가 이미 추천했는지를 그대로 반영한다.
 * 로그인하지 않았으면 누를 수 없고, 왜 못 누르는지 알려준다.
 */
export function LikeButton({
  count,
  likedByMe,
  canLike,
  isPending,
  onToggle,
  size = 'default',
}: {
  count: number
  likedByMe: boolean
  canLike: boolean
  isPending: boolean
  onToggle: () => void
  size?: 'default' | 'sm'
}) {
  return (
    <Button
      type="button"
      variant={likedByMe ? 'secondary' : 'outline'}
      size={size}
      disabled={isPending}
      aria-pressed={likedByMe}
      title={canLike ? undefined : '추천하려면 로그인하세요.'}
      onClick={onToggle}
    >
      <Heart className={cn(likedByMe && 'fill-current')} aria-hidden />
      {count}
    </Button>
  )
}
