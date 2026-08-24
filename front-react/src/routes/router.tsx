import { createBrowserRouter } from 'react-router-dom'
import { RequireAuth } from '@/auth/RequireAuth'
import { RootLayout } from '@/components/layout/RootLayout'
import { LoginPage } from '@/pages/LoginPage'
import { MePage } from '@/pages/MePage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { PostDetailPage } from '@/pages/PostDetailPage'
import { PostEditPage } from '@/pages/PostEditPage'
import { PostListPage } from '@/pages/PostListPage'
import { PostWritePage } from '@/pages/PostWritePage'
import { SignupPage } from '@/pages/SignupPage'
import { paths } from '@/routes/paths'

export const router = createBrowserRouter([
  {
    path: paths.postList,
    element: <RootLayout />,
    children: [
      { index: true, element: <PostListPage /> },
      { path: 'p/:id', element: <PostDetailPage /> },
      {
        path: 'p/:id/edit',
        element: (
          <RequireAuth>
            <PostEditPage />
          </RequireAuth>
        ),
      },
      {
        path: 'write',
        element: (
          <RequireAuth>
            <PostWritePage />
          </RequireAuth>
        ),
      },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      {
        path: 'me',
        element: (
          <RequireAuth>
            <MePage />
          </RequireAuth>
        ),
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
