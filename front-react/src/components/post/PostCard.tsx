import { Link } from 'react-router-dom'
import type { PostListItem } from '@/api/posts'
import { PostMeta } from '@/components/post/PostMeta'
import { Avatar } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { formatDateTime } from '@/lib/date'
import { toPreviewText } from '@/lib/markdown-text'
import { paths } from '@/routes/paths'

export function PostCard({ post }: { post: PostListItem }) {
  const preview = toPreviewText(post.excerpt)

  return (
    <Link
      to={paths.postDetail(post.id)}
      className="group flex flex-col gap-4 rounded-xl border border-border bg-surface p-6 transition-[background-color,border-color] hover:border-input hover:bg-surface-hover"
    >
      <div className="flex flex-col gap-2">
        <div className="flex items-start gap-2">
          <h2 className="line-clamp-2 text-[18px] leading-7 font-semibold tracking-tight text-balance">
            {post.title}
          </h2>
          {!post.published && <Badge className="mt-1 shrink-0">비발행</Badge>}
        </div>

        {preview && (
          <p className="line-clamp-2 text-sm leading-relaxed text-muted-foreground">{preview}</p>
        )}
      </div>

      <div className="flex flex-wrap items-center gap-x-2.5 gap-y-2 text-[13px]">
        <Avatar nickname={post.authorNickname} className="size-6 text-[11px]" />
        <span className="font-medium">{post.authorNickname}</span>
        <span className="text-muted-foreground/50">·</span>
        <span className="text-muted-foreground">{formatDateTime(post.createDate)}</span>

        <PostMeta
          className="ml-auto"
          viewCount={post.viewCount}
          likeCount={post.likeCount}
          commentCount={post.commentCount}
        />
      </div>
    </Link>
  )
}
