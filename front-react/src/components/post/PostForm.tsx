import { zodResolver } from '@hookform/resolvers/zod'
import { Eye, PencilLine } from 'lucide-react'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { FormAlert } from '@/components/form/FormAlert'
import { FormField } from '@/components/form/FormField'
import { Markdown } from '@/components/Markdown'
import { POST_FIELDS, postSchema, type PostFormValues } from '@/components/post/post-schema'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { applyApiFieldErrors } from '@/lib/form'
import { cn } from '@/lib/utils'

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
  // 좁은 화면에서는 나란히 놓을 자리가 없어 한 번에 하나만 보여준다.
  const [narrowPane, setNarrowPane] = useState<'write' | 'preview'>('write')

  const {
    register,
    handleSubmit,
    setError,
    control,
    formState: { errors },
  } = useForm<PostFormValues>({ resolver: zodResolver(postSchema), defaultValues })

  // watch()가 아니라 useWatch를 쓴다. 본문 한 필드만 구독하므로 제목을 칠 때
  // 미리보기가 다시 그려지지 않는다.
  const content = useWatch({ control, name: 'content' })

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

      <div className="flex flex-col gap-2">
        <div className="flex items-center justify-between gap-3">
          <span className="text-sm font-medium">본문</span>

          {/* 넓은 화면에서는 둘 다 보이므로 이 전환기가 필요 없다. */}
          <div className="flex items-center gap-0.5 lg:hidden">
            <PaneTab active={narrowPane === 'write'} onClick={() => setNarrowPane('write')}>
              <PencilLine aria-hidden /> 쓰기
            </PaneTab>
            <PaneTab active={narrowPane === 'preview'} onClick={() => setNarrowPane('preview')}>
              <Eye aria-hidden /> 미리보기
            </PaneTab>
          </div>
        </div>

        <div className="grid gap-3 lg:grid-cols-2">
          <div className={cn(narrowPane === 'write' ? 'block' : 'hidden', 'lg:block')}>
            <Textarea
              id="content"
              rows={20}
              className="h-full min-h-[28rem] font-mono text-[13px] leading-relaxed"
              aria-invalid={Boolean(errors.content)}
              {...register('content')}
            />
          </div>

          <div
            className={cn(
              narrowPane === 'preview' ? 'block' : 'hidden',
              'lg:block',
              'min-h-[28rem] overflow-auto rounded-md border border-border bg-surface px-5 py-4',
            )}
          >
            {content?.trim() ? (
              <Markdown>{content}</Markdown>
            ) : (
              <p className="text-sm text-muted-foreground">
                여기에 변환된 모습이 보입니다. 왼쪽에 마크다운으로 쓰세요.
              </p>
            )}
          </div>
        </div>

        {errors.content?.message ? (
          <p role="alert" className="text-sm text-destructive">
            {errors.content.message}
          </p>
        ) : (
          <p className="text-sm text-muted-foreground">
            마크다운으로 씁니다. 제목, 목록, 표, 코드블록이 그대로 그려집니다.
          </p>
        )}
      </div>

      <label className="flex items-start gap-3 rounded-md border p-3">
        <input type="checkbox" className="mt-0.5 size-4 accent-primary" {...register('published')} />
        <span className="flex flex-col gap-0.5">
          <span className="text-sm font-medium">발행하기</span>
          <span className="text-sm text-muted-foreground">
            발행하지 않으면 목록에도 상세에도 나만 보입니다.
          </span>
        </span>
      </label>

      <Button type="submit" className="self-start" disabled={isPending}>
        {isPending ? pendingLabel : submitLabel}
      </Button>
    </form>
  )
}

function PaneTab({
  active,
  onClick,
  children,
}: {
  active: boolean
  onClick: () => void
  children: React.ReactNode
}) {
  return (
    <Button
      type="button"
      variant={active ? 'secondary' : 'ghost'}
      size="sm"
      aria-pressed={active}
      onClick={onClick}
    >
      {children}
    </Button>
  )
}
