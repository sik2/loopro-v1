import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deletePost, fetchPost, type PostDetail } from '@/api/posts'
import { useAuth } from '@/auth/use-auth'
import { CommentSection } from '@/components/post/CommentSection'
import { PostLikeButton } from '@/components/post/PostLikeButton'
import { Markdown } from '@/components/Markdown'
import { QueryState } from '@/components/QueryState'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ApiError } from '@/lib/api-error'
import { formatDate } from '@/lib/date'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { paths } from '@/routes/paths'

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
          <header className="flex flex-col gap-3 border-b pb-6">
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="text-3xl font-semibold tracking-tight">{data.title}</h1>
              {!data.published && <Badge>비발행 · 나만 보임</Badge>}
            </div>
            <div className="flex items-center gap-3">
              <p className="text-sm text-muted-foreground">
                {data.authorNickname} · {formatDate(data.createDate)} · 조회 {data.viewCount}
              </p>
              <PostActions post={data} />
            </div>
          </header>

          <Markdown>{data.content}</Markdown>

          <div className="flex justify-center border-t pt-6">
            <PostLikeButton post={data} />
          </div>

          <CommentSection postId={data.id} />
        </article>
      )}
    </QueryState>
  )
}

/** 수정은 작성자 본인만, 삭제는 본인과 ADMIN. 권한 없는 사람에게는 버튼 자체가 보이지 않는다. */
function PostActions({ post }: { post: PostDetail }) {
  const { member } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => deletePost(post.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      queryClient.removeQueries({ queryKey: ['post', post.id] })
      navigate(paths.postList, { replace: true })
    },
  })

  if (!member) return null

  const isAuthor = member.id === post.authorId
  const canDelete = isAuthor || member.role === 'ADMIN'

  if (!canDelete) return null

  return (
    <div className="ml-auto flex items-center gap-1">
      {isAuthor && (
        <Button asChild variant="ghost" size="sm">
          <Link to={paths.postEdit(post.id)}>고치기</Link>
        </Button>
      )}
      {canDelete && (
        <Button
          variant="ghost"
          size="sm"
          className="text-destructive hover:bg-destructive/10 hover:text-destructive"
          disabled={mutation.isPending}
          onClick={() => {
            if (window.confirm('이 글을 지울까요? 되돌릴 수 없습니다.')) mutation.mutate()
          }}
        >
          지우기
        </Button>
      )}
    </div>
  )
}
