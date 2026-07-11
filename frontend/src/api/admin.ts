import { get, post, put, del } from '@/utils/request'

export interface AdminStats {
  totalUsers: number
  totalCourses: number
  totalOrders: number
  totalRevenue: number
  todayOrders: number
  todayRevenue: number
}

export interface UserInfo {
  id: number
  phone: string
  realName: string
  role: string
  status: string
  createdAt: string
}

export interface CourseInfo {
  id: number
  title: string
  teacherName: string
  price: number
  enrollmentCount: number
  status: string
  createdAt: string
}

export interface OrderInfo {
  id: number
  orderNo: string
  courseName: string
  userName: string
  amount: number
  status: string
  createdAt: string
}

export function getAdminStats(): Promise<AdminStats> {
  return get('/admin/stats')
}

export function getUsers(page: number, pageSize: number): Promise<{ list: UserInfo[], total: number }> {
  return get(`/admin/users?page=${page}&pageSize=${pageSize}`)
}

export function getUserById(id: number): Promise<UserInfo> {
  return get(`/admin/users/${id}`)
}

export function updateUserStatus(id: number, status: string): Promise<void> {
  return put(`/admin/users/${id}/status`, { status })
}

export function getCourses(page: number, pageSize: number, status?: string): Promise<{ list: CourseInfo[], total: number }> {
  let url = `/admin/courses?page=${page}&pageSize=${pageSize}`
  if (status) url += `&status=${status}`
  return get(url)
}

export function approveCourse(id: number): Promise<void> {
  return put(`/admin/courses/${id}/approve`)
}

export function rejectCourse(id: number, reason: string): Promise<void> {
  return put(`/admin/courses/${id}/reject`, { reason })
}

export function getOrders(page: number, pageSize: number, status?: string): Promise<{ list: OrderInfo[], total: number }> {
  let url = `/admin/orders?page=${page}&pageSize=${pageSize}`
  if (status) url += `&status=${status}`
  return get(url)
}

export function updateOrderStatus(id: number, status: string): Promise<void> {
  return put(`/admin/orders/${id}/status`, { status })
}