import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterAll, afterEach, beforeAll } from 'vitest'
import { server } from '@/test/server'

// jsdom에는 matchMedia가 없다. 테마가 기기 설정을 물어보므로 채워둔다.
// 기본값은 어두운 화면이다(= light 질의가 false).
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }),
})

// 테스트는 back을 띄우지 않는다. 네트워크 경계에서 가짜 서버가 응답한다 —
// back의 계약(상태 코드와 ProblemDetail)은 back의 HTTP seam 테스트가 지킨다.
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))

afterEach(() => {
  server.resetHandlers()
  cleanup()
  localStorage.clear()
})

afterAll(() => server.close())
