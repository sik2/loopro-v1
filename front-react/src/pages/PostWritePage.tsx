import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { writePost } from '@/api/posts'
import { PostForm } from '@/components/post/PostForm'
import { paths } from '@/routes/paths'

export function PostWritePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: writePost,
    onSuccess: (post) => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      navigate(paths.postDetail(post.id), { replace: true })
    },
  })

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold tracking-tight">글 쓰기</h1>

      <PostForm
        defaultValues={{ title: '', content: '', published: true }}
        submitLabel="올리기"
        pendingLabel="올리는 중…"
        isPending={mutation.isPending}
        onSubmit={mutation.mutateAsync}
      />
    </div>
  )
}
