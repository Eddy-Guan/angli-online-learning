import { get, post, put, del } from '@/utils/request'

export interface Student {
  id: number
  parentId: number
  name: string
  age: number
  grade: string
  gender: string
  createdAt: string
}

export interface StudentCreateRequest {
  name: string
  age?: number
  grade?: string
  gender?: string
}

export function addStudent(parentId: number, data: StudentCreateRequest): Promise<Student> {
  const params: Record<string, unknown> = { parentId, ...data }
  return post('/parent/students', params)
}

export function getStudentsByParentId(parentId: number): Promise<Student[]> {
  return get(`/parent/students/parent/${parentId}`)
}

export function getStudentById(id: number): Promise<Student> {
  return get(`/parent/students/${id}`)
}

export function updateStudent(id: number, data: Partial<StudentCreateRequest>): Promise<Student> {
  const params: Record<string, unknown> = { ...data }
  return put(`/parent/students/${id}`, params)
}

export function deleteStudent(id: number): Promise<void> {
  return del(`/parent/students/${id}`)
}
