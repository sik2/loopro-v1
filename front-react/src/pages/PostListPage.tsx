import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'
import { fetchPostList } from '@/api/posts'
import { PostCard } from '@/components/post/PostCard'
import { QueryState } from '@/components/QueryState'
import { Button } from '@/components/ui/button'

const PAGE_SIZE = 10

/**
 * 목록은 "무엇을 읽을지 고르는" 화면이다. 제목만으로는 고르기 어려워서
 * 카드마다 본문 앞부분을 두 줄 함께 보여준다.
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
    <div className="flex flex-col gap-8">
      <header className="flex items-baseline justify-between gap-4">
        <h1 className="text-[26px] font-semibold tracking-tight">글</h1>
        {data && (
          <p className="text-[13px] text-muted-foreground tabular-nums">{data.totalItems}개</p>
        )}
      </header>

      <QueryState isPending={isPending} error={error}>
        {data && data.items.length === 0 ? (
          <p className="rounded-xl border border-dashed border-border py-20 text-center text-sm text-muted-foreground">
            아직 글이 없습니다.
          </p>
        ) : (
          <ul className="flex flex-col gap-4">
            {data?.items.map((post) => (
              <li key={post.id}>
                <PostCard post={post} />
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
    <nav className="flex items-center justify-center gap-2 pt-2">
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
