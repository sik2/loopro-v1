import { Link } from 'react-router-dom'
import type { PostListItem } from '@/api/posts'
import { PostMeta } from '@/components/post/PostMeta'
import { Avatar } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { formatDateTime } from '@/lib/date'
import { toPreviewText } from '@/lib/markdown-text'
import { paths } from '@/routes/paths'

/**
 * 3열로 놓이면 카드 하나가 300~400px이다. 작성자·일시·지표를 한 줄에 다 밀어넣으면
 * 그 폭에서 줄바꿈이 제멋대로 일어나므로, 세 구역으로 나눠 쌓는다.
 */
export function PostCard({ post }: { post: PostListItem }) {
  const preview = toPreviewText(post.excerpt)

  return (
    <Link
      to={paths.postDetail(post.id)}
      className="group flex h-full flex-col gap-4 rounded-xl border border-border bg-surface p-5 transition-[background-color,border-color] hover:border-input hover:bg-surface-hover"
    >
      <div className="flex flex-col gap-2">
        <div className="flex items-start gap-2">
          <h2 className="line-clamp-2 text-[17px] leading-6 font-semibold tracking-tight text-balance">
            {post.title}
          </h2>
          {!post.published && <Badge className="mt-0.5 shrink-0">비발행</Badge>}
        </div>

        {preview && (
          <p className="line-clamp-2 text-[13px] leading-relaxed text-muted-foreground">
            {preview}
          </p>
        )}
      </div>

      <div className="mt-auto flex flex-col gap-3">
        <div className="flex min-w-0 items-center gap-2 text-[13px]">
          <Avatar nickname={post.authorNickname} className="size-6 text-[11px]" />
          <span className="truncate font-medium">{post.authorNickname}</span>
          <span className="text-muted-foreground/50">·</span>
          <span className="shrink-0 text-muted-foreground">{formatDateTime(post.createDate)}</span>
        </div>

        <PostMeta
          className="border-t border-border pt-3"
          viewCount={post.viewCount}
          likeCount={post.likeCount}
          commentCount={post.commentCount}
        />
      </div>
    </Link>
  )
}
