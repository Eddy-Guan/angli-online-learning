import { get, post, put, del } from '@/utils/request'

export interface TeacherStats {
  courses: number
  students: number
  homework: number
  schedule: number
}

export interface ScheduleItem {
  id: number
  courseName: string
  startTime: string
  endTime: string
  studentCount: number
  status: string
}

export interface HomeworkItem {
  id: number
  courseName: string
  assignmentDate: string
  submittedCount: number
  totalCount: number
}

export interface TeacherCourse {
  id: number
  title: string
  coverImage: string
  status: string
  enrollmentCount: number
  createdAt: string
}

export interface DailySummary {
  id: number
  courseId: number
  content: string
  summaryDate: string
  createdAt: string
}

export interface Message {
  id: number
  senderId: number
  content: string
  messageType: string
  isRead: boolean
  createdAt: string
}

export interface NotificationResult {
  unreadCount: number
  messages: Message[]
}

export function getTeacherStats(teacherId: number): Promise<TeacherStats> {
  return get(`/teacher/stats?teacherId=${teacherId}`)
}

export function getTeacherCourses(teacherId: number): Promise<TeacherCourse[]> {
  return get(`/teacher/courses?teacherId=${teacherId}`)
}

export function getTodaySchedule(teacherId: number): Promise<ScheduleItem[]> {
  return get(`/teacher/schedule/today?teacherId=${teacherId}`)
}

export function getPendingHomework(teacherId: number): Promise<HomeworkItem[]> {
  return get(`/teacher/homework/pending?teacherId=${teacherId}`)
}

export function getNotifications(teacherId: number): Promise<NotificationResult> {
  return get(`/teacher/notifications?teacherId=${teacherId}`)
}

export function createCourse(teacherId: number, data: any): Promise<TeacherCourse> {
  return post(`/teacher/courses?teacherId=${teacherId}`, data)
}

export function updateCourse(teacherId: number, courseId: number, data: any): Promise<TeacherCourse> {
  return put(`/teacher/courses/${courseId}?teacherId=${teacherId}`, data)
}

export function deleteCourse(teacherId: number, courseId: number): Promise<void> {
  return del(`/teacher/courses/${courseId}?teacherId=${teacherId}`)
}

export function getDailySummaries(courseId: number): Promise<DailySummary[]> {
  return get(`/teacher/daily-summary/course/${courseId}`)
}

export function createDailySummary(teacherId: number, courseId: number, content: string, summaryDate: string): Promise<DailySummary> {
  return post(`/teacher/daily-summary?teacherId=${teacherId}&courseId=${courseId}&content=${encodeURIComponent(content)}&summaryDate=${summaryDate}`)
}