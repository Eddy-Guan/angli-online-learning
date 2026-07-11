import { get, post, put, del } from '@/utils/request'

export interface Course {
  id: number
  title: string
  description: string
  coverImage: string
  price: number
  originalPrice: number
  totalHours: number
  teacherName: string
  teacherAvatar: string
  status: string
  category: string
  enrollmentCount: number
  favoriteCount: number
  createdAt: string
}

export interface CourseChapter {
  id: number
  courseId: number
  chapterName: string
  chapterOrder: number
  duration: string
  content: string
  isCompleted: number
  createdAt: string
}

export interface Evaluation {
  id: number
  userId: number
  courseId: number
  studentId: number
  rating: number
  comment: string
  createdAt: string
}

export function getRecommendedCourses(): Promise<Course[]> {
  return get('/public/courses/recommended')
}

export function getHotCourses(): Promise<Course[]> {
  return get('/public/courses/hot')
}

export function getAllPublishedCourses(): Promise<Course[]> {
  return get('/public/courses/all')
}

export function getCourseById(id: number): Promise<Course> {
  return get(`/public/courses/${id}`)
}

export function searchCourses(keyword: string): Promise<Course[]> {
  return get('/public/courses/search', { keyword })
}

export function addFavorite(userId: number, courseId: number): Promise<{ success: boolean }> {
  return post(`/parent/favorites?userId=${userId}&courseId=${courseId}`)
}

export function removeFavorite(userId: number, courseId: number): Promise<{ success: boolean }> {
  return del(`/parent/favorites?userId=${userId}&courseId=${courseId}`)
}

export function isFavorite(userId: number, courseId: number): Promise<{ isFavorite: boolean }> {
  return get(`/parent/favorites/status?userId=${userId}&courseId=${courseId}`)
}

export function getFavorites(userId: number): Promise<Course[]> {
  return get(`/parent/favorites?userId=${userId}`)
}

export function getCourseChapters(courseId: number): Promise<CourseChapter[]> {
  return get(`/public/courses/${courseId}/chapters`)
}

export function getCourseEvaluations(courseId: number): Promise<Evaluation[]> {
  return get(`/public/courses/${courseId}/evaluations`)
}
