import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import { fetchPost, updatePost } from '@/api/posts'
import { useAuth } from '@/auth/use-auth'
import { PostForm } from '@/components/post/PostForm'
import { QueryState } from '@/components/QueryState'
import type { PostFormValues } from '@/components/post/post-schema'
import { paths } from '@/routes/paths'

export function PostEditPage() {
  const { id } = useParams()
  const postId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { member } = useAuth()

  const { data, isPending, error } = useQuery({
    queryKey: ['post', postId],
    queryFn: () => fetchPost(postId),
    enabled: Number.isInteger(postId),
  })

  const mutation = useMutation({
    mutationFn: (values: PostFormValues) => updatePost(postId, values),
    onSuccess: (post) => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      queryClient.setQueryData(['post', postId], post)
      navigate(paths.postDetail(postId), { replace: true })
    },
  })

  // 남의 글은 수정할 수 없다. 서버도 막지만 화면을 먼저 되돌린다.
  if (data && member && data.authorId !== member.id) {
    return <Navigate to={paths.postDetail(postId)} replace />
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold tracking-tight">글 고치기</h1>

      <QueryState isPending={isPending} error={error}>
        {data && (
          <PostForm
            defaultValues={{ title: data.title, content: data.content }}
            submitLabel="저장하기"
            pendingLabel="저장하는 중…"
            isPending={mutation.isPending}
            onSubmit={mutation.mutateAsync}
          />
        )}
      </QueryState>
    </div>
  )
}
