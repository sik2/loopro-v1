import { QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { queryClient } from '@/lib/query-client'
import { paintTheme, resolveTheme } from '@/theme/theme'
import { router } from '@/routes/router'
import '@/index.css'

// 그리기 전에 칠해야 반대 색이 한 번 번쩍이지 않는다.
paintTheme(resolveTheme())

const rootElement = document.getElementById('root')

if (!rootElement) {
  throw new Error('#root 엘리먼트를 찾을 수 없습니다.')
}

createRoot(rootElement).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </StrictMode>,
)
