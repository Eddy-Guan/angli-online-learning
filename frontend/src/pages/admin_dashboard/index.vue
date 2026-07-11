<template>
  <view class="admin-page">
    <view class="header">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <text class="nav-title">管理后台</text>
        <view class="logout-btn" @click="handleLogout">
          <text class="logout-icon">🚪</text>
        </view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="stats-section">
        <view class="stat-card">
          <text class="stat-icon">👤</text>
          <text class="stat-value">{{ stats.users }}</text>
          <text class="stat-label">总用户数</text>
        </view>
        <view class="stat-card">
          <text class="stat-icon">📚</text>
          <text class="stat-value">{{ stats.courses }}</text>
          <text class="stat-label">课程数量</text>
        </view>
        <view class="stat-card">
          <text class="stat-icon">👨‍🏫</text>
          <text class="stat-value">{{ stats.teachers }}</text>
          <text class="stat-label">教师数量</text>
        </view>
        <view class="stat-card">
          <text class="stat-icon">💰</text>
          <text class="stat-value">{{ stats.orders }}</text>
          <text class="stat-label">订单总数</text>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">待审核教师</text>
          <text class="section-count">{{ pendingTeachers.length }}</text>
        </view>

        <view class="list-card" v-if="pendingTeachers.length > 0">
          <view class="list-item" v-for="item in pendingTeachers" :key="item.id">
            <view class="item-info">
              <view class="item-avatar">
                <text class="avatar-text">{{ item.realName?.charAt(0) || 'T' }}</text>
              </view>
              <view class="item-detail">
                <text class="item-name">{{ item.realName }}</text>
                <text class="item-phone">{{ item.phone }}</text>
              </view>
            </view>
            <view class="item-actions">
              <view class="action-btn approve" @click="approveTeacher(item.id)">通过</view>
              <view class="action-btn reject" @click="rejectTeacher(item.id)">拒绝</view>
            </view>
          </view>
        </view>

        <view class="empty-card" v-else>
          <text class="empty-icon">✅</text>
          <text class="empty-text">暂无待审核教师</text>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">待上架课程</text>
          <text class="section-count">{{ pendingCourses.length }}</text>
        </view>

        <view class="list-card" v-if="pendingCourses.length > 0">
          <view class="list-item" v-for="item in pendingCourses" :key="item.id">
            <view class="item-info">
              <image class="item-cover" :src="item.coverImage" mode="aspectFill" />
              <view class="item-detail">
                <text class="item-name">{{ item.title }}</text>
                <text class="item-teacher">{{ item.teacherName }}</text>
              </view>
            </view>
            <view class="item-actions">
              <view class="action-btn approve" @click="handleApproveCourse(item.id)">上架</view>
              <view class="action-btn reject" @click="handleRejectCourse(item.id)">驳回</view>
            </view>
          </view>
        </view>

        <view class="empty-card" v-else>
          <text class="empty-icon">✅</text>
          <text class="empty-text">暂无待上架课程</text>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">快捷操作</text>
        </view>

        <view class="quick-grid">
          <view class="quick-item" @click="goToUserManage">
            <text class="quick-icon">👥</text>
            <text class="quick-text">用户管理</text>
          </view>
          <view class="quick-item" @click="goToCourseManage">
            <text class="quick-icon">📚</text>
            <text class="quick-text">课程管理</text>
          </view>
          <view class="quick-item" @click="goToTeacherManage">
            <text class="quick-icon">👨‍🏫</text>
            <text class="quick-text">教师管理</text>
          </view>
          <view class="quick-item" @click="goToOrderManage">
            <text class="quick-icon">📋</text>
            <text class="quick-text">订单管理</text>
          </view>
          <view class="quick-item" @click="goToDataAnalysis">
            <text class="quick-icon">📊</text>
            <text class="quick-text">数据分析</text>
          </view>
          <view class="quick-item" @click="goToSystemSettings">
            <text class="quick-icon">⚙️</text>
            <text class="quick-text">系统设置</text>
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
import { getAdminStats, getUsers, getCourses, approveCourse as approveCourseApi, rejectCourse as rejectCourseApi, updateUserStatus, type AdminStats, type UserInfo, type CourseInfo } from '@/api/admin'

