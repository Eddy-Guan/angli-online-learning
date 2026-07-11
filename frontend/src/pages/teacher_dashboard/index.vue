<template>
  <view class="teacher-page">
    <view class="header">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <text class="nav-title">教师端</text>
        <view class="logout-btn" @click="handleLogout">
          <text class="logout-icon">🚪</text>
        </view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="stats-section">
        <view class="stat-card">
          <text class="stat-icon">📚</text>
          <text class="stat-value">{{ stats.courses }}</text>
          <text class="stat-label">我的课程</text>
        </view>
        <view class="stat-card">
          <text class="stat-icon">👥</text>
          <text class="stat-value">{{ stats.students }}</text>
          <text class="stat-label">学生数量</text>
        </view>
        <view class="stat-card">
          <text class="stat-icon">📝</text>
          <text class="stat-value">{{ stats.homework }}</text>
          <text class="stat-label">待批作业</text>
        </view>
        <view class="stat-card">
          <text class="stat-icon">📅</text>
          <text class="stat-value">{{ stats.schedule }}</text>
          <text class="stat-label">本周课程</text>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">今日课程</text>
          <text class="section-more">查看全部 →</text>
        </view>

        <view class="schedule-list" v-if="todaySchedule.length > 0">
          <view class="schedule-card" v-for="item in todaySchedule" :key="item.id">
            <view class="schedule-time">
              <text class="time-start">{{ item.startTime }}</text>
              <text class="time-divider">-</text>
              <text class="time-end">{{ item.endTime }}</text>
            </view>
            <view class="schedule-info">
              <text class="schedule-course">{{ item.courseName }}</text>
              <text class="schedule-students">{{ item.studentCount }}名学生</text>
              <view class="schedule-status" :class="item.status.toLowerCase()">
                {{ item.status }}
              </view>
            </view>
          </view>
        </view>

        <view class="empty-card" v-else>
          <text class="empty-icon">📅</text>
          <text class="empty-text">今天没有课程安排</text>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">待批作业</text>
        </view>

        <view class="homework-list" v-if="homeworkList.length > 0">
          <view class="homework-card" v-for="item in homeworkList" :key="item.id" @click="goToGradeHomework(item.id)">
            <view class="homework-info">
              <text class="homework-course">{{ item.courseName }}</text>
              <text class="homework-date">{{ item.assignmentDate }}</text>
            </view>
            <view class="homework-status">
              <text class="homework-count">{{ item.submittedCount }}/{{ item.totalCount }}已提交</text>
              <text class="homework-arrow">→</text>
            </view>
          </view>
        </view>

        <view class="empty-card" v-else>
          <text class="empty-icon">📝</text>
          <text class="empty-text">没有待批作业</text>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">快捷操作</text>
        </view>

        <view class="quick-grid">
          <view class="quick-item" @click="goToAddCourse">
            <text class="quick-icon">➕</text>
            <text class="quick-text">添加课程</text>
          </view>
          <view class="quick-item" @click="goToStudentList">
            <text class="quick-icon">👥</text>
            <text class="quick-text">学生列表</text>
          </view>
          <view class="quick-item" @click="goToGradeBook">
            <text class="quick-icon">📊</text>
            <text class="quick-text">成绩管理</text>
          </view>
          <view class="quick-item" @click="goToAttendance">
            <text class="quick-icon">✅</text>
            <text class="quick-text">考勤记录</text>
          </view>
        </view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTeacherStats, getTodaySchedule, getPendingHomework, getTeacherCourses, type TeacherStats, type ScheduleItem, type HomeworkItem } from '@/api/teacher'

const userStore = useUserStore()

const stats = ref<TeacherStats>({
  courses: 0,
  students: 0,
  homework: 0,
  schedule: 0
})

const todaySchedule = ref<ScheduleItem[]>([])
const homeworkList = ref<HomeworkItem[]>([])

onMounted(() => {
  userStore.loadFromStorage()

  if (!userStore.isTeacher) {
    uni.navigateTo({ url: '/pages/user_login/index' })
    return
  }

  loadData()
})

