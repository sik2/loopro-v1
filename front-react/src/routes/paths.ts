/** 화면 경로를 한 곳에 모은다. 링크는 문자열 리터럴 대신 이 함수를 쓴다. */
export const paths = {
  postList: '/',
  postDetail: (id: number | string) => `/p/${id}`,
  postWrite: '/write',
  postEdit: (id: number | string) => `/p/${id}/edit`,
  login: '/login',
  signup: '/signup',
  me: '/me',
} as const
