import { get, post } from '@/utils/request'

export interface Order {
  id: number
  orderNo: string
  userId: number
  courseId: number
  studentId: number
  amount: number
  status: string
  payMethod: string
  payTime: string
  createdAt: string
}

export interface OrderCreateRequest {
  courseId: number
  studentId?: number
}

export interface PayResult {
  success: boolean
  order: Order
}

export function createOrder(userId: number, data: OrderCreateRequest): Promise<Order> {
  return post(`/parent/orders?userId=${userId}`, data)
}

export function getOrderById(id: number): Promise<Order> {
  return get(`/parent/orders/${id}`)
}

export function getOrdersByUserId(userId: number): Promise<Order[]> {
  return get(`/parent/orders?userId=${userId}`)
}

export function payOrder(orderId: number): Promise<PayResult> {
  return post(`/parent/orders/${orderId}/pay`)
}

export function queryOrderStatus(orderId: number): Promise<Order> {
  return post(`/parent/orders/${orderId}/query`)
}