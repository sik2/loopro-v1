import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

/**
 * 화면 자리만 잡아두는 뼈대. 각 화면 티켓이 이 자리를 실제 내용으로 바꾼다.
 */
export function PagePlaceholder({ title, ticket }: { title: string; ticket: string }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>아직 비어 있는 화면입니다.</CardDescription>
      </CardHeader>
      <CardContent className="text-sm text-muted-foreground">
        이 화면의 내용은 <span className="font-mono">{ticket}</span>에서 채워집니다.
      </CardContent>
    </Card>
  )
}
