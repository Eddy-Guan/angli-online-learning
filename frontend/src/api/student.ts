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
  let url = `/parent/students?parentId=${parentId}&name=${encodeURIComponent(data.name)}`
  if (data.age) url += `&age=${data.age}`
  if (data.grade) url += `&grade=${encodeURIComponent(data.grade)}`
  if (data.gender) url += `&gender=${encodeURIComponent(data.gender)}`
  return post(url)
}

export function getStudentsByParentId(parentId: number): Promise<Student[]> {
  return get(`/parent/students/parent/${parentId}`)
}

export function getStudentById(id: number): Promise<Student> {
  return get(`/parent/students/${id}`)
}

export function updateStudent(id: number, data: Partial<StudentCreateRequest>): Promise<Student> {
  let url = `/parent/students/${id}`
  const params: string[] = []
  if (data.name) params.push(`name=${encodeURIComponent(data.name)}`)
  if (data.age !== undefined) params.push(`age=${data.age}`)
  if (data.grade) params.push(`grade=${encodeURIComponent(data.grade)}`)
  if (data.gender) params.push(`gender=${encodeURIComponent(data.gender)}`)
  if (params.length > 0) url += `?${params.join('&')}`
  return put(url)
}

export function deleteStudent(id: number): Promise<void> {
  return del(`/parent/students/${id}`)
}
