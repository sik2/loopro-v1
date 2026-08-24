import { apiFetch } from '@/lib/api-client'
import type { Page } from '@/lib/page'

export type PostListItem = {
  id: number
  title: string
  authorNickname: string
  createDate: string
}

export type PostDetail = {
  id: number
  title: string
  content: string
  authorId: number
  authorNickname: string
  createDate: string
  modifyDate: string
}

export type PostWriteRequest = {
  title: string
  content: string
}

export function fetchPostList(page: number, size = 10) {
  return apiFetch<Page<PostListItem>>(`/api/posts?page=${page}&size=${size}`)
}

export function fetchPost(id: number) {
  return apiFetch<PostDetail>(`/api/posts/${id}`)
}

export function writePost(request: PostWriteRequest) {
  return apiFetch<PostDetail>('/api/posts', { method: 'POST', body: request })
}
