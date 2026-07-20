<template>
  <view class="home-page">
    <view class="header">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <view class="user-info">
          <view class="avatar">
            <text class="avatar-text">{{ userStore.userInfo?.realName?.charAt(0) || 'U' }}</text>
          </view>
          <view class="greeting">
            <text class="greeting-text">你好，{{ userStore.userInfo?.realName || '家长' }}</text>
            <text class="greeting-sub">{{ currentDate }}</text>
          </view>
        </view>
        <view class="notification" @click="goToNotifications">
          <text class="notification-icon">🔔</text>
          <view class="badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
        </view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="banner">
        <swiper class="banner-swiper" indicator-dots circular autoplay interval="4000">
          <swiper-item v-for="(item, index) in banners" :key="index">
            <view class="banner-item" :style="{ background: item.color }">
              <view class="banner-content">
                <text class="banner-title">{{ item.title }}</text>
                <text class="banner-sub">{{ item.sub }}</text>
              </view>
            </view>
          </swiper-item>
        </swiper>
      </view>

      <view class="checkin-card" @click="handleCheckin" v-if="!checkinStatus.hasChecked">
        <view class="checkin-left">
          <text class="checkin-icon">📅</text>
          <view class="checkin-info">
            <text class="checkin-title">今日打卡</text>
            <text class="checkin-days">已连续打卡 {{ checkinStatus.continuousDays }} 天</text>
          </view>
        </view>
        <view class="checkin-btn">立即打卡</view>
      </view>
      <view class="checkin-card checked" v-else>
        <view class="checkin-left">
          <text class="checkin-icon">✅</text>
          <view class="checkin-info">
            <text class="checkin-title">今日已打卡</text>
            <text class="checkin-days">已连续打卡 {{ checkinStatus.continuousDays }} 天</text>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">热门推荐</text>
          <text class="section-more" @click="goToCourseList">更多 →</text>
        </view>
        <scroll-view class="course-scroll" scroll-x>
          <view class="course-list">
            <view v-for="course in recommendedCourses" :key="course.id" class="course-card"
              @click="goToCourseDetail(course.id)">
              <image class="course-cover" :src="course.coverImage || '/api/images/default-course.svg'"
                mode="aspectFill" />
              <view class="course-info">
                <text class="course-title ellipsis">{{ course.title }}</text>
                <text class="course-teacher">{{ course.teacherName }}</text>
                <view class="course-bottom">
                  <text class="course-price">¥{{ course.price }}</text>
                  <text class="course-students">{{ course.enrollmentCount }}人已学</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">学习数据</text>
        </view>
        <view class="stats-card">
          <view class="stat-item">
            <text class="stat-value">{{ learningStats.totalHours }}</text>
            <text class="stat-label">累计学习时长(小时)</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ learningStats.completedLessons }}</text>
            <text class="stat-label">已完成课程(节)</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ learningStats.streakDays }}</text>
            <text class="stat-label">连续学习(天)</text>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">快捷功能</text>
        </view>
        <view class="quick-links">
          <view class="quick-item" v-for="item in quickItems" :key="item.key" @click="handleQuickLink(item.key)">
            <view class="quick-icon">{{ item.icon }}</view>
            <text class="quick-text">{{ item.label }}</text>
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
import { getRecommendedCourses, type Course } from '@/api/course'
import { getCheckinStatus, checkin, type CheckinStatus } from '@/api/checkin'
import { getLearningStats, type LearningStats } from '@/api/learning'
import { getUnreadCount } from '@/api/message'

const userStore = useUserStore()

const banners = [
  { title: '新学期开学季', sub: '限时优惠，报名立减200元', color: 'linear-gradient(135deg, #FF6B6B, #FF8E53)' },
  { title: '精品课程推荐', sub: '名师授课，助力成长', color: 'linear-gradient(135deg, #4ECDC4, #44A08D)' },
  { title: '学习打卡挑战', sub: '坚持21天，养成好习惯', color: 'linear-gradient(135deg, #45B7D1, #2980B9)' }
]

const quickItems = [
  { key: 'bindChild', icon: '👶', label: '绑定孩子' },
  { key: 'orders', icon: '📋', label: '我的订单' },
  { key: 'certificates', icon: '🏆', label: '荣誉证书' },
  { key: 'help', icon: '💬', label: '在线客服' }
]

const recommendedCourses = ref<Course[]>([])
const checkinStatus = ref<CheckinStatus>({ hasChecked: false, continuousDays: 0 })
const learningStats = ref<LearningStats>({ totalHours: 0, completedLessons: 0, streakDays: 0 })
const unreadCount = ref(0)

const currentDate = computed(() => {
  const now = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
})

onMounted(async () => {
  userStore.loadFromStorage()

  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/user_login/index' })
    return
  }

  await loadData()
})

