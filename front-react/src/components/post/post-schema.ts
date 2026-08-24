import { z } from 'zod'

/** back의 PostWriteRequest / PostUpdateRequest 검증 규칙과 맞물린다. */
export const postSchema = z.object({
  title: z.string().min(1, '제목을 입력해 주세요.').max(200, '제목은 200자 이하여야 합니다.'),
  content: z.string().min(1, '본문을 입력해 주세요.'),
})

export type PostFormValues = z.infer<typeof postSchema>

export const POST_FIELDS = ['title', 'content'] as const
