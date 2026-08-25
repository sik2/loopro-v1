import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { ReactElement } from 'react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { RootLayout } from '@/components/layout/RootLayout'

/**
 * 화면 하나가 아니라 <b>앱을 세운다</b> — 라우터·인증·서버 상태를 다 끼운 채로.
 *
 * <p>back의 seam이 HTTP 하나였듯 front의 seam도 하나다: 사용자가 보는 화면.
 * 훅이나 컴포넌트를 따로 떼어 검사하지 않는다.
 */
export function renderApp(
  element: ReactElement,
  { path = '/', route = '/' }: { path?: string; route?: string } = {},
) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })

  const router = createMemoryRouter(
    [
      {
        path: '/',
        element: <RootLayout />,
        children: [
          { path: route.slice(1) || '/', element, index: route === '/' },
          // 화면이 어디론가 이동하면(작성 후 상세로 등) 그 경로가 없어서 터진다.
          // 어디로 갔는지만 알면 되므로 표지판 하나를 깔아둔다.
          { path: '*', element: <MovedElsewhere /> },
        ],
      },
    ],
    { initialEntries: [path] },
  )

  return {
    user: userEvent.setup(),
    ...render(
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
      </QueryClientProvider>,
    ),
  }
}

/** 이동한 곳을 알려주는 표지판. `await screen.findByTestId('moved')`로 확인한다. */
function MovedElsewhere() {
  return <div data-testid="moved">이동함</div>
}

/** 로그인한 상태로 시작한다. 토큰은 apiFetch가 헤더에 실어 보낸다. */
export function signIn(token = 'test-token') {
  localStorage.setItem('loopro.accessToken', token)
}
