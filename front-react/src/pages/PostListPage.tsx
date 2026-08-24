import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { fetchPostList, type PostListItem } from '@/api/posts'
import { PostMeta } from '@/components/post/PostMeta'
import { QueryState } from '@/components/QueryState'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatDateTime } from '@/lib/date'
import { paths } from '@/routes/paths'

const PAGE_SIZE = 10

/**
 * 목록은 "무엇을 읽을지 고르는" 화면이다. 박스를 겹겹이 두르면 스크롤할 때
 * 테두리를 세게 되므로, 얇은 구분선만 두고 제목이 무게를 다 갖게 한다.
 */
export function PostListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Math.max(Number(searchParams.get('page') ?? '1') || 1, 1)

  const { data, isPending, error } = useQuery({
    queryKey: ['posts', page],
    queryFn: () => fetchPostList(page, PAGE_SIZE),
    placeholderData: keepPreviousData,
  })

  return (
    <div className="flex flex-col gap-10">
      <header className="flex items-baseline justify-between gap-4">
        <h1 className="text-[26px] font-semibold tracking-tight">글</h1>
        {data && (
          <p className="text-[13px] text-muted-foreground tabular-nums">{data.totalItems}개</p>
        )}
      </header>

      <QueryState isPending={isPending} error={error}>
        {data && data.items.length === 0 ? (
          <p className="py-20 text-center text-sm text-muted-foreground">아직 글이 없습니다.</p>
        ) : (
          <ul className="-mx-3 flex flex-col">
            {data?.items.map((post) => (
              <li key={post.id} className="border-b border-border last:border-b-0">
                <PostRow post={post} />
              </li>
            ))}
          </ul>
        )}

        {data && data.totalPages > 1 && (
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            onChange={(next) => setSearchParams({ page: String(next) })}
          />
        )}
      </QueryState>
    </div>
  )
}

function PostRow({ post }: { post: PostListItem }) {
  return (
    <Link
      to={paths.postDetail(post.id)}
      className="block rounded-lg px-3 py-5 transition-colors hover:bg-accent"
    >
      <div className="flex items-start gap-3">
        <h2 className="text-[17px] leading-7 font-semibold text-balance">{post.title}</h2>
        {!post.published && <Badge className="mt-1 shrink-0">비발행</Badge>}
      </div>

      <div className="mt-2.5 flex flex-wrap items-center gap-x-2 gap-y-2 text-[13px]">
        <span className="font-medium">{post.authorNickname}</span>
        <span className="text-muted-foreground/60">·</span>
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

function Pagination({
  page,
  totalPages,
  onChange,
}: {
  page: number
  totalPages: number
  onChange: (page: number) => void
}) {
  return (
    <nav className="flex items-center justify-center gap-2">
      <Button
        variant="ghost"
        size="icon"
        aria-label="이전 페이지"
        disabled={page <= 1}
        onClick={() => onChange(page - 1)}
      >
        <ChevronLeft aria-hidden />
      </Button>
      <span className="px-1 text-[13px] text-muted-foreground tabular-nums">
        {page} / {totalPages}
      </span>
      <Button
        variant="ghost"
        size="icon"
        aria-label="다음 페이지"
        disabled={page >= totalPages}
        onClick={() => onChange(page + 1)}
      >
        <ChevronRight aria-hidden />
      </Button>
    </nav>
  )
}