async function loadData() {
  if (!userStore.userInfo) return

  const teacherId = userStore.userInfo.userId

  try {
    const [teacherStats, schedule, homework] = await Promise.all([
      getTeacherStats(teacherId),
      getTodaySchedule(teacherId),
      getPendingHomework(teacherId)
    ])

    stats.value = teacherStats
    todaySchedule.value = schedule
    homeworkList.value = homework
  } catch (err) {
    console.error('Load teacher data failed:', err)
  }
}

function handleLogout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
      }
    }
  })
}

function goToGradeHomework(id: number) {
  uni.showToast({ title: `批改作业功能开发中，作业ID: ${id}`, icon: 'none' })
}

function goToAddCourse() {
  uni.showToast({ title: '添加课程功能开发中', icon: 'none' })
}

function goToStudentList() {
  uni.showToast({ title: '学生列表功能开发中', icon: 'none' })
}

function goToGradeBook() {
  uni.showToast({ title: '成绩管理功能开发中', icon: 'none' })
}

function goToAttendance() {
  uni.showToast({ title: '考勤记录功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.teacher-page {
  min-height: 100vh;
  background: $bg-color;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.2);
  padding: 12rpx;
  border-radius: 50%;
}

.logout-icon {
  font-size: 32rpx;
}

.content {
  height: calc(100vh - var(--status-bar-height, 44px) - 80rpx);
  padding: 32rpx;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 32rpx;
}

.stat-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  text-align: center;
}

.stat-icon {
  font-size: 48rpx;
  display: block;
  margin-bottom: 16rpx;
}

.stat-value {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #667eea;
}

.stat-label {
  font-size: 24rpx;
  color: $text-muted;
  margin-top: 8rpx;
  display: block;
}

.section {
  margin-bottom: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-primary;
}

.section-more {
  font-size: 26rpx;
  color: $text-secondary;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.schedule-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  display: flex;
  gap: 24rpx;
}

.schedule-time {
  text-align: center;
  padding-right: 24rpx;
  border-right: 2rpx solid $border-color;

  .time-start {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: $text-primary;
  }

  .time-divider {
    font-size: 24rpx;
    color: $text-muted;
    margin: 8rpx 0;
    display: block;
  }

  .time-end {
    font-size: 28rpx;
    color: $text-secondary;
  }
}

.schedule-info {
  flex: 1;

  .schedule-course {
    display: block;
    font-size: 30rpx;
    font-weight: 600;
    color: $text-primary;
  }

  .schedule-students {
    display: block;
    font-size: 24rpx;
    color: $text-muted;
    margin-top: 8rpx;
  }

  .schedule-status {
    display: inline-block;
    font-size: 22rpx;
    padding: 6rpx 16rpx;
    border-radius: 12rpx;
    margin-top: 16rpx;

    &.未开始 {
      background: #f0f5ff;
      color: #1677ff;
    }

    &.进行中 {
      background: #f6ffed;
      color: #52c41a;
    }

    &.已结束 {
      background: #f5f5f5;
      color: $text-muted;
    }
  }
}

.homework-list {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
}

.homework-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 2rpx solid $border-color;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #f8f8f8;
  }
}

.homework-info {
  .homework-course {
    display: block;
    font-size: 30rpx;
    font-weight: 600;
    color: $text-primary;
  }

  .homework-date {
    display: block;
    font-size: 24rpx;
    color: $text-muted;
    margin-top: 8rpx;
  }
}

.homework-status {
  display: flex;
  align-items: center;
  gap: 12rpx;

  .homework-count {
    font-size: 26rpx;
    color: $text-secondary;
  }

  .homework-arrow {
    font-size: 28rpx;
    color: $text-muted;
  }
}

.empty-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 60rpx;
  text-align: center;

  .empty-icon {
    font-size: 80rpx;
    display: block;
  }

  .empty-text {
    font-size: 28rpx;
    color: $text-muted;
    margin-top: 16rpx;
    display: block;
  }
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.quick-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx 0;
  text-align: center;

  &:active {
    background: #f8f8f8;
  }
}

.quick-icon {
  font-size: 56rpx;
  display: block;
  margin-bottom: 12rpx;
}

.quick-text {
  font-size: 24rpx;
  color: $text-secondary;
}

.bottom-space {
  height: 60rpx;
}
</style>