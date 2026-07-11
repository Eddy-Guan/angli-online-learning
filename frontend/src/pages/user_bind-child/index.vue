<template>
  <view class="bind-child-page">
    <view class="header">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <view class="back-btn" @click="goBack">
          <text>←</text>
        </view>
        <text class="nav-title">绑定孩子</text>
        <view class="placeholder"></view>
      </view>
    </view>
    
    <scroll-view class="content" scroll-y>
      <view class="form-card">
        <view class="form-item">
          <text class="form-label">孩子姓名 <text class="required">*</text></text>
          <input 
            v-model="form.name" 
            placeholder="请输入孩子姓名" 
            class="form-input"
          />
        </view>
        
        <view class="form-item">
          <text class="form-label">年龄</text>
          <input 
            v-model="form.age" 
            type="number"
            placeholder="请输入年龄" 
            class="form-input"
          />
        </view>
        
        <view class="form-item">
          <text class="form-label">年级</text>
          <picker :value="gradeIndex" :range="grades" @change="onGradeChange">
            <view class="picker-input">
              <text>{{ grades[gradeIndex] || '请选择年级' }}</text>
              <text class="picker-arrow">▼</text>
            </view>
          </picker>
        </view>
        
        <view class="form-item">
          <text class="form-label">性别</text>
          <view class="gender-options">
            <view 
              :class="['gender-option', { active: form.gender === 'MALE' }]"
              @click="form.gender = 'MALE'"
            >
              男
            </view>
            <view 
              :class="['gender-option', { active: form.gender === 'FEMALE' }]"
              @click="form.gender = 'FEMALE'"
            >
              女
            </view>
          </view>
        </view>
        
        <view class="btn-primary" @click="handleBind" :disabled="!canSubmit">
          确认绑定
        </view>
      </view>
      
      <view class="section-card" v-if="children.length > 0">
        <view class="section-header">
          <text class="section-title">已绑定的孩子</text>
        </view>
        <view class="children-list">
          <view class="child-item" v-for="child in children" :key="child.id">
            <view class="child-avatar">
              <text>{{ child.gender === 'MALE' ? '👦' : '👧' }}</text>
            </view>
            <view class="child-info">
              <text class="child-name">{{ child.name }}</text>
              <text class="child-detail">{{ child.grade }} · {{ child.age }}岁</text>
            </view>
            <view class="child-actions">
              <text class="action-btn" @click="editChild(child)">编辑</text>
              <text class="action-btn delete" @click="deleteChild(child.id)">删除</text>
            </view>
          </view>
        </view>
      </view>
      
      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { addStudent, getStudentsByParentId, deleteStudent, type Student, type StudentCreateRequest } from '@/api/student'

const userStore = useUserStore()

const grades = ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级', '初一', '初二', '初三', '高一', '高二', '高三']
const gradeIndex = ref(-1)

const form = ref<StudentCreateRequest & { gender: string }>({
  name: '',
  age: undefined,
  grade: undefined,
  gender: ''
})

const children = ref<Student[]>([])

const canSubmit = computed(() => {
  return form.value.name.trim().length > 0
})

onMounted(async () => {
  userStore.loadFromStorage()
  
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/user_login/index' })
    return
  }
  
  await loadChildren()
})

async function loadChildren() {
  if (!userStore.userInfo) return
  
  try {
    const result = await getStudentsByParentId(userStore.userInfo.userId)
    children.value = result
  } catch (err) {
    console.error('Load children failed:', err)
  }
}

function onGradeChange(e: any) {
  gradeIndex.value = e.detail.value
  form.value.grade = grades[e.detail.value]
}

async function handleBind() {
  if (!userStore.userInfo) return
  
  try {
    await addStudent(userStore.userInfo.userId, {
      name: form.value.name,
      age: form.value.age,
      grade: form.value.grade,
      gender: form.value.gender
    })
    
    uni.showToast({ title: '绑定成功', icon: 'success' })
    
    form.value = { name: '', age: undefined, grade: undefined, gender: '' }
    gradeIndex.value = -1
    
    await loadChildren()
  } catch (err: any) {
    uni.showToast({ title: err.message || '绑定失败', icon: 'none' })
  }
}

function editChild(child: Student) {
  uni.showToast({ title: '编辑功能开发中', icon: 'none' })
}

async function deleteChild(id: number) {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个孩子的绑定信息吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteStudent(id)
          await loadChildren()
          uni.showToast({ title: '删除成功', icon: 'success' })
        } catch (err: any) {
          uni.showToast({ title: err.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

function goBack() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.bind-child-page {
  min-height: 100vh;
  background: $bg-color;
}

.header {
  background: #fff;
}

.status-bar {
  height: var(--status-bar-height, 44px);
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 32rpx;
}

.back-btn {
  font-size: 40rpx;
  color: $text-primary;
  padding: 12rpx;
}

.placeholder {
  width: 64rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: $text-primary;
}

.content {
  height: calc(100vh - var(--status-bar-height, 44px) - 80rpx);
}

.form-card {
  background: #fff;
  margin: 24rpx 32rpx;
  padding: 32rpx;
  border-radius: 16rpx;
}

.form-item {
  margin-bottom: 32rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: $text-primary;
  margin-bottom: 16rpx;
  
  .required {
    color: #FF4757;
  }
}

.form-input {
  width: 100%;
  height: 88rpx;
  background: $bg-color;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: $text-primary;
}

.picker-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  background: $bg-color;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: $text-primary;
  
  .picker-arrow {
    font-size: 20rpx;
    color: $text-muted;
  }
}

.gender-options {
  display: flex;
  gap: 24rpx;
}

.gender-option {
  flex: 1;
  text-align: center;
  padding: 28rpx;
  background: $bg-color;
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
  margin-top: 48rpx;
}

.section-card {
  background: #fff;
  margin: 24rpx 32rpx;
  padding: 32rpx;
  border-radius: 16rpx;
}

.section-header {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-primary;
}

.children-list {
  border-top: 2rpx solid $border-color;
}

.child-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 2rpx solid $border-color;
  
  &:last-child {
    border-bottom: none;
  }
}

.child-avatar {
  width: 80rpx;
  height: 80rpx;
  background: $bg-color;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
}

.child-info {
  flex: 1;
  margin-left: 20rpx;
  
  .child-name {
    display: block;
    font-size: 30rpx;
    font-weight: 600;
    color: $text-primary;
  }
  
  .child-detail {
    display: block;
    font-size: 24rpx;
    color: $text-muted;
    margin-top: 8rpx;
  }
}

.child-actions {
  display: flex;
  gap: 24rpx;
}

.action-btn {
  font-size: 26rpx;
  color: $primary-color;
  
  &.delete {
    color: #FF4757;
  }
}

.bottom-space {
  height: 80rpx;
}
</style>
