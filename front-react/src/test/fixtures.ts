import type { CommentDto } from '@/api/comments'
import type { MemberDto } from '@/api/members'
import type { PostDetail, PostListItem } from '@/api/posts'

export const GUREUM: MemberDto = {
  id: 2,
  nickname: '구름',
  role: 'USER',
  createDate: '2026-08-01T09:00:00',
}

export const ADMIN: MemberDto = {
  id: 1,
  nickname: '관리자',
  role: 'ADMIN',
  createDate: '2026-08-01T09:00:00',
}

export const BARAM: MemberDto = {
  id: 3,
  nickname: '바람',
  role: 'USER',
  createDate: '2026-08-01T09:00:00',
}

export const MARKDOWN = `# 제목

본문에 **굵게**가 있다.

- 첫째
- 둘째

\`\`\`java
record Post(String title) {}
\`\`\`

| 항목 | 값 |
| --- | --- |
| 상태 | 정상 |
`

/** 새니타이징이 실제로 도는지 보기 위한 본문. 하나라도 살아 나오면 실패다. */
export const HOSTILE_MARKDOWN = `<script>window.__pwned = true</script>

<img src=x onerror="window.__pwned = true">

<iframe src="https://evil.example"></iframe>

[클릭](javascript:alert(1))

정상 문단
`

export function listItem(overrides: Partial<PostListItem> = {}): PostListItem {
  return {
    id: 1,
    title: '첫 번째 글',
    excerpt: '# 첫 번째 글\n\n본문 앞부분입니다.',
    authorNickname: GUREUM.nickname,
    createDate: '2026-08-20T10:00:00',
    viewCount: 3,
    commentCount: 1,
    likeCount: 2,
    likedByMe: false,
    published: true,
    ...overrides,
  }
}

export function postDetail(overrides: Partial<PostDetail> = {}): PostDetail {
  return {
    id: 1,
    title: '첫 번째 글',
    content: MARKDOWN,
    authorId: GUREUM.id,
    authorNickname: GUREUM.nickname,
    createDate: '2026-08-20T10:00:00',
    modifyDate: '2026-08-20T10:00:00',
    viewCount: 3,
    commentCount: 1,
    likeCount: 2,
    likedByMe: false,
    published: true,
    ...overrides,
  }
}

export function comment(overrides: Partial<CommentDto> = {}): CommentDto {
  return {
    id: 1,
    content: '첫 댓글',
    authorId: BARAM.id,
    authorNickname: BARAM.nickname,
    createDate: '2026-08-20T11:00:00',
    modifyDate: '2026-08-20T11:00:00',
    likeCount: 0,
    likedByMe: false,
    ...overrides,
  }
}
