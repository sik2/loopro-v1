import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from '@/auth/use-auth'
import { FormAlert } from '@/components/form/FormAlert'
import { FormField } from '@/components/form/FormField'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { applyApiFieldErrors } from '@/lib/form'
import { paths } from '@/routes/paths'

const loginSchema = z.object({
  username: z.string().min(1, 'Username을 입력해 주세요.'),
  password: z.string().min(1, '비밀번호를 입력해 주세요.'),
})

type LoginForm = z.infer<typeof loginSchema>

const FIELDS = ['username', 'password'] as const

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()
  const [formError, setFormError] = useState<string>()

  const from = (location.state as { from?: string } | null)?.from ?? paths.postList

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { username: '', password: '' },
  })

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: () => navigate(from, { replace: true }),
    onError: (error) => setFormError(applyApiFieldErrors(error, setError, FIELDS)),
  })

  return (
    <Card className="mx-auto max-w-md">
      <CardHeader>
        <CardTitle>로그인</CardTitle>
        <CardDescription>Username과 비밀번호로 로그인합니다.</CardDescription>
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

          <FormField id="username" label="Username" error={errors.username?.message}>
            <Input
              id="username"
              autoComplete="username"
              aria-invalid={Boolean(errors.username)}
              {...register('username')}
            />
          </FormField>

          <FormField id="password" label="비밀번호" error={errors.password?.message}>
            <Input
              id="password"
              type="password"
              autoComplete="current-password"
              aria-invalid={Boolean(errors.password)}
              {...register('password')}
            />
          </FormField>

          <Button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? '로그인하는 중…' : '로그인'}
          </Button>

          <p className="text-sm text-muted-foreground">
            계정이 없나요?{' '}
            <Link to={paths.signup} className="underline underline-offset-4">
              회원가입
            </Link>
          </p>
        </form>
      </CardContent>
    </Card>
  )
}
