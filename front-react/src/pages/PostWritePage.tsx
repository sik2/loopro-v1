import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { writePost } from '@/api/posts'
import { FormAlert } from '@/components/form/FormAlert'
import { FormField } from '@/components/form/FormField'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { applyApiFieldErrors } from '@/lib/form'
import { paths } from '@/routes/paths'

/** back의 PostWriteRequest 검증 규칙과 맞물린다. */
const postSchema = z.object({
  title: z.string().min(1, '제목을 입력해 주세요.').max(200, '제목은 200자 이하여야 합니다.'),
  content: z.string().min(1, '본문을 입력해 주세요.'),
})

type PostForm = z.infer<typeof postSchema>

const FIELDS = ['title', 'content'] as const

export function PostWritePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [formError, setFormError] = useState<string>()

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<PostForm>({
    resolver: zodResolver(postSchema),
    defaultValues: { title: '', content: '' },
  })

  const mutation = useMutation({
    mutationFn: writePost,
    onSuccess: (post) => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      navigate(paths.postDetail(post.id), { replace: true })
    },
    onError: (error) => setFormError(applyApiFieldErrors(error, setError, FIELDS)),
  })

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold tracking-tight">글 쓰기</h1>

      <form
        noValidate
        className="flex flex-col gap-4"
        onSubmit={handleSubmit((values) => {
          setFormError(undefined)
          return mutation.mutateAsync(values).catch(() => undefined)
        })}
      >
        <FormAlert message={formError} />

        <FormField id="title" label="제목" error={errors.title?.message}>
          <Input id="title" aria-invalid={Boolean(errors.title)} {...register('title')} />
        </FormField>

        <FormField
          id="content"
          label="본문"
          error={errors.content?.message}
          hint="마크다운으로 씁니다. 제목, 목록, 표, 코드블록이 그대로 그려집니다."
        >
          <Textarea
            id="content"
            rows={18}
            className="font-mono"
            aria-invalid={Boolean(errors.content)}
            {...register('content')}
          />
        </FormField>

        <Button type="submit" className="self-start" disabled={mutation.isPending}>
          {mutation.isPending ? '올리는 중…' : '올리기'}
        </Button>
      </form>
    </div>
  )
}
