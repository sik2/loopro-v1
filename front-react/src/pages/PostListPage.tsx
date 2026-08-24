import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'
import { fetchPostList } from '@/api/posts'
import { QueryState } from '@/components/QueryState'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatDate } from '@/lib/date'
import { paths } from '@/routes/paths'

const PAGE_SIZE = 10

export function PostListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Math.max(Number(searchParams.get('page') ?? '1') || 1, 1)

  const { data, isPending, error } = useQuery({
    queryKey: ['posts', page],
    queryFn: () => fetchPostList(page, PAGE_SIZE),
    placeholderData: keepPreviousData,
  })

  const goToPage = (next: number) => setSearchParams({ page: String(next) })

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold tracking-tight">글</h1>

      <QueryState isPending={isPending} error={error}>
        {data && data.items.length === 0 ? (
          <p className="text-sm text-muted-foreground">아직 글이 없습니다.</p>
        ) : (
          <ul className="divide-y border-y">
            {data?.items.map((post) => (
              <li key={post.id}>
                <Link to={paths.postDetail(post.id)} className="block py-4 hover:bg-accent/40">
                  <p className="flex items-center gap-2 font-medium">
                    {post.title}
                    {!post.published && <Badge>비발행</Badge>}
                  </p>
                  <p className="mt-1 text-sm text-muted-foreground">
                    {post.authorNickname} · {formatDate(post.createDate)} · 조회 {post.viewCount} · 댓글{' '}
                    {post.commentCount} · 추천 {post.likeCount}
                  </p>
                </Link>
              </li>
            ))}
          </ul>
        )}

        {data && data.totalPages > 1 && (
          <nav className="flex items-center justify-center gap-3">
            <Button
              variant="outline"
              size="sm"
              disabled={page <= 1}
              onClick={() => goToPage(page - 1)}
            >
              이전
            </Button>
            <span className="text-sm text-muted-foreground">
              {data.page} / {data.totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              disabled={page >= data.totalPages}
              onClick={() => goToPage(page + 1)}
            >
              다음
            </Button>
          </nav>
        )}
      </QueryState>
    </div>
  )
}
