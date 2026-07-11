<template>
  <view class="login-page">
    <view class="header">
      <view class="logo">
        <text class="logo-text">昂立教育</text>
        <text class="logo-sub">在线学习平台</text>
      </view>
    </view>
    
    <view class="form-container">
      <view class="form-item">
        <view class="input-group">
          <text class="input-icon">📱</text>
          <input 
            v-model="phone" 
            type="number" 
            placeholder="请输入手机号" 
            class="input"
            maxlength="11"
          />
        </view>
      </view>
      
      <view class="form-item">
        <view class="input-group">
          <text class="input-icon">🔒</text>
          <input 
            v-model="password" 
            type="password" 
            placeholder="请输入密码" 
            class="input"
            :password="!showPassword"
          />
          <text class="toggle-pwd" @click="showPassword = !showPassword">
            {{ showPassword ? '🙈' : '👁️' }}
          </text>
        </view>
      </view>
      
      <view class="form-item" v-if="isRegister">
        <view class="input-group">
          <text class="input-icon">🔑</text>
          <input 
            v-model="confirmPassword" 
            type="password" 
            placeholder="请确认密码" 
            class="input"
            :password="!showPassword"
          />
        </view>
      </view>
      
      <view class="role-select">
        <text class="role-label">选择角色：</text>
        <view class="role-options">
          <view 
            v-for="role in roles" 
            :key="role.value"
            :class="['role-option', { active: selectedRole === role.value }]"
            @click="selectedRole = role.value"
          >
            {{ role.label }}
          </view>
        </view>
      </view>
      
      <view class="btn-primary" @click="handleSubmit" :disabled="!canSubmit">
        {{ isRegister ? '注册' : '登录' }}
      </view>
      
      <view class="register-link" @click="toggleRegister">
        {{ isRegister ? '已有账号？立即登录' : '还没有账号？立即注册' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const selectedRole = ref('PARENT')
const isRegister = ref(false)

const roles = [
  { value: 'PARENT', label: '家长' },
  { value: 'TEACHER', label: '教师' },
  { value: 'ADMIN', label: '管理员' }
]

const canSubmit = computed(() => {
  if (isRegister.value) {
    return phone.value.length === 11 && password.value.length >= 6 && password.value === confirmPassword.value
  }
  return phone.value.length === 11 && password.value.length >= 6
})

async function handleSubmit() {
  if (!canSubmit.value) {
    if (isRegister.value) {
      uni.showToast({ title: '请输入正确的信息', icon: 'none' })
    } else {
      uni.showToast({ title: '请输入正确的手机号和密码', icon: 'none' })
    }
    return
  }
  
  try {
    if (isRegister.value) {
      await userStore.handleRegister({
        phone: phone.value,
        password: password.value,
        realName: phone.value,
        role: selectedRole.value
      })
      
      uni.showToast({ title: '注册成功', icon: 'success' })
      setTimeout(() => {
        isRegister.value = false
        phone.value = ''
        password.value = ''
        confirmPassword.value = ''
      }, 1500)
    } else {
      await userStore.handleLogin({
        phone: phone.value,
        password: password.value
      })
      
      uni.showToast({ title: '登录成功', icon: 'success' })
      
      if (userStore.isParent) {
        uni.switchTab({ url: '/pages/user_home/index' })
      } else if (userStore.isTeacher) {
        uni.navigateTo({ url: '/pages/teacher_dashboard/index' })
      } else if (userStore.isAdmin) {
        uni.navigateTo({ url: '/pages/admin_dashboard/index' })
      }
    }
  } catch (err: any) {
    console.error('Submit failed:', err)
    uni.showToast({ title: err.message || '操作失败', icon: 'none' })
  }
}

function toggleRegister() {
  isRegister.value = !isRegister.value
  phone.value = ''
  password.value = ''
  confirmPassword.value = ''
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #F05A22 0%, #ff7a45 100%);
  padding: 0 32rpx;
  display: flex;
  flex-direction: column;
}

.header {
  padding-top: 180rpx;
  text-align: center;
}

.logo {
  .logo-text {
    display: block;
    font-size: 64rpx;
    font-weight: 700;
    color: #fff;
    letter-spacing: 8rpx;
  }
  .logo-sub {
    display: block;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.85);
    margin-top: 16rpx;
  }
}

.form-container {
  flex: 1;
  margin-top: 100rpx;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 48rpx 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.input-group {
  display: flex;
  align-items: center;
  background: #F5F5F7;
  border-radius: 12rpx;
  padding: 0 24rpx;
  height: 96rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
  
  &:focus-within {
    border-color: $primary-color;
    background: #fff;
  }
}

.input-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.input {
  flex: 1;
  font-size: 30rpx;
  color: $text-primary;
}

.toggle-pwd {
  font-size: 32rpx;
  padding: 8rpx;
}

.role-select {
  margin-bottom: 48rpx;
  
  .role-label {
    display: block;
    font-size: 28rpx;
    color: $text-secondary;
    margin-bottom: 16rpx;
  }
}

.role-options {
  display: flex;
  gap: 24rpx;
}

.role-option {
  flex: 1;
  text-align: center;
  padding: 24rpx;
  background: #F5F5F7;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: $text-secondary;
  border: 2rpx solid transparent;
  transition: all 0.3s;
  
  &.active {
    background: $primary-light;
    color: $primary-color;
    border-color: $primary-color;
  }
}

.btn-primary {
  margin-bottom: 32rpx;
}

.register-link {
  text-align: center;
  font-size: 28rpx;
  color: $text-secondary;
  
  &:active {
    color: $primary-color;
  }
}
</style>