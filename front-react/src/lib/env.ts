/**
 * API 기본 주소는 환경변수로만 주입한다. 코드에 하드코딩하지 않는다.
 * 값이 없으면 조용히 잘못된 주소로 요청하는 대신 즉시 실패한다.
 */
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL

if (!apiBaseUrl) {
  throw new Error('VITE_API_BASE_URL이 설정되지 않았습니다. .env.example을 참고하세요.')
}

export const env = {
  apiBaseUrl: apiBaseUrl.replace(/\/$/, ''),
} as const
