import { post, get } from '@/utils/request'

export interface LoginData {
  phone: string
  password: string
}

export interface LoginResponse {
  token: string
  role: string
  realName: string
  userId: number
}

export interface RegisterData {
  phone: string
  password: string
  realName: string
  role: string
  code: string
}

export function login(data: LoginData): Promise<LoginResponse> {
  return post('/auth/login', data)
}

export function register(data: RegisterData): Promise<void> {
  return post('/auth/register', data)
}

export function sendCode(phone: string): Promise<void> {
  return post('/auth/send-code', { phone })
}
