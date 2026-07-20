import { get, post, put, del } from '@/utils/request'

export interface AdminStats {
  totalUsers: number
  totalCourses: number
  totalOrders: number
  totalRevenue: number
  totalTeachers: number
  todayOrders: number
  todayRevenue: number
}

export interface DashboardStats {
  totalUsers: number
  activeTeachers: number
  pendingTeachers: number
  totalStudents: number
  averageRating: number
  monthlyRevenue: number
}

export interface UserInfo {
  id: number
  phone: string
  realName: string
  role: string
  status: string
  createdAt: string
  username?: string
  registerTime?: string
}

export interface CourseInfo {
  id: number
  title: string
  teacherName: string
  price: number
  enrollmentCount: number
  status: string
  createdAt: string
  totalHours: number
  coverImage: string
  description: string
}

export interface CreateCourseRequest {
  title: string
  description: string
  category: string
  price: number
  originalPrice: number
  totalHours: number
  coverImage: string
  teacherName: string
  chapters: string[]
  points: number
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

export function getDashboard(): Promise<DashboardStats> {
  return get('/admin/dashboard')
}

export function getUsers(): Promise<UserInfo[]> {
  return get('/admin/users')
}

export function getUserById(id: number): Promise<UserInfo> {
  return get(`/admin/users/${id}`)
}

export function updateUserStatus(id: number, status: string): Promise<void> {
  return put(`/admin/users/${id}/status`, { status })
}

export function getCourses(status?: string): Promise<CourseInfo[]> {
  let url = '/admin/courses'
  if (status) url += `?status=${status}`
  return get(url)
}

export interface CourseApplication {
  id: number
  teacherId: number
  title: string
  subject: string
  grade: string
  totalHours: number
  description: string
  chaptersJson: string
  status: string
  rejectReason: string
  appliedAt: string
  reviewedAt: string
}

export function getCourseApplications(status?: string): Promise<CourseApplication[]> {
  let url = '/admin/course-applications'
  if (status) url += `?status=${status}`
  return get(url)
}

export function approveCourseApplication(id: number, recommend?: boolean): Promise<void> {
  const params = recommend ? `?recommend=${recommend}` : ''
  return put(`/admin/course-applications/${id}/approve${params}`)
}

export function rejectCourseApplication(id: number, reason: string): Promise<void> {
  return put(`/admin/course-applications/${id}/reject`, { reason })
}

export function getOrders(page: number, pageSize: number, status?: string): Promise<{ list: OrderInfo[], total: number }> {
  let url = `/admin/orders?page=${page}&pageSize=${pageSize}`
  if (status) url += `&status=${status}`
  return get(url)
}

export function updateOrderStatus(id: number, status: string): Promise<void> {
  return put(`/admin/orders/${id}/status`, { status })
}

export function createCourse(data: CreateCourseRequest): Promise<CourseInfo> {
  return post('/admin/courses', data)
}

export function unpublishCourse(id: number): Promise<void> {
  return put(`/admin/courses/${id}/unpublish`)
}

export function approveTeacher(id: number): Promise<void> {
  return put(`/admin/users/${id}/approve`)
}

export function rejectTeacher(id: number): Promise<void> {
  return put(`/admin/users/${id}/reject`)
}

export interface PublishNoticeRequest {
  title: string
  content: string
  type: 'normal' | 'important' | 'system'
  pushToUsers: boolean
  pushToTeachers: boolean
}

export function publishNotice(data: PublishNoticeRequest): Promise<void> {
  return post('/admin/notices/publish', data)
}

