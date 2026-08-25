import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { signup } from '@/api/members'
import { FormAlert } from '@/components/form/FormAlert'
import { FormField } from '@/components/form/FormField'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { applyApiFieldErrors } from '@/lib/form'
import { paths } from '@/routes/paths'

/**
 * back의 SignupRequest 검증 규칙과 맞물린다. 한쪽만 고치면 어긋난다.
 * 화면 표기는 `아이디`·`닉네임`, 코드와 API의 이름은 `username`·`nickname`이다(CONTEXT.md).
 */
const signupSchema = z.object({
  username: z
    .string()
    .min(3, '아이디는 3자 이상 30자 이하여야 합니다.')
    .max(30, '아이디는 3자 이상 30자 이하여야 합니다.')
    .regex(/^[a-zA-Z0-9_]+$/, '아이디는 영문, 숫자, 밑줄만 쓸 수 있습니다.'),
  password: z
    .string()
    .min(8, '비밀번호는 8자 이상 64자 이하여야 합니다.')
    .max(64, '비밀번호는 8자 이상 64자 이하여야 합니다.'),
  /**
   * 비밀번호 확인은 화면에만 있다. back으로 보내지 않는다 — 저장할 값도 아니고,
   * 화면을 거치지 않은 요청은 자기가 무엇을 보냈는지 이미 알기 때문에 막을 것이 없다.
   * 오타를 잡는 장치이지 보안 장치가 아니다.
   */
  passwordConfirm: z.string().min(1, '비밀번호를 한 번 더 입력해 주세요.'),
  nickname: z
    .string()
    .min(2, '닉네임은 2자 이상 30자 이하여야 합니다.')
    .max(30, '닉네임은 2자 이상 30자 이하여야 합니다.'),
}).refine((v) => v.password === v.passwordConfirm, {
  message: '비밀번호가 서로 다릅니다.',
  path: ['passwordConfirm'],
})

type SignupForm = z.infer<typeof signupSchema>

/** 서버가 오류를 붙일 수 있는 항목. `passwordConfirm`은 서버가 모르는 값이라 뺀다. */
const FIELDS = ['username', 'password', 'nickname'] as const

export function SignupPage() {
  const navigate = useNavigate()
  const [formError, setFormError] = useState<string>()

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<SignupForm>({
    resolver: zodResolver(signupSchema),
    defaultValues: { username: '', password: '', passwordConfirm: '', nickname: '' },
  })

  const mutation = useMutation({
    // 확인용 값은 여기서 떨어뜨린다. 요청 본문에 실리지 않는다.
    mutationFn: ({ username, password, nickname }: SignupForm) =>
      signup({ username, password, nickname }),
    onSuccess: () => navigate(paths.login, { replace: true }),
    onError: (error) => setFormError(applyApiFieldErrors(error, setError, FIELDS)),
  })

  return (
    <Card className="mx-auto max-w-md">
      <CardHeader>
        <CardTitle>회원가입</CardTitle>
        <CardDescription>아이디로 로그인하고, 닉네임으로 사람들에게 보입니다.</CardDescription>
      </CardHeader>

      <CardContent>
        <form
          noValidate
          className="flex flex-col gap-4"
          onSubmit={handleSubmit((values) => {
            setFormError(undefined)
            return mutation.mutateAsync(values).catch(() => undefined)
          })}
        >
          <FormAlert message={formError} />

          <FormField
            id="username"
            label="아이디"
            error={errors.username?.message}
            hint="영문, 숫자, 밑줄 3~30자. 로그인할 때만 쓰이고 화면에는 보이지 않습니다."
          >
            <Input
              id="username"
              autoComplete="username"
              aria-invalid={Boolean(errors.username)}
              {...register('username')}
            />
          </FormField>

          <FormField
            id="password"
            label="비밀번호"
            error={errors.password?.message}
            hint="8자 이상 64자 이하."
          >
            <Input
              id="password"
              type="password"
              autoComplete="new-password"
              aria-invalid={Boolean(errors.password)}
              {...register('password')}
            />
          </FormField>

          <FormField
            id="passwordConfirm"
            label="비밀번호 확인"
            error={errors.passwordConfirm?.message}
            hint="같은 비밀번호를 한 번 더 입력하세요."
          >
            <Input
              id="passwordConfirm"
              type="password"
              autoComplete="new-password"
              aria-invalid={Boolean(errors.passwordConfirm)}
              {...register('passwordConfirm')}
            />
          </FormField>

          <FormField
            id="nickname"
            label="닉네임"
            error={errors.nickname?.message}
            hint="2~30자. 다른 사람에게 보이는 이름입니다."
          >
            <Input
              id="nickname"
              autoComplete="nickname"
              aria-invalid={Boolean(errors.nickname)}
              {...register('nickname')}
            />
          </FormField>

          <Button type="submit" disabled={isSubmitting || mutation.isPending}>
            {mutation.isPending ? '가입하는 중…' : '가입하기'}
          </Button>

          <p className="text-sm text-muted-foreground">
            이미 계정이 있나요?{' '}
            <Link to={paths.login} className="underline underline-offset-4">
              로그인
            </Link>
          </p>
        </form>
      </CardContent>
    </Card>
  )
}
