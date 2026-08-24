import { useQuery } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { fetchPost } from '@/api/posts'
import { Markdown } from '@/components/Markdown'
import { QueryState } from '@/components/QueryState'
import { formatDate } from '@/lib/date'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { ApiError } from '@/lib/api-error'

export function PostDetailPage() {
  const { id } = useParams()
  const postId = Number(id)

  const { data, isPending, error } = useQuery({
    queryKey: ['post', postId],
    queryFn: () => fetchPost(postId),
    enabled: Number.isInteger(postId),
  })

  if (error instanceof ApiError && error.status === 404) {
    return <NotFoundPage />
  }

  return (
    <QueryState isPending={isPending} error={error}>
      {data && (
        <article className="flex flex-col gap-6">
          <header className="flex flex-col gap-2 border-b pb-6">
            <h1 className="text-3xl font-semibold tracking-tight">{data.title}</h1>
            <p className="text-sm text-muted-foreground">
              {data.authorNickname} · {formatDate(data.createDate)}
            </p>
          </header>

          <Markdown>{data.content}</Markdown>
        </article>
      )}
    </QueryState>
  )
}