async function loadData() {
  try {
    const courses = await getRecommendedCourses()
    recommendedCourses.value = courses

    if (userStore.userInfo) {
      const [status, stats, unreadData] = await Promise.all([
        getCheckinStatus(userStore.userInfo.userId),
        getLearningStats(userStore.userInfo.userId),
        getUnreadCount(userStore.userInfo.userId)
      ])
      checkinStatus.value = status
      learningStats.value = stats
      unreadCount.value = unreadData.count || 0
    }
  } catch (err) {
    console.error('Load data failed:', err)
  }
}

async function handleCheckin() {
  if (!userStore.userInfo || checkinStatus.value.hasChecked) return

  try {
    const result = await checkin(userStore.userInfo.userId)
    checkinStatus.value = {
      hasChecked: result.success,
      continuousDays: result.continuousDays
    }

    uni.showToast({ title: '打卡成功', icon: 'success' })
  } catch (err) {
    console.error('Checkin failed:', err)
  }
}

function goToNotifications() {
  uni.navigateTo({ url: '/pages/user_notifications/index' })
}

function goToCourseList() {
  uni.switchTab({ url: '/pages/user_course/index' })
}

function goToCourseDetail(id: number) {
  uni.navigateTo({ url: `/pages/user_course_detail/index?id=${id}` })
}

function handleQuickLink(key: string) {
  switch (key) {
    case 'bindChild':
      uni.navigateTo({ url: '/pages/user_bind-child/index' })
      break
    case 'orders':
      uni.showToast({ title: '订单功能开发中', icon: 'none' })
      break
    case 'certificates':
      uni.showToast({ title: '证书功能开发中', icon: 'none' })
      break
    case 'help':
      uni.showToast({ title: '客服功能开发中', icon: 'none' })
      break
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: $bg-color;
}

.header {
  background: linear-gradient(135deg, $primary-color, $primary-dark);
}

.status-bar {
  height: var(--status-bar-height, 44px);
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 32rpx;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 36rpx;
  color: #fff;
  font-weight: 600;
}

.greeting {
  .greeting-text {
    display: block;
    font-size: 32rpx;
    color: #fff;
    font-weight: 600;
  }

  .greeting-sub {
    display: block;
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.8);
    margin-top: 4rpx;
  }
}

.notification {
  position: relative;
  font-size: 44rpx;

  .badge {
    position: absolute;
    top: -8rpx;
    right: -8rpx;
    background: #FF4757;
    color: #fff;
    font-size: 20rpx;
    padding: 2rpx 10rpx;
    border-radius: 16rpx;
    min-width: 32rpx;
    text-align: center;
  }
}

.content {
  height: calc(100vh - var(--status-bar-height, 44px) - 120rpx - 120rpx);
}

.banner {
  padding: 32rpx;
}

.banner-swiper {
  height: 280rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.banner-item {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
  padding: 40rpx;
}

.banner-content {
  .banner-title {
    display: block;
    font-size: 40rpx;
    color: #fff;
    font-weight: 700;
    margin-bottom: 12rpx;
  }

  .banner-sub {
    display: block;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.9);
  }
}

.checkin-card {
  margin: 0 32rpx 32rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 2rpx solid $primary-color;

  &.checked {
    border-color: #52c41a;

    .checkin-title {
      color: #52c41a;
    }
  }
}

.checkin-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.checkin-icon {
  font-size: 48rpx;
}

.checkin-info {
  .checkin-title {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: $primary-color;
  }

  .checkin-days {
    display: block;
    font-size: 24rpx;
    color: $text-muted;
    margin-top: 8rpx;
  }
}

.checkin-btn {
  background: linear-gradient(135deg, $primary-color, $primary-dark);
  color: #fff;
  padding: 20rpx 40rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;

  &:active {
    opacity: 0.85;
  }
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

  &:active {
    color: $primary-color;
  }
}

.course-scroll {
  white-space: nowrap;
}

.course-list {
  display: inline-flex;
  gap: 24rpx;
  padding-right: 32rpx;
}

.course-card {
  width: 320rpx;
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  display: inline-block;
  vertical-align: top;
}

.course-cover {
  width: 100%;
  height: 200rpx;
}

.course-info {
  padding: 20rpx;
}

.course-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 8rpx;
}

.course-teacher {
  font-size: 24rpx;
  color: $text-muted;
  display: block;
  margin-bottom: 16rpx;
}

.course-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-price {
  font-size: 32rpx;
  font-weight: 700;
  color: $primary-color;
}

.course-students {
  font-size: 22rpx;
  color: $text-muted;
}

.stats-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 32rpx;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: $primary-color;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: $text-muted;
}

.stat-divider {
  width: 2rpx;
  height: 80rpx;
  background: $border-color;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32rpx;
}

.quick-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx 0;
  text-align: center;
}

.quick-icon {
  font-size: 56rpx;
  margin-bottom: 16rpx;
}

.quick-text {
  font-size: 26rpx;
  color: $text-secondary;
}

.bottom-space {
  height: 160rpx;
}
</style>