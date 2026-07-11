import type { UniApp } from '@dcloudio/types'

const BASE_URL = 'http://localhost:8080/api'

interface RequestOptions extends UniApp.RequestOptions {
  showLoading?: boolean
  showError?: boolean
}

export function request<T = any>(options: RequestOptions): Promise<T> {
  const { showLoading = true, showError = true, url, ...rest } = options
  
  return new Promise((resolve, reject) => {
    if (showLoading) {
      uni.showLoading({ title: '加载中...' })
    }
    
    const token = uni.getStorageSync('token')
    const header: Record<string, string> = {
      'Content-Type': 'application/json',
      ...rest.header
    }
    
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }
    
    uni.request({
      url: `${BASE_URL}${url}`,
      header,
      ...rest,
      success: (res) => {
        if (showLoading) {
          uni.hideLoading()
        }
        
        const data = res.data as { code: number; message: string; data: T }
        
        if (data.code === 200) {
          resolve(data.data)
        } else {
          if (showError) {
            uni.showToast({
              title: data.message || '请求失败',
              icon: 'none'
            })
          }
          reject(data)
        }
      },
      fail: (err) => {
        if (showLoading) {
          uni.hideLoading()
        }
        
        if (showError) {
          uni.showToast({
            title: '网络请求失败',
            icon: 'none'
          })
        }
        reject(err)
      }
    })
  })
}

export function get<T = any>(url: string, data?: Record<string, any>, options?: Omit<RequestOptions, 'url' | 'method'>): Promise<T> {
  return request({
    url,
    method: 'GET',
    data,
    ...options
  })
}

export function post<T = any>(url: string, data?: Record<string, any>, options?: Omit<RequestOptions, 'url' | 'method'>): Promise<T> {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

export function put<T = any>(url: string, data?: Record<string, any>, options?: Omit<RequestOptions, 'url' | 'method'>): Promise<T> {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

export function del<T = any>(url: string, data?: Record<string, any>, options?: Omit<RequestOptions, 'url' | 'method'>): Promise<T> {
  return request({
    url,
    method: 'DELETE',
    data,
    ...options
  })
}