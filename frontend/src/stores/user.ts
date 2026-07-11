import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, type LoginData, type RegisterData, type LoginResponse } from '@/api/auth'

export interface UserInfo {
  userId: number
  phone: string
  realName: string
  role: string
  status: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)
  
  const isLoggedIn = computed(() => !!token.value)
  const isParent = computed(() => userInfo.value?.role === 'PARENT')
  const isTeacher = computed(() => userInfo.value?.role === 'TEACHER')
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  
  function setToken(newToken: string) {
    token.value = newToken
    uni.setStorageSync('token', newToken)
  }
  
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    uni.setStorageSync('userInfo', JSON.stringify(info))
  }
  
  async function handleLogin(data: LoginData): Promise<void> {
    const result = await login(data)
    setToken(result.token)
    setUserInfo({
      userId: result.userId,
      phone: data.phone,
      realName: result.realName,
      role: result.role,
      status: 'ACTIVE'
    })
  }
  
  async function handleRegister(data: RegisterData): Promise<void> {
    await register(data)
  }
  
  function logout() {
    token.value = ''
    userInfo.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    uni.navigateTo({ url: '/pages/user_login/index' })
  }
  
  function loadFromStorage() {
    const savedToken = uni.getStorageSync('token')
    const savedUserInfo = uni.getStorageSync('userInfo')
    
    if (savedToken) {
      token.value = savedToken
    }
    
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch {
        userInfo.value = null
      }
    }
  }
  
  return {
    token,
    userInfo,
    isLoggedIn,
    isParent,
    isTeacher,
    isAdmin,
    setToken,
    setUserInfo,
    handleLogin,
    handleRegister,
    logout,
    loadFromStorage
  }
})
