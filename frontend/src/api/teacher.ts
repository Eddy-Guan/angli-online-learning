import { get, post, put, del } from '@/utils/request'

export interface TeacherHomeStats {
  courseCount: number
  studentCount: number
  averageRating: number
  attendanceRate: number
  homeworkSubmitRate: number
  parentRating: number
  teacherName?: string
  tags?: string[]
}

export interface TeacherCourse {
  id: number
  title: string
  subject: string
  grade: string
  totalHours: number
  completedHours: number
  studentCount: number
  status: string
  teacherId: number
  createdAt: string
  endAt?: string
}

export interface CourseChapter {
  id: number
  courseId: number
  chapterNumber: number
  title: string
  description: string
  isCompleted: boolean
}

export interface CourseApplyRequest {
  title: string
  subject: string
  grade: string
  totalHours: number
  description: string
  chapters: { title: string; description: string }[]
}

export interface CourseApplyRecord {
  id: number
  title: string
  status: string
  appliedAt: string
  chapters: string[]
}

export interface PushNotificationRequest {
  courseId: number
  summary: string
  homework: string
  goodStudents: string
}

export interface PushRecord {
  id: number
  teacherId: number
  courseId: number
  courseTitle: string
  summary: string
  homework: string
  goodStudents: string
  targetUserIds: string
  targetCount: number
  createdAt: string
}

export interface PushRecordDetail {
  record: PushRecord
  targetUsers: User[]
}

export interface User {
  id: number
  realName: string
  phone: string
  role: string
}

export interface TeacherSettings {
  id: number
  realName: string
  phone: string
  tags: string[]
}

export function getTeacherHomeData(teacherId: number): Promise<TeacherHomeStats> {
  return get(`/teacher/home?teacherId=${teacherId}`)
}

export function getTeacherCourses(teacherId: number, status?: string): Promise<TeacherCourse[]> {
  const params = new URLSearchParams()
  params.append('teacherId', teacherId.toString())
  if (status) params.append('status', status)
  return get(`/teacher/courses?${params.toString()}`)
}

export interface CourseParent {
  id: number
  name: string
  phone: string
  courseId: number
}

export function getCourseParents(courseId: number): Promise<CourseParent[]> {
  return get(`/teacher/course/${courseId}/parents`)
}

export function getCourseChapters(courseId: number): Promise<CourseChapter[]> {
  return get(`/teacher/courses/${courseId}/chapters`)
}

export function toggleChapterComplete(courseId: number, chapterId: number): Promise<void> {
  return put(`/teacher/courses/${courseId}/chapters/${chapterId}/toggle`)
}

export function applyCourse(teacherId: number, data: CourseApplyRequest): Promise<CourseApplyRecord> {
  return post(`/teacher/courses/apply?teacherId=${teacherId}`, data)
}

export function getApplyRecords(teacherId: number): Promise<CourseApplyRecord[]> {
  return get(`/teacher/courses/apply/records?teacherId=${teacherId}`)
}

export function pushNotification(teacherId: number, data: PushNotificationRequest): Promise<void> {
  return post(`/teacher/push?teacherId=${teacherId}`, data)
}

export function getPushRecords(teacherId: number): Promise<PushRecord[]> {
  return get(`/teacher/push/records?teacherId=${teacherId}`)
}

export function getPushRecordDetail(recordId: number, teacherId: number): Promise<PushRecordDetail> {
  return get(`/teacher/push/records/${recordId}?teacherId=${teacherId}`)
}

export function getTeacherSettings(teacherId: number): Promise<TeacherSettings> {
  return get(`/teacher/settings?teacherId=${teacherId}`)
}

export function updateTeacherSettings(teacherId: number, data: Partial<TeacherSettings>): Promise<TeacherSettings> {
  return put(`/teacher/settings?teacherId=${teacherId}`, data)
}

export interface DailySummary {
  id: number
  courseId: number
  content: string
  summaryDate: string
  createdAt: string
}

export function createDailySummary(teacherId: number, courseId: number, content: string, summaryDate: string): Promise<DailySummary> {
  return post(`/teacher/daily-summary?teacherId=${teacherId}&courseId=${courseId}&content=${encodeURIComponent(content)}&summaryDate=${summaryDate}`)
}