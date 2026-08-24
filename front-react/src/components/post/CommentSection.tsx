import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  cancelCommentLike,
  deleteComment,
  fetchComments,
  likeComment,
  updateComment,
  writeComment,
  type CommentDto,
} from '@/api/comments'
import { useAuth } from '@/auth/use-auth'
import { LikeButton } from '@/components/post/LikeButton'
import { QueryState } from '@/components/QueryState'
import { Avatar } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { formatDateTime } from '@/lib/date'
import { paths } from '@/routes/paths'

/** 댓글은 별도 화면 없이 글 상세 안에서 다룬다. */
export function CommentSection({ postId }: { postId: number }) {
  const { member } = useAuth()

  const { data, isPending, error } = useQuery({
    queryKey: ['comments', postId],
    queryFn: () => fetchComments(postId),
  })

  return (
    <section className="flex flex-col gap-6 border-t border-border pt-8">
      <h2 className="text-[17px] font-semibold tracking-tight">
        댓글 {data && <span className="text-muted-foreground tabular-nums">{data.length}</span>}
      </h2>

      {member ? (
        <CommentComposer postId={postId} />
      ) : (
        <p className="rounded-lg border border-dashed border-border px-4 py-5 text-center text-[13px] text-muted-foreground">
          댓글을 쓰려면{' '}
          <Link to={paths.login} className="font-medium text-link underline-offset-4 hover:underline">
            로그인
          </Link>
          하세요.
        </p>
      )}

      <QueryState isPending={isPending} error={error}>
        {data && data.length === 0 ? (
          <p className="py-6 text-center text-[13px] text-muted-foreground">아직 댓글이 없습니다.</p>
        ) : (
          <ul className="flex flex-col">
            {data?.map((comment) => (
              <li key={comment.id} className="border-b border-border py-5 last:border-b-0">
                <CommentItem postId={postId} comment={comment} />
              </li>
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
    <div className="flex gap-3">
      <Avatar nickname={comment.authorNickname} className="size-7 text-xs" />

      <div className="flex min-w-0 flex-1 flex-col gap-2">
        <div className="flex items-center gap-2 text-[13px]">
          <span className="font-medium">{comment.authorNickname}</span>
          <span className="text-muted-foreground/60">·</span>
          <span className="text-muted-foreground">{formatDateTime(comment.createDate)}</span>

          {draft === null && (
            <span className="ml-auto flex items-center gap-0.5">
              {isAuthor && (
                <Button variant="ghost" size="sm" onClick={() => setDraft(comment.content)}>
                  고치기
                </Button>
              )}
              {canDelete && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="hover:bg-destructive/10 hover:text-destructive"
                  disabled={remove.isPending}
                  onClick={() => {
                    if (window.confirm('이 댓글을 지울까요?')) remove.mutate()
                  }}
                >
                  지우기
                </Button>
              )}
            </span>
          )}
        </div>

        {draft === null ? (
          <>
            <p className="text-[15px] leading-relaxed whitespace-pre-wrap">{comment.content}</p>
            <CommentLikeButton postId={postId} comment={comment} />
          </>
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
      </div>
    </div>
  )
}

function CommentLikeButton({ postId, comment }: { postId: number; comment: CommentDto }) {
  const { member } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => (comment.likedByMe ? cancelCommentLike(comment.id) : likeComment(comment.id)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['comments', postId] }),
  })

  return (
    <div className="flex">
      <LikeButton
        size="sm"
        count={comment.likeCount}
        likedByMe={comment.likedByMe}
        canLike={Boolean(member)}
        isPending={mutation.isPending}
        onToggle={() => (member ? mutation.mutate() : navigate(paths.login))}
      />
    </div>
  )
}
