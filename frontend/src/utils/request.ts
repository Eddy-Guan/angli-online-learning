const BASE_URL = '/api'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, any>
  header?: Record<string, string>
  showLoading?: boolean
  showError?: boolean
}

export function request<T = any>(options: RequestOptions): Promise<T> {
  const { showLoading = true, showError = true, url, method = 'GET', data, header: customHeader } = options
  
  return new Promise((resolve, reject) => {
    if (showLoading) {
      uni.showLoading({ title: '加载中...' })
    }
    
    const token = uni.getStorageSync('token')
    const header: Record<string, string> = {
      'Content-Type': 'application/json',
      ...customHeader
    }
    
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }
    
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header,
      success: (res) => {
        if (showLoading) {
          uni.hideLoading()
        }
        
        const responseData = res.data as { code: number; message: string; data: T }
        
        if (responseData.code === 200) {
          resolve(responseData.data)
        } else {
          if (showError) {
            uni.showToast({
              title: responseData.message || '请求失败',
              icon: 'none'
            })
          }
          reject(responseData)
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