import { apiFetch } from '@/lib/api-client'
import type { Page } from '@/lib/page'

export type PostListItem = {
  id: number
  title: string
  authorNickname: string
  createDate: string
  viewCount: number
  commentCount: number
  likeCount: number
  likedByMe: boolean
  published: boolean
}

export type PostDetail = {
  id: number
  title: string
  content: string
  authorId: number
  authorNickname: string
  createDate: string
  modifyDate: string
  viewCount: number
  commentCount: number
  likeCount: number
  likedByMe: boolean
  published: boolean
}

export type PostWriteRequest = {
  title: string
  content: string
  published: boolean
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

export function updatePost(id: number, request: PostWriteRequest) {
  return apiFetch<PostDetail>(`/api/posts/${id}`, { method: 'PUT', body: request })
}

export function deletePost(id: number) {
  return apiFetch<void>(`/api/posts/${id}`, { method: 'DELETE' })
}

export function likePost(id: number) {
  return apiFetch<void>(`/api/posts/${id}/like`, { method: 'PUT' })
}

export function cancelPostLike(id: number) {
  return apiFetch<void>(`/api/posts/${id}/like`, { method: 'DELETE' })
}
