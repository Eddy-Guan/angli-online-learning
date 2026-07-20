import { get } from '@/utils/request'

export interface LearningStats {
  totalHours: number
  completedLessons: number
  streakDays: number
}

export function getLearningStats(userId: number): Promise<LearningStats> {
  return get(`/parent/stats/learning?userId=${userId}`)
}