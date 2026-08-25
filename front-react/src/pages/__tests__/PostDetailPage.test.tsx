import { screen, waitFor } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { describe, expect, it } from 'vitest'
import { PostDetailPage } from '@/pages/PostDetailPage'
import { ADMIN, BARAM, GUREUM, HOSTILE_MARKDOWN, postDetail } from '@/test/fixtures'
import { renderApp, signIn } from '@/test/render'
import { server } from '@/test/server'

const API = 'http://localhost:8080'

function open() {
  return renderApp(<PostDetailPage />, { path: '/p/1', route: '/p/:id' })
}

describe('글 상세', () => {
  it('마크다운을 제목·목록·코드블록·표로 그린다', async () => {
    open()

    expect(await screen.findByRole('heading', { name: '제목' })).toBeInTheDocument()
    expect(screen.getByText('굵게').tagName).toBe('STRONG')
    expect(screen.getByText('첫째')).toBeInTheDocument()
    expect(document.querySelector('article pre')).toBeInTheDocument()
    expect(screen.getByRole('table')).toBeInTheDocument()
  })

  it('악성 마크다운은 하나도 살려 보내지 않는다', async () => {
    server.use(http.get(`${API}/api/posts/:id`, () =>
      HttpResponse.json(postDetail({ content: HOSTILE_MARKDOWN })),
    ))

    open()
    await screen.findByText('정상 문단')

    const article = document.querySelector('article')!
    expect(article.querySelectorAll('script')).toHaveLength(0)
    expect(article.querySelectorAll('img')).toHaveLength(0)
    expect(article.querySelectorAll('iframe')).toHaveLength(0)
    // javascript: 링크는 href가 통째로 사라져야 한다.
    expect([...article.querySelectorAll('a')].map((a) => a.getAttribute('href'))).not.toContain(
      expect.stringContaining('javascript'),
    )
    expect((window as unknown as { __pwned?: boolean }).__pwned).toBeUndefined()
  })

  it('작성자에게만 고치기가 보이고, ADMIN에게는 지우기만 보인다', async () => {
    signIn()

    // 작성자 본인
    const own = open()
    expect(await screen.findByRole('link', { name: '고치기' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '지우기' })).toBeInTheDocument()
    own.unmount()

    // ADMIN — 삭제는 관리지만 수정은 위조다
    server.use(http.get(`${API}/api/members/me`, () => HttpResponse.json(ADMIN)))
    const admin = open()
    expect(await screen.findByRole('button', { name: '지우기' })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: '고치기' })).not.toBeInTheDocument()
    admin.unmount()

    // 남
    server.use(http.get(`${API}/api/members/me`, () => HttpResponse.json(BARAM)))
    open()
    await screen.findByRole('heading', { name: '첫 번째 글', level: 1 })
    await waitFor(() => expect(screen.queryByRole('button', { name: '지우기' })).not.toBeInTheDocument())
    expect(screen.queryByRole('link', { name: '고치기' })).not.toBeInTheDocument()
  })

  it('비로그인이면 아무 버튼도 없다', async () => {
    open()
    await screen.findByRole('heading', { name: '첫 번째 글', level: 1 })
    expect(screen.queryByRole('button', { name: '지우기' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: '고치기' })).not.toBeInTheDocument()
  })

  it('없는 글이면 404 화면을 보여준다', async () => {
    server.use(http.get(`${API}/api/posts/:id`, () =>
      HttpResponse.json({ status: 404, detail: '없음' }, { status: 404 }),
    ))

    open()
    expect(await screen.findByRole('heading', { name: /찾을 수 없는/ })).toBeInTheDocument()
  })

  it('비발행 글에는 표시가 붙는다', async () => {
    signIn()
    server.use(http.get(`${API}/api/posts/:id`, () =>
      HttpResponse.json(postDetail({ published: false, authorId: GUREUM.id })),
    ))

    open()
    expect(await screen.findByText(/비발행/)).toBeInTheDocument()
  })
})
