import { get, post } from '@/utils/request'

export interface Evaluation {
  id: number
  userId: number
  courseId: number
  studentId: number
  rating: number
  comment: string
  createdAt: string
}

export interface EvaluationRequest {
  courseId: number
  studentId?: number
  rating: number
  comment?: string
}

export function createEvaluation(userId: number, data: EvaluationRequest): Promise<Evaluation> {
  return post(`/parent/evaluations?userId=${userId}`, data)
}

export function getEvaluationById(id: number): Promise<Evaluation> {
  return get(`/parent/evaluations/${id}`)
}

export function getEvaluationsByCourseId(courseId: number): Promise<Evaluation[]> {
  return get(`/parent/evaluations/course/${courseId}`)
}

export function getCourseAverageRating(courseId: number): Promise<number> {
  return get(`/parent/evaluations/course/${courseId}/rating`)
}
