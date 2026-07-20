<template>
  <view class="teacher-page">
    <view class="hd">
      <text class="hd-title">教师主页</text>
      <view class="hd-actions">
        <view class="hd-notification" @click="goToNotifications">
          <text class="notification-icon">🔔</text>
          <view class="notification-badge" v-if="unreadCount > 0">
            <text class="badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
          </view>
        </view>
        <view class="hd-setting" @click="goToSettings">
          <text class="setting-icon">⚙️</text>
        </view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="pf">
        <view class="av">{{ avatar }}</view>
        <text class="pf-name">{{ teacherName }}</text>
        <text class="pf-tags">{{ tagsText }}</text>
        <view class="stat">
          <view class="stat-item">
            <text class="stat-value">{{ stats.courseCount }}</text>
            <text class="stat-label">授课数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ stats.studentCount }}</text>
            <text class="stat-label">学生数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ stats.averageRating }}</text>
            <text class="stat-label">评分</text>
          </view>
        </view>
      </view>

      <view class="cr" @click="goToTodos">
        <view class="th">🔔</view>
        <view class="in">
          <text class="cr-title">今日待办</text>
          <text class="cr-meta">{{ todoText }}</text>
        </view>
        <text class="cr-arrow">›</text>
      </view>

      <view class="ps">
        <text class="ps-title">📊 教学数据</text>
        <view class="data-grid">
          <view class="data-item">
            <text class="data-value">{{ stats.attendanceRate }}%</text>
            <text class="data-label">到课率</text>
          </view>
          <view class="data-item">
            <text class="data-value">{{ stats.homeworkSubmitRate }}%</text>
            <text class="data-label">作业提交</text>
          </view>
          <view class="data-item">
            <text class="data-value">{{ stats.parentRating }}</text>
            <text class="data-label">家长评分</text>
          </view>
        </view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>

    <nav class="bn">
      <view class="ni on" @click="goToHome">
        <text class="ni-icon">👨‍🏫</text>
        <text class="ni-text">主页</text>
      </view>
      <view class="ni" @click="goToApply">
        <text class="ni-icon">📝</text>
        <text class="ni-text">课程申请</text>
      </view>
      <view class="ni" @click="goToCourses">
        <text class="ni-icon">📚</text>
        <text class="ni-text">课程</text>
      </view>
      <view class="ni" @click="goToPush">
        <text class="ni-icon">📨</text>
        <text class="ni-text">推送</text>
      </view>
    </nav>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTeacherHomeData, type TeacherHomeStats } from '@/api/teacher'
import { getUnreadCount } from '@/api/message'

const userStore = useUserStore()

const teacherName = ref('张伟老师')
const avatar = ref('👨‍🏫')
const tags = ref(['数学', '金牌讲师', '教龄8年'])

const stats = ref<TeacherHomeStats>({
  courseCount: 42,
  studentCount: 128,
  averageRating: 4.9,
  attendanceRate: 86,
  homeworkSubmitRate: 92,
  parentRating: 4.9
})

const todoText = ref('📖 初三数学课 14:00-16:00 · 检查作业 · 推送今日总结')
const unreadCount = ref(0)

const tagsText = computed(() => tags.value.join(' · '))

onMounted(() => {
  userStore.loadFromStorage()
  loadTeacherData()
  loadUnreadCount()
})

async function loadUnreadCount() {
  try {
    const userId = userStore.userInfo?.userId || 0
    const data = await getUnreadCount(userId)
    unreadCount.value = data.count || 0
  } catch {
    unreadCount.value = 0
  }
}

async function loadTeacherData() {
  if (!userStore.userInfo) return

  try {
    const data = await getTeacherHomeData(userStore.userInfo.userId)
    stats.value = data
    if (data.teacherName) {
      teacherName.value = data.teacherName
    }
    if (data.tags && data.tags.length > 0) {
      tags.value = data.tags
    }
  } catch (err) {
    console.error('Load teacher home data failed:', err)
  }
}

function goToSettings() {
  uni.navigateTo({ url: '/pages/teacher_settings/index' })
}

function goToTodos() {
  uni.showToast({ title: '待办列表功能开发中', icon: 'none' })
}

function goToHome() {
  uni.switchTab({ url: '/pages/teacher_home/index' })
}

function goToApply() {
  uni.navigateTo({ url: '/pages/teacher_apply/index' })
}

function goToCourses() {
  uni.navigateTo({ url: '/pages/teacher_courses/index' })
}

function goToPush() {
  uni.navigateTo({ url: '/pages/teacher_push/index' })
}

function goToNotifications() {
  uni.navigateTo({ url: '/pages/teacher_notifications/index' })
}
</script>

<style lang="scss" scoped>
.teacher-page {
  min-height: 100vh;
  background: #F5F5F5;
}

.hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60rpx 32rpx 24rpx;
  background: #fff;
  font-size: 36rpx;
  font-weight: 700;
}

.hd-actions {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.hd-notification {
  font-size: 32rpx;
  position: relative;
}

.notification-badge {
  position: absolute;
  top: -8rpx;
  right: -16rpx;
  background: #F05A22;
  color: #fff;
  font-size: 20rpx;
  min-width: 32rpx;
  height: 32rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6rpx;
}

.badge-text {
  font-weight: 600;
}

.hd-setting {
  font-size: 26rpx;
  color: #999;
}

.pf {
  padding: 40rpx 32rpx;
  background: #fff;
  text-align: center;
}

.av {
  width: 128rpx;
  height: 128rpx;
  background: #fde8e1;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  margin: 0 auto 16rpx;
  color: #F05A22;
}

.pf-name {
  font-size: 36rpx;
  font-weight: 600;
  display: block;
  margin-bottom: 8rpx;
}

.pf-tags {
  font-size: 26rpx;
  color: #999;
  display: block;
  margin-bottom: 24rpx;
}

.stat {
  display: flex;
  justify-content: center;
  gap: 48rpx;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 36rpx;
  font-weight: 700;
  display: block;
  color: #333;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-top: 4rpx;
}

.cr {
  background: #fff;
  margin: 16rpx 32rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  border: 2rpx solid #eee;
  display: flex;
  gap: 24rpx;
  align-items: center;
}

.th {
  width: 120rpx;
  height: 120rpx;
  background: #fde8e1;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  flex-shrink: 0;
}

.in {
  flex: 1;
}

.cr-title {
  font-size: 28rpx;
  font-weight: 600;
  display: block;
  margin-bottom: 4rpx;
}

.cr-meta {
  font-size: 24rpx;
  color: #999;
  display: block;
}

.cr-arrow {
  font-size: 28rpx;
  color: #999;
}

.ps {
  margin: 32rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  border: 2rpx solid #eee;
}

.ps-title {
  font-size: 30rpx;
  font-weight: 600;
  display: block;
  margin-bottom: 24rpx;
}

.data-grid {
  display: flex;
  gap: 24rpx;
}

.data-item {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  background: #fde8e1;
  border-radius: 20rpx;
}

.data-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #F05A22;
  display: block;
}

.data-label {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-top: 4rpx;
}

.bottom-space {
  height: 160rpx;
}

.bn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  padding: 12rpx 0 32rpx;
  background: #fff;
  border-top: 2rpx solid #eee;
  z-index: 10;
}

.ni {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  font-size: 20rpx;
  color: #999;
}

.ni.on {
  color: #F05A22;
}

.ni-icon {
  font-size: 40rpx;
}

.ni-text {
  font-size: 20rpx;
}
</style>