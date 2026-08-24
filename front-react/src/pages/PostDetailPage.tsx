import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deletePost, fetchPost, type PostDetail } from '@/api/posts'
import { useAuth } from '@/auth/use-auth'
import { Markdown } from '@/components/Markdown'
import { CommentSection } from '@/components/post/CommentSection'
import { PostLikeButton } from '@/components/post/PostLikeButton'
import { PostMeta } from '@/components/post/PostMeta'
import { QueryState } from '@/components/QueryState'
import { Avatar } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ApiError } from '@/lib/api-error'
import { formatDateTime } from '@/lib/date'
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
        <article className="flex flex-col gap-10">
          <header className="flex flex-col gap-6">
            <div className="flex flex-col gap-4">
              {!data.published && (
                <div>
                  <Badge>비발행 · 나만 보임</Badge>
                </div>
              )}
              <h1 className="text-[30px] leading-[1.3] font-semibold tracking-tight text-balance sm:text-[36px]">
                {data.title}
              </h1>
            </div>

            <div className="flex flex-wrap items-center gap-3">
              <Avatar nickname={data.authorNickname} />
              <div className="flex flex-col gap-0.5">
                <span className="text-sm font-medium">{data.authorNickname}</span>
                <span className="text-[13px] text-muted-foreground">
                  {formatDateTime(data.createDate)}
                  {data.modifyDate !== data.createDate &&
                    ` · 고침 ${formatDateTime(data.modifyDate)}`}
                </span>
              </div>

              <PostActions post={data} />
            </div>

            <PostMeta
              className="border-y border-border py-3"
              viewCount={data.viewCount}
              likeCount={data.likeCount}
              commentCount={data.commentCount}
            />
          </header>

          <Markdown>{data.content}</Markdown>

          <div className="flex justify-center border-t border-border pt-8">
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
      <Button
        variant="ghost"
        size="sm"
        className="hover:bg-destructive/10 hover:text-destructive"
        disabled={mutation.isPending}
        onClick={() => {
          if (window.confirm('이 글을 지울까요? 되돌릴 수 없습니다.')) mutation.mutate()
        }}
      >
        지우기
      </Button>
    </div>
  )
}
