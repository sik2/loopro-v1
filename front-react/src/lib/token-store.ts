const STORAGE_KEY = 'loopro.accessToken'

type Listener = (token: string | null) => void

const listeners = new Set<Listener>()

/**
 * access token은 브라우저에 저장했다가 요청 헤더에 실어 보낸다.
 * 로그아웃은 이 값을 버리는 것이 전부다 — 서버는 토큰을 무효화하지 않는다(ADR-0002).
 */
export const tokenStore = {
  read(): string | null {
    return localStorage.getItem(STORAGE_KEY)
  },

  write(token: string) {
    localStorage.setItem(STORAGE_KEY, token)
    listeners.forEach((listener) => listener(token))
  },

  clear() {
    localStorage.removeItem(STORAGE_KEY)
    listeners.forEach((listener) => listener(null))
  },

  subscribe(listener: Listener): () => void {
    listeners.add(listener)
    return () => {
      listeners.delete(listener)
    }
  },
}
