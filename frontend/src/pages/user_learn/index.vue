<template>
  <view class="learn-page">
    <view class="header">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <text class="nav-title">学习中心</text>
      </view>
    </view>
    
    <scroll-view class="content" scroll-y>
      <view class="section">
        <view class="section-header">
          <text class="section-title">我的课程</text>
        </view>
        
        <view class="learning-list" v-if="learningCourses.length > 0">
          <view 
            v-for="course in learningCourses" 
            :key="course.id" 
            :class="['learning-card', { ended: course.status === 'ENDED' }]"
            @click="goToLearnCourse(course.id)"
          >
            <image class="learning-cover" :src="course.coverImage" mode="aspectFill" />
            <view class="course-end-tag" v-if="course.status === 'ENDED'">已结束</view>
            <view class="learning-info">
              <text class="learning-title ellipsis">{{ course.title }}</text>
              <text class="learning-teacher">{{ course.teacherName }}</text>
              <view class="learning-progress" v-if="course.status !== 'ENDED'">
                <view class="progress-bar">
                  <view class="progress-fill" :style="{ width: course.progress + '%' }"></view>
                </view>
                <text class="progress-text">{{ course.progress }}%</text>
              </view>
              <view class="learning-next" v-if="course.status !== 'ENDED'">
                <text class="next-text">继续学习</text>
                <text class="next-arrow">→</text>
              </view>
              <view class="course-end-date" v-if="course.status === 'ENDED'">
                <text>课程已于{{ course.endDate }}结束</text>
              </view>
              <view class="evaluate-btn" v-if="course.canEvaluate" @click.stop="goToEvaluate(course.id)">
                <text>去评价</text>
              </view>
            </view>
          </view>
        </view>
        
        <view class="empty-state" v-else>
          <text class="empty-icon">📚</text>
          <text class="empty-title">还没有学习的课程</text>
          <text class="empty-sub">快去选课中心挑选心仪的课程吧</text>
          <view class="btn-primary" style="margin-top: 32rpx;" @click="goToCourseCenter">去选课</view>
        </view>
      </view>
      
      <view class="section">
        <view class="section-header">
          <text class="section-title">学习计划</text>
        </view>
        
        <view class="plan-card">
          <view class="plan-header">
            <text class="plan-title">本周学习目标</text>
            <text class="plan-date">{{ weekRange }}</text>
          </view>
          <view class="plan-stats">
            <view class="plan-stat">
              <text class="stat-num">{{ planStats.targetHours }}</text>
              <text class="stat-label">目标时长(小时)</text>
            </view>
            <view class="plan-stat">
              <text class="stat-num">{{ planStats.completedHours }}</text>
              <text class="stat-label">已完成</text>
            </view>
            <view class="plan-stat">
              <text class="stat-num">{{ planStats.remainingHours }}</text>
              <text class="stat-label">剩余</text>
            </view>
          </view>
          <view class="plan-progress">
            <view class="plan-progress-bar">
              <view class="plan-progress-fill" :style="{ width: planProgressPercent + '%' }"></view>
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
import { getOrdersByUserId } from '@/api/order'
import { getCourseById, type Course } from '@/api/course'

interface LearningCourse extends Course {
  progress: number
  endDate?: string
  canEvaluate: boolean
}

const userStore = useUserStore()

const learningCourses = ref<LearningCourse[]>([])
const planStats = ref({
  targetHours: 10,
  completedHours: 6,
  remainingHours: 4
})

const planProgressPercent = computed(() => {
  return Math.round((planStats.value.completedHours / planStats.value.targetHours) * 100)
})

const weekRange = computed(() => {
  const now = new Date()
  const day = now.getDay() || 7
  const monday = new Date(now)
  monday.setDate(now.getDate() - day + 1)
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)
  
  return `${monday.getMonth() + 1}/${monday.getDate()} - ${sunday.getMonth() + 1}/${sunday.getDate()}`
})

onMounted(async () => {
  userStore.loadFromStorage()
  
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/user_login/index' })
    return
  }
  
  await loadLearningCourses()
})

