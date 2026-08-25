import { screen, waitFor } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { describe, expect, it } from 'vitest'
import { LoginPage } from '@/pages/LoginPage'
import { PostListPage } from '@/pages/PostListPage'
import { SignupPage } from '@/pages/SignupPage'
import { GUREUM } from '@/test/fixtures'
import { problem } from '@/test/handlers'
import { renderApp, signIn } from '@/test/render'
import { server } from '@/test/server'

const API = 'http://localhost:8080'
const TOKEN_KEY = 'loopro.accessToken'

describe('로그인', () => {
  it('성공하면 토큰을 저장한다', async () => {
    server.use(http.post(`${API}/api/auth/login`, () =>
      HttpResponse.json({ accessToken: 'granted', member: GUREUM }),
    ))

    const { user } = renderApp(<LoginPage />, { path: '/login', route: '/login' })
    await user.type(screen.getByLabelText('아이디'), 'gureum')
    await user.type(screen.getByLabelText('비밀번호'), 'password123')
    await user.click(screen.getByRole('button', { name: '로그인' }))

    await waitFor(() => expect(localStorage.getItem(TOKEN_KEY)).toBe('granted'))
  })

  it('실패하면 서버 메시지를 보여주고 토큰을 저장하지 않는다', async () => {
    server.use(http.post(`${API}/api/auth/login`, () =>
      problem(401, '아이디 또는 비밀번호가 올바르지 않습니다.'),
    ))

    const { user } = renderApp(<LoginPage />, { path: '/login', route: '/login' })
    await user.type(screen.getByLabelText('아이디'), 'gureum')
    await user.type(screen.getByLabelText('비밀번호'), 'wrong')
    await user.click(screen.getByRole('button', { name: '로그인' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('아이디 또는 비밀번호가 올바르지 않습니다.')
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull()
  })

  it('로그인 실패의 401은 세션 만료가 아니므로 화면을 쫓아내지 않는다', async () => {
    server.use(http.post(`${API}/api/auth/login`, () => problem(401, '아이디 또는 비밀번호가 올바르지 않습니다.')))

    const { user } = renderApp(<LoginPage />, { path: '/login', route: '/login' })
    await user.type(screen.getByLabelText('아이디'), 'gureum')
    await user.type(screen.getByLabelText('비밀번호'), 'wrong')
    await user.click(screen.getByRole('button', { name: '로그인' }))

    await screen.findByRole('alert')
    // 로그인 화면 그대로다.
    expect(screen.getByRole('button', { name: '로그인' })).toBeInTheDocument()
  })

  it('비어 있으면 서버에 보내지 않고 화면에서 막는다', async () => {
    let called = false
    server.use(http.post(`${API}/api/auth/login`, () => {
      called = true
      return HttpResponse.json({ accessToken: 'x', member: GUREUM })
    }))

    const { user } = renderApp(<LoginPage />, { path: '/login', route: '/login' })
    await user.click(screen.getByRole('button', { name: '로그인' }))

    expect(await screen.findAllByRole('alert')).toHaveLength(2)
    expect(called).toBe(false)
  })
})

describe('세션 만료', () => {
  it('보호된 요청이 401이면 토큰을 버린다', async () => {
    signIn('stale')
    server.use(http.get(`${API}/api/members/me`, () => problem(401, '로그인이 필요합니다.')))

    renderApp(<PostListPage />)

    await waitFor(() => expect(localStorage.getItem(TOKEN_KEY)).toBeNull())
  })
})

describe('회원가입', () => {
  it('서버가 짚어준 항목에 오류를 붙인다', async () => {
    server.use(http.post(`${API}/api/members`, () =>
      problem(409, '이미 사용 중입니다.', [{ field: 'username', message: '이미 사용 중입니다.' }]),
    ))

    const { user } = renderApp(<SignupPage />, { path: '/signup', route: '/signup' })
    await fillSignup(user, { password: 'password123', passwordConfirm: 'password123' })
    await user.click(screen.getByRole('button', { name: '가입하기' }))

    const alerts = await screen.findAllByRole('alert')
    expect(alerts.map((a) => a.textContent)).toContain('이미 사용 중입니다.')
  })

  it('화면 검증이 back 규칙과 같은 지점에서 막는다', async () => {
    let called = false
    server.use(http.post(`${API}/api/members`, () => {
      called = true
      return HttpResponse.json(GUREUM, { status: 201 })
    }))

    const { user } = renderApp(<SignupPage />, { path: '/signup', route: '/signup' })
    await fillSignup(user, {
      username: 'ab',              // 3자 미만
      password: 'short',           // 8자 미만
      passwordConfirm: 'short',
      nickname: '가',               // 2자 미만
    })
    await user.click(screen.getByRole('button', { name: '가입하기' }))

    expect(await screen.findAllByRole('alert')).toHaveLength(3)
    expect(called).toBe(false)
  })

  it('비밀번호와 확인이 다르면 확인 칸에 알려주고 보내지 않는다', async () => {
    let called = false
    server.use(http.post(`${API}/api/members`, () => {
      called = true
      return HttpResponse.json(GUREUM, { status: 201 })
    }))

    const { user } = renderApp(<SignupPage />, { path: '/signup', route: '/signup' })
    await fillSignup(user, { password: 'password123', passwordConfirm: 'password124' })
    await user.click(screen.getByRole('button', { name: '가입하기' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('비밀번호가 서로 다릅니다.')
    expect(screen.getByLabelText('비밀번호 확인')).toHaveAttribute('aria-invalid', 'true')
    expect(called).toBe(false)
  })

  it('확인 칸이 비어도 보내지 않는다', async () => {
    let called = false
    server.use(http.post(`${API}/api/members`, () => {
      called = true
      return HttpResponse.json(GUREUM, { status: 201 })
    }))

    const { user } = renderApp(<SignupPage />, { path: '/signup', route: '/signup' })
    await fillSignup(user, { password: 'password123', passwordConfirm: '' })
    await user.click(screen.getByRole('button', { name: '가입하기' }))

    await screen.findByRole('alert')
    expect(called).toBe(false)
  })

  it('확인용 값은 서버로 보내지 않는다', async () => {
    let sent: Record<string, unknown> | undefined
    server.use(http.post(`${API}/api/members`, async ({ request }) => {
      sent = (await request.json()) as Record<string, unknown>
      return HttpResponse.json(GUREUM, { status: 201 })
    }))

    const { user } = renderApp(<SignupPage />, { path: '/signup', route: '/signup' })
    await fillSignup(user, { password: 'password123', passwordConfirm: 'password123' })
    await user.click(screen.getByRole('button', { name: '가입하기' }))

    await waitFor(() => expect(sent).toBeDefined())
    expect(sent).toEqual({ username: 'gureum', password: 'password123', nickname: '구름' })
    expect(sent).not.toHaveProperty('passwordConfirm')
  })
})

type SignupInput = {
  username?: string
  password?: string
  passwordConfirm?: string
  nickname?: string
}

async function fillSignup(
  user: ReturnType<typeof renderApp>['user'],
  { username = 'gureum', password = 'password123', passwordConfirm = password, nickname = '구름' }: SignupInput,
) {
  await user.type(screen.getByLabelText('아이디'), username)
  if (password) await user.type(screen.getByLabelText('비밀번호'), password)
  if (passwordConfirm) await user.type(screen.getByLabelText('비밀번호 확인'), passwordConfirm)
  await user.type(screen.getByLabelText('닉네임'), nickname)
}
