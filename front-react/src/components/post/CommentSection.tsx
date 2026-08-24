import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  deleteComment,
  fetchComments,
  updateComment,
  writeComment,
  type CommentDto,
} from '@/api/comments'
import { useAuth } from '@/auth/use-auth'
import { QueryState } from '@/components/QueryState'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { formatDate } from '@/lib/date'
import { paths } from '@/routes/paths'

/** 댓글은 별도 화면 없이 글 상세 안에서 다룬다. */
export function CommentSection({ postId }: { postId: number }) {
  const { member } = useAuth()

  const { data, isPending, error } = useQuery({
    queryKey: ['comments', postId],
    queryFn: () => fetchComments(postId),
  })

  return (
    <section className="flex flex-col gap-4 border-t pt-6">
      <h2 className="text-lg font-semibold">댓글 {data ? data.length : ''}</h2>

      {member ? (
        <CommentComposer postId={postId} />
      ) : (
        <p className="rounded-md border bg-muted/40 px-3 py-2 text-sm text-muted-foreground">
          댓글을 쓰려면{' '}
          <Link to={paths.login} className="underline underline-offset-4">
            로그인
          </Link>
          하세요.
        </p>
      )}

      <QueryState isPending={isPending} error={error}>
        {data && data.length === 0 ? (
          <p className="text-sm text-muted-foreground">아직 댓글이 없습니다.</p>
        ) : (
          <ul className="flex flex-col divide-y">
            {data?.map((comment) => (
              <CommentItem key={comment.id} postId={postId} comment={comment} />
            ))}
          </ul>
        )}
      </QueryState>
    </section>
  )
}

function CommentComposer({ postId }: { postId: number }) {
  const [content, setContent] = useState('')
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => writeComment(postId, content),
    onSuccess: () => {
      setContent('')
      queryClient.invalidateQueries({ queryKey: ['comments', postId] })
      queryClient.invalidateQueries({ queryKey: ['post', postId] })
    },
  })

  return (
    <form
      className="flex flex-col items-end gap-2"
      onSubmit={(event) => {
        event.preventDefault()
        if (content.trim()) mutation.mutate()
      }}
    >
      <Textarea
        aria-label="댓글 내용"
        rows={3}
        placeholder="댓글을 남겨보세요."
        value={content}
        onChange={(event) => setContent(event.target.value)}
      />
      <Button type="submit" size="sm" disabled={!content.trim() || mutation.isPending}>
        {mutation.isPending ? '다는 중…' : '댓글 달기'}
      </Button>
    </form>
  )
}

function CommentItem({ postId, comment }: { postId: number; comment: CommentDto }) {
  const { member } = useAuth()
  const queryClient = useQueryClient()
  const [draft, setDraft] = useState<string | null>(null)

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['comments', postId] })
    queryClient.invalidateQueries({ queryKey: ['post', postId] })
  }

  const update = useMutation({
    mutationFn: () => updateComment(comment.id, draft ?? ''),
    onSuccess: () => {
      setDraft(null)
      invalidate()
    },
  })

  const remove = useMutation({ mutationFn: () => deleteComment(comment.id), onSuccess: invalidate })

  const isAuthor = member?.id === comment.authorId
  const canDelete = isAuthor || member?.role === 'ADMIN'

  return (
    <li className="flex flex-col gap-2 py-4">
      <div className="flex items-center gap-2 text-sm">
        <span className="font-medium">{comment.authorNickname}</span>
        <span className="text-muted-foreground">{formatDate(comment.createDate)}</span>

        <span className="ml-auto flex items-center gap-1">
          {isAuthor && draft === null && (
            <Button variant="ghost" size="sm" onClick={() => setDraft(comment.content)}>
              고치기
            </Button>
          )}
          {canDelete && (
            <Button
              variant="ghost"
              size="sm"
              className="text-destructive hover:bg-destructive/10 hover:text-destructive"
              disabled={remove.isPending}
              onClick={() => {
                if (window.confirm('이 댓글을 지울까요?')) remove.mutate()
              }}
            >
              지우기
            </Button>
          )}
        </span>
      </div>

      {draft === null ? (
        <p className="text-sm whitespace-pre-wrap">{comment.content}</p>
      ) : (
        <div className="flex flex-col items-end gap-2">
          <Textarea
            aria-label="댓글 고치기"
            rows={3}
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
          />
          <div className="flex gap-1">
            <Button variant="ghost" size="sm" onClick={() => setDraft(null)}>
              그만두기
            </Button>
            <Button
              size="sm"
              disabled={!draft.trim() || update.isPending}
              onClick={() => update.mutate()}
            >
              저장하기
            </Button>
          </div>
        </div>
      )}
    </li>
  )
}
