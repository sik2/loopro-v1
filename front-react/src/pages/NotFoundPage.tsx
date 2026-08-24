import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { paths } from '@/routes/paths'

export function NotFoundPage() {
  return (
    <div className="flex flex-col items-start gap-4">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">찾을 수 없는 페이지입니다</h1>
        <p className="mt-1 text-sm text-muted-foreground">주소를 다시 확인해 주세요.</p>
      </div>
      <Button asChild variant="outline">
        <Link to={paths.postList}>글 목록으로</Link>
      </Button>
    </div>
  )
}
