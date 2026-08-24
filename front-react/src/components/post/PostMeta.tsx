import { Eye, Heart, MessageSquare } from 'lucide-react'

/**
 * 조회·추천·댓글수 한 줄. 숫자 셋이 같은 무게로 나란히 서면 시선을 끌기만 하므로
 * 아이콘과 숫자 모두 본문보다 한 단계 낮춘다.
 */
export function PostMeta({
  viewCount,
  likeCount,
  commentCount,
  className,
}: {
  viewCount: number
  likeCount: number
  commentCount: number
  className?: string
}) {
  return (
    <div className={className}>
      <ul className="flex items-center gap-4 text-[13px] text-muted-foreground tabular-nums">
        <Metric icon={<Eye aria-hidden />} label="조회" value={viewCount} />
        <Metric icon={<Heart aria-hidden />} label="추천" value={likeCount} />
        <Metric icon={<MessageSquare aria-hidden />} label="댓글" value={commentCount} />
      </ul>
    </div>
  )
}

function Metric({ icon, label, value }: { icon: React.ReactNode; label: string; value: number }) {
  return (
    <li className="flex items-center gap-1.5">
      <span className="[&_svg]:size-3.5 [&_svg]:opacity-70">{icon}</span>
      <span className="sr-only">{label}</span>
      {value}
    </li>
  )
}
