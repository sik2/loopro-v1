import { useAuth } from '@/auth/use-auth'
import { Avatar } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatDate } from '@/lib/date'

export function MePage() {
  const { member, logout } = useAuth()

  if (!member) return null

  return (
    <div className="mx-auto flex max-w-md flex-col gap-8">
      <header className="flex items-center gap-4">
        <Avatar nickname={member.nickname} className="size-14 text-xl" />
        <div className="flex flex-col gap-1.5">
          <h1 className="text-xl font-semibold tracking-tight">{member.nickname}</h1>
          <div>
            <Badge>{member.role}</Badge>
          </div>
        </div>
      </header>

      <dl className="flex flex-col">
        <Row label="가입일" value={formatDate(member.createDate)} />
      </dl>

      <div>
        <Button variant="outline" onClick={logout}>
          로그아웃
        </Button>
      </div>
    </div>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between border-b border-border py-3 text-sm">
      <dt className="text-muted-foreground">{label}</dt>
      <dd className="font-medium">{value}</dd>
    </div>
  )
}
