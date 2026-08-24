import { createBrowserRouter } from 'react-router-dom'
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
      { path: 'p/:id/edit', element: <PostEditPage /> },
      { path: 'write', element: <PostWritePage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      { path: 'me', element: <MePage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
