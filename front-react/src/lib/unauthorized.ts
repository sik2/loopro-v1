type Handler = () => void

let handler: Handler | undefined

/**
 * 401을 받았을 때 할 일을 등록한다. 토큰을 버리고 로그인 화면으로 보내는 쪽은
 * 라우터를 아는 컴포넌트이므로, API 클라이언트는 이 훅으로만 알린다.
 */
export function setUnauthorizedHandler(next: Handler | undefined) {
  handler = next
}

export function notifyUnauthorized() {
  handler?.()
}
