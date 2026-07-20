import { get, post, put } from '@/utils/request'

export interface MessageItem {
  id: number
  userId: number
  type: string
  title: string
  content: string
  relatedId: number
  isRead: boolean
  readAt?: string
  createdAt: string
  updatedAt?: string
}

export interface GetMessagesResponse {
  list: MessageItem[]
  unreadCount: number
}

export function getMessages(userId: number, offset: number, limit: number): Promise<GetMessagesResponse> {
  return get(`/message/list?userId=${userId}&offset=${offset}&limit=${limit}`)
}

export function getUnreadCount(userId: number): Promise<{ count: number }> {
  return get(`/message/unread-count?userId=${userId}`)
}

export function markAsRead(messageId: number): Promise<void> {
  return put(`/message/read/${messageId}`)
}

export function markAllMessagesRead(userId: number): Promise<void> {
  return put(`/message/read-all?userId=${userId}`)
}