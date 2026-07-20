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
  let url = `/parent/checkin/status?userId=${userId}`
  if (studentId) url += `&studentId=${studentId}`
  return get(url)
}

export function checkin(userId: number, studentId?: number): Promise<CheckinResult> {
  let url = `/parent/checkin?userId=${userId}`
  if (studentId) url += `&studentId=${studentId}`
  return post(url)
}
