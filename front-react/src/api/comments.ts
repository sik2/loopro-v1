import { apiFetch } from '@/lib/api-client'

export type CommentDto = {
  id: number
  content: string
  authorId: number
  authorNickname: string
  createDate: string
  modifyDate: string
}

export function fetchComments(postId: number) {
  return apiFetch<CommentDto[]>(`/api/posts/${postId}/comments`)
}

export function writeComment(postId: number, content: string) {
  return apiFetch<CommentDto>(`/api/posts/${postId}/comments`, {
    method: 'POST',
    body: { content },
  })
}

export function updateComment(commentId: number, content: string) {
  return apiFetch<CommentDto>(`/api/comments/${commentId}`, { method: 'PUT', body: { content } })
}

export function deleteComment(commentId: number) {
  return apiFetch<void>(`/api/comments/${commentId}`, { method: 'DELETE' })
}
