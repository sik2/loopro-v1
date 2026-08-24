import type { ReactNode } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { AuthProvider } from '@/auth/AuthProvider'
import { useAuth } from '@/auth/use-auth'
import { Avatar } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { paths } from '@/routes/paths'
import { ThemeToggle } from '@/theme/ThemeToggle'

export function RootLayout() {
  return (
    <AuthProvider>
      <div className="flex min-h-dvh flex-col">
        <Header />

        <main className="mx-auto w-full max-w-3xl flex-1 px-5 py-10 sm:px-6">
          <Outlet />
        </main>

        <footer className="mt-16 border-t border-border">
          <div className="mx-auto w-full max-w-3xl px-5 py-8 text-[13px] text-muted-foreground sm:px-6">
            Loopro — 교안을 쓰고 나누는 곳
          </div>
        </footer>
      </div>
    </AuthProvider>
  )
}

function Header() {
  const { member, logout } = useAuth()

  return (
    <header className="sticky top-0 z-20 border-b border-border bg-header backdrop-blur-xl">
      <div className="mx-auto flex h-14 w-full max-w-3xl items-center gap-5 px-5 sm:px-6">
        <Link
          to={paths.postList}
          className="text-[15px] font-semibold tracking-tight transition-opacity hover:opacity-70"
        >
          Loopro
        </Link>

        <nav className="flex items-center gap-1 text-[13px]">
          <HeaderLink to={paths.postList}>글</HeaderLink>
          {member && <HeaderLink to={paths.postWrite}>쓰기</HeaderLink>}
        </nav>

        <div className="ml-auto flex items-center gap-1">
          <ThemeToggle />

          {member ? (
            <>
              <Link
                to={paths.me}
                className="ml-1 flex items-center gap-2 rounded-full py-1 pr-3 pl-1 transition-colors hover:bg-accent"
              >
                <Avatar nickname={member.nickname} className="size-7 text-xs" />
                <span className="text-[13px] font-medium">{member.nickname}</span>
              </Link>
              <Button variant="ghost" size="sm" onClick={logout}>
                로그아웃
              </Button>
            </>
          ) : (
            <>
              <Button asChild variant="ghost" size="sm">
                <Link to={paths.signup}>회원가입</Link>
              </Button>
              <Button asChild variant="secondary" size="sm">
                <Link to={paths.login}>로그인</Link>
              </Button>
            </>
          )}
        </div>
      </div>
    </header>
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
          isActive ? 'font-medium text-foreground' : 'text-muted-foreground',
        )
      }
    >
      {children}
    </NavLink>
  )
}
