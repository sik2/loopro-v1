import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { FormAlert } from '@/components/form/FormAlert'
import { FormField } from '@/components/form/FormField'
import { POST_FIELDS, postSchema, type PostFormValues } from '@/components/post/post-schema'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { applyApiFieldErrors } from '@/lib/form'

/** 작성 화면과 수정 화면이 같은 폼을 쓴다. 두 화면의 검증 규칙이 갈라지지 않게 한다. */
export function PostForm({
  defaultValues,
  submitLabel,
  pendingLabel,
  isPending,
  onSubmit,
}: {
  defaultValues: PostFormValues
  submitLabel: string
  pendingLabel: string
  isPending: boolean
  onSubmit: (values: PostFormValues) => Promise<unknown>
}) {
  const [formError, setFormError] = useState<string>()

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<PostFormValues>({ resolver: zodResolver(postSchema), defaultValues })

  return (
    <form
      noValidate
      className="flex flex-col gap-4"
      onSubmit={handleSubmit(async (values) => {
        setFormError(undefined)
        try {
          await onSubmit(values)
        } catch (error) {
          setFormError(applyApiFieldErrors(error, setError, POST_FIELDS))
        }
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

      <Button type="submit" className="self-start" disabled={isPending}>
        {isPending ? pendingLabel : submitLabel}
      </Button>
    </form>
  )
}