const userStore = useUserStore()

const stats = ref<AdminStats>({
  totalUsers: 0,
  totalCourses: 0,
  totalOrders: 0,
  totalRevenue: 0,
  todayOrders: 0,
  todayRevenue: 0
})

const pendingTeachers = ref<UserInfo[]>([])
const pendingCourses = ref<CourseInfo[]>([])

onMounted(() => {
  userStore.loadFromStorage()

  if (!userStore.isAdmin) {
    uni.navigateTo({ url: '/pages/user_login/index' })
    return
  }

  loadData()
})

async function loadData() {
  try {
    const adminStats = await getAdminStats()
    stats.value = adminStats

    const pendingTeacherResult = await getUsers(1, 10)
    pendingTeachers.value = pendingTeacherResult.list.filter(u => u.role === 'TEACHER' && u.status === 'PENDING')

    const pendingCourseResult = await getCourses(1, 10, 'PENDING')
    pendingCourses.value = pendingCourseResult.list
  } catch (err) {
    console.error('Load admin data failed:', err)
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

async function approveTeacher(id: number) {
  try {
    await updateUserStatus(id, 'ACTIVE')
    uni.showToast({ title: '通过教师审核', icon: 'success' })
    loadData()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function rejectTeacher(id: number) {
  try {
    await updateUserStatus(id, 'DISABLED')
    uni.showToast({ title: '拒绝教师审核', icon: 'none' })
    loadData()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleApproveCourse(id: number) {
  try {
    await approveCourseApi(id)
    uni.showToast({ title: '课程已上架', icon: 'success' })
    loadData()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleRejectCourse(id: number) {
  try {
    await rejectCourseApi(id, '不符合上架要求')
    uni.showToast({ title: '课程已驳回', icon: 'none' })
    loadData()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function goToUserManage() {
  uni.showToast({ title: '用户管理功能开发中', icon: 'none' })
}

function goToCourseManage() {
  uni.showToast({ title: '课程管理功能开发中', icon: 'none' })
}

function goToTeacherManage() {
  uni.showToast({ title: '教师管理功能开发中', icon: 'none' })
}

function goToOrderManage() {
  uni.showToast({ title: '订单管理功能开发中', icon: 'none' })
}

function goToDataAnalysis() {
  uni.showToast({ title: '数据分析功能开发中', icon: 'none' })
}

function goToSystemSettings() {
  uni.showToast({ title: '系统设置功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.admin-page {
  min-height: 100vh;
  background: $bg-color;
}

.header {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
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
  color: #11998e;
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

.section-count {
  font-size: 26rpx;
  color: #fff;
  background: #FF4757;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.list-card {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 2rpx solid $border-color;

  &:last-child {
    border-bottom: none;
  }
}

.item-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.item-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #e8f5e9;
  display: flex;
  align-items: center;
  justify-content: center;

  .avatar-text {
    font-size: 32rpx;
    color: #4caf50;
    font-weight: 600;
  }
}

.item-cover {
  width: 80rpx;
  height: 80rpx;
  border-radius: 8rpx;
}

.item-detail {
  .item-name {
    display: block;
    font-size: 30rpx;
    font-weight: 600;
    color: $text-primary;
  }

  .item-phone,
  .item-teacher {
    display: block;
    font-size: 24rpx;
    color: $text-muted;
    margin-top: 8rpx;
  }
}

.item-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  font-size: 26rpx;
  padding: 12rpx 24rpx;
  border-radius: 8rpx;

  &.approve {
    background: #e8f5e9;
    color: #4caf50;
  }

  &.reject {
    background: #ffebee;
    color: #f44336;
  }

  &:active {
    opacity: 0.8;
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
  grid-template-columns: repeat(3, 1fr);
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