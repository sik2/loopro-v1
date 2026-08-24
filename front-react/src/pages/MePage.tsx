import { useAuth } from '@/auth/use-auth'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export function MePage() {
  const { member, logout } = useAuth()

  if (!member) return null

  return (
    <Card className="mx-auto max-w-md">
      <CardHeader>
        <CardTitle>내 정보</CardTitle>
        <CardDescription>어떤 계정으로 로그인했는지 확인합니다.</CardDescription>
      </CardHeader>

      <CardContent className="flex flex-col gap-6">
        <dl className="grid grid-cols-[6rem_1fr] gap-y-3 text-sm">
          <dt className="text-muted-foreground">Nickname</dt>
          <dd className="font-medium">{member.nickname}</dd>

          <dt className="text-muted-foreground">Role</dt>
          <dd className="font-medium">{member.role}</dd>

          <dt className="text-muted-foreground">가입일</dt>
          <dd className="font-medium">{formatDate(member.createDate)}</dd>
        </dl>

        <Button variant="outline" onClick={logout} className="self-start">
          로그아웃
        </Button>
      </CardContent>
    </Card>
  )
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}
