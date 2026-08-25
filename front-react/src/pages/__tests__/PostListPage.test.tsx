import { screen } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { describe, expect, it } from 'vitest'
import { PostListPage } from '@/pages/PostListPage'
import { listItem } from '@/test/fixtures'
import { renderApp } from '@/test/render'
import { server } from '@/test/server'

const API = 'http://localhost:8080'

function page(items = [listItem()], overrides = {}) {
  return HttpResponse.json({
    items,
    page: 1,
    size: 10,
    totalItems: items.length,
    totalPages: 1,
    ...overrides,
  })
}

describe('글 목록', () => {
  it('제목·작성자·지표를 보여준다', async () => {
    renderApp(<PostListPage />)

    expect(await screen.findByRole('heading', { name: '첫 번째 글', level: 2 })).toBeInTheDocument()
    expect(screen.getByText('구름')).toBeInTheDocument()
    // 조회 3 · 추천 2 · 댓글 1
    expect(screen.getByText('3')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()
  })

  it('미리보기에서 마크다운 기호를 걷어내고, 제목을 두 번 말하지 않는다', async () => {
    server.use(http.get(`${API}/api/posts`, () =>
      page([listItem({
        title: '마크다운으로 쓰기',
        excerpt: '# 마크다운으로 쓰기\n\n- **첫째** 항목\n\n```java\nvar x = 1;\n```\n\n마지막 문장',
      })]),
    ))

    renderApp(<PostListPage />)
    await screen.findByRole('heading', { name: '마크다운으로 쓰기', level: 2 })

    const preview = screen.getByText(/첫째 항목/)
    expect(preview).toHaveTextContent('마지막 문장')
    // 기호도, 코드블록도, 제목 반복도 없어야 한다.
    expect(preview.textContent).not.toMatch(/[#*`]/)
    expect(preview.textContent).not.toContain('var x = 1')
    expect(preview.textContent).not.toContain('마크다운으로 쓰기')
  })

  it('비발행 글에는 표시가 붙는다', async () => {
    server.use(http.get(`${API}/api/posts`, () => page([listItem({ published: false })])))

    renderApp(<PostListPage />)
    expect(await screen.findByText('비발행')).toBeInTheDocument()
  })

  it('한 페이지뿐이면 페이지 넘김을 감춘다', async () => {
    renderApp(<PostListPage />)
    await screen.findByRole('heading', { name: '첫 번째 글', level: 2 })
    expect(screen.queryByLabelText('다음 페이지')).not.toBeInTheDocument()
  })

  it('여러 페이지면 넘길 수 있고, 첫 페이지에서 이전은 막힌다', async () => {
    server.use(http.get(`${API}/api/posts`, ({ request }) => {
      const p = Number(new URL(request.url).searchParams.get('page') ?? 1)
      return HttpResponse.json({
        items: [listItem({ id: p, title: `${p}쪽 글` })],
        page: p,
        size: 10,
        totalItems: 30,
        totalPages: 3,
      })
    }))

    const { user } = renderApp(<PostListPage />)
    await screen.findByRole('heading', { name: '1쪽 글', level: 2 })
    expect(screen.getByLabelText('이전 페이지')).toBeDisabled()

    await user.click(screen.getByLabelText('다음 페이지'))
    expect(await screen.findByRole('heading', { name: '2쪽 글', level: 2 })).toBeInTheDocument()
    expect(screen.getByLabelText('이전 페이지')).toBeEnabled()
  })

  it('글이 없으면 빈 상태를 알려준다', async () => {
    server.use(http.get(`${API}/api/posts`, () => page([])))

    renderApp(<PostListPage />)
    expect(await screen.findByText(/아직 글이 없습니다/)).toBeInTheDocument()
  })
})
