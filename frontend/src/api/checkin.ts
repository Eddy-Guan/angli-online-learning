import { get, post } from '@/utils/request'

export interface CheckinStatus {
  hasChecked: boolean
  continuousDays: number
}

export interface CheckinResult {
  success: boolean
  continuousDays: number
}

export function getCheckinStatus(userId: number, studentId?: number): Promise<CheckinStatus> {
  const params: Record<string, number> = { userId }
  if (studentId) params.studentId = studentId
  return get('/parent/checkin/status', params)
}

export function checkin(userId: number, studentId?: number): Promise<CheckinResult> {
  const params: Record<string, number> = { userId }
  if (studentId) params.studentId = studentId
  return post('/parent/checkin', params)
}