async function loadLearningCourses() {
  if (!userStore.userInfo) return
  
  try {
    const orders = await getOrdersByUserId(userStore.userInfo.userId)
    const paidOrders = orders.filter(o => o.status === 'PAID')
    
    const courses: LearningCourse[] = []
    for (const order of paidOrders) {
      try {
        const course = await getCourseById(order.courseId)
        courses.push({
          ...course,
          progress: Math.floor(Math.random() * 100),
          endDate: course.status === 'ENDED' ? '2024-01-15' : undefined,
          canEvaluate: course.status === 'PUBLISHED' && Math.random() > 0.5
        })
      } catch {
        console.error(`Failed to load course ${order.courseId}`)
      }
    }
    
    learningCourses.value = courses
  } catch (err) {
    console.error('Load learning courses failed:', err)
  }
}

function goToLearnCourse(id: number) {
  uni.showToast({ title: '课程学习功能开发中', icon: 'none' })
}

function goToEvaluate(courseId: number) {
  uni.showToast({ title: '评价功能开发中', icon: 'none' })
}

function goToCourseCenter() {
  uni.switchTab({ url: '/pages/user_course/index' })
}
</script>

<style lang="scss" scoped>
.learn-page {
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
  padding: 20rpx 32rpx;
  text-align: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $text-primary;
}

.content {
  height: calc(100vh - var(--status-bar-height, 44px) - 80rpx - 120rpx);
}

.section {
  padding: 0 32rpx;
  margin-bottom: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  color: $text-primary;
}

.section-more {
  font-size: 26rpx;
  color: $text-secondary;
}

.learning-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  gap: 24rpx;
  margin-bottom: 20rpx;
  position: relative;
  
  &:active {
    background: #f8f8f8;
  }
  
  &.ended {
    opacity: 0.7;
  }
}

.course-end-tag {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  background: #999;
  color: #fff;
  font-size: 20rpx;
  padding: 6rpx 16rpx;
  border-radius: 6rpx;
}

.learning-cover {
  width: 160rpx;
  height: 120rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.learning-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.learning-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $text-primary;
}

.learning-teacher {
  font-size: 24rpx;
  color: $text-muted;
  margin-top: 8rpx;
}

.learning-progress {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 16rpx;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background: $border-color;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $primary-color, $primary-dark);
  border-radius: 6rpx;
  transition: width 0.3s;
}

.progress-text {
  font-size: 24rpx;
  color: $text-secondary;
  min-width: 80rpx;
  text-align: right;
}

.learning-next {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
  margin-top: auto;
}

.next-text {
  font-size: 26rpx;
  color: $primary-color;
}

.next-arrow {
  font-size: 26rpx;
  color: $primary-color;
}

.course-end-date {
  font-size: 24rpx;
  color: #999;
  margin-top: auto;
}

.evaluate-btn {
  background: $primary-color;
  color: #fff;
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
  text-align: center;
  margin-top: 16rpx;
  align-self: flex-start;
}

.empty-state {
  background: #fff;
  border-radius: 16rpx;
  padding: 80rpx 48rpx;
  text-align: center;
}

.empty-icon {
  font-size: 96rpx;
  display: block;
}

.empty-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: $text-primary;
  margin-top: 24rpx;
}

.empty-sub {
  display: block;
  font-size: 26rpx;
  color: $text-muted;
  margin-top: 12rpx;
}

.plan-card {
  background: linear-gradient(135deg, $primary-color, $primary-dark);
  border-radius: 16rpx;
  padding: 32rpx;
  color: #fff;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plan-title {
  font-size: 32rpx;
  font-weight: 600;
}

.plan-date {
  font-size: 24rpx;
  opacity: 0.85;
}

.plan-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 40rpx;
}

.plan-stat {
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
}

.stat-label {
  font-size: 24rpx;
  opacity: 0.85;
  margin-top: 8rpx;
  display: block;
}

.plan-progress {
  margin-top: 32rpx;
}

.plan-progress-bar {
  height: 16rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 8rpx;
  overflow: hidden;
}

.plan-progress-fill {
  height: 100%;
  background: #fff;
  border-radius: 8rpx;
  transition: width 0.3s;
}

.bottom-space {
  height: 160rpx;
}
</style>
