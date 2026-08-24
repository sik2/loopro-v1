import type { ReactNode } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { paths } from '@/routes/paths'
import { cn } from '@/lib/utils'

export function RootLayout() {
  return (
    <div className="flex min-h-dvh flex-col">
      <header className="sticky top-0 z-10 border-b bg-background/80 backdrop-blur">
        <div className="mx-auto flex h-14 w-full max-w-3xl items-center gap-4 px-4">
          <Link to={paths.postList} className="text-base font-semibold tracking-tight">
            Loopro
          </Link>

          <nav className="flex items-center gap-1 text-sm">
            <HeaderLink to={paths.postList}>글</HeaderLink>
            <HeaderLink to={paths.postWrite}>쓰기</HeaderLink>
          </nav>

          <div className="ml-auto flex items-center gap-1">
            <Button asChild variant="ghost" size="sm">
              <Link to={paths.me}>내 정보</Link>
            </Button>
            <Button asChild variant="outline" size="sm">
              <Link to={paths.login}>로그인</Link>
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto w-full max-w-3xl flex-1 px-4 py-8">
        <Outlet />
      </main>

      <footer className="border-t">
        <div className="mx-auto w-full max-w-3xl px-4 py-6 text-sm text-muted-foreground">
          Loopro — 교안을 쓰고 나누는 곳
        </div>
      </footer>
    </div>
  )
}

function HeaderLink({ to, children }: { to: string; children: ReactNode }) {
  return (
    <NavLink
      to={to}
      end
      className={({ isActive }) =>
        cn(
          'rounded-md px-2 py-1 transition-colors hover:text-foreground',
          isActive ? 'text-foreground font-medium' : 'text-muted-foreground',
        )
      }
    >
      {children}
    </NavLink>
  )
}
