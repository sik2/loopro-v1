import { screen, within } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { describe, expect, it } from 'vitest'
import { PostWritePage } from '@/pages/PostWritePage'
import { HOSTILE_MARKDOWN, postDetail } from '@/test/fixtures'
import { problem } from '@/test/handlers'
import { renderApp, signIn } from '@/test/render'
import { server } from '@/test/server'

const API = 'http://localhost:8080'

function openWrite() {
  signIn()
  return renderApp(<PostWritePage />, { path: '/write', route: '/write' })
}

function preview() {
  return document.querySelector('form .prose') as HTMLElement
}

describe('글 쓰기', () => {
  it('빈 상태에서는 미리보기가 안내를 보여준다', async () => {
    openWrite()
    expect(await screen.findByText(/여기에 변환된 모습이 보입니다/)).toBeInTheDocument()
  })

  it('치는 대로 미리보기가 마크다운을 변환한다', async () => {
    const { user } = openWrite()
    await user.type(screen.getByLabelText('본문'), '# 제목입니다')

    expect(await within(preview()).findByRole('heading', { name: '제목입니다' })).toBeInTheDocument()
  })

  it('미리보기도 상세와 같은 새니타이징을 거친다', async () => {
    const { user } = openWrite()
    // paste가 아니라 type이면 한 글자씩 들어가 느리므로 붙여넣기로 넣는다.
    await user.click(screen.getByLabelText('본문'))
    await user.paste(HOSTILE_MARKDOWN)

    await within(preview()).findByText('정상 문단')
    expect(preview().querySelectorAll('script')).toHaveLength(0)
    expect(preview().querySelectorAll('img')).toHaveLength(0)
    expect(preview().querySelectorAll('iframe')).toHaveLength(0)
    expect((window as unknown as { __pwned?: boolean }).__pwned).toBeUndefined()
  })

  it('보낸 값이 그대로 서버로 간다', async () => {
    let sent: unknown
    server.use(http.post(`${API}/api/posts`, async ({ request }) => {
      sent = await request.json()
      return HttpResponse.json(postDetail(), { status: 201 })
    }))

    const { user } = openWrite()
    await user.type(screen.getByLabelText('제목'), '새 글')
    await user.type(screen.getByLabelText('본문'), '본문입니다')
    await user.click(screen.getByRole('button', { name: '올리기' }))

    // 성공하면 그 글의 상세로 이동한다.
    expect(await screen.findByTestId('moved')).toBeInTheDocument()
    expect(sent).toMatchObject({ title: '새 글', content: '본문입니다', published: true })
  })

  it('서버가 항목 오류를 주면 그 자리에 붙인다', async () => {
    server.use(http.post(`${API}/api/posts`, () =>
      problem(400, '입력값이 올바르지 않습니다.', [{ field: 'content', message: '10만자 이하여야 합니다.' }]),
    ))

    const { user } = openWrite()
    await user.type(screen.getByLabelText('제목'), '긴 글')
    await user.type(screen.getByLabelText('본문'), '아주 긴 본문')
    await user.click(screen.getByRole('button', { name: '올리기' }))

    const alerts = await screen.findAllByRole('alert')
    expect(alerts.map((a) => a.textContent)).toContain('10만자 이하여야 합니다.')
  })

  it('제목이 비면 서버에 보내지 않는다', async () => {
    let called = false
    server.use(http.post(`${API}/api/posts`, () => {
      called = true
      return HttpResponse.json(postDetail(), { status: 201 })
    }))

    const { user } = openWrite()
    await user.type(screen.getByLabelText('본문'), '본문만 있음')
    await user.click(screen.getByRole('button', { name: '올리기' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('제목을 입력해 주세요.')
    expect(called).toBe(false)
  })
})
