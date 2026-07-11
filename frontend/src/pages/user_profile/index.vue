<template>
  <view class="profile-page">
    <view class="header">
      <view class="status-bar"></view>
      <view class="profile-header">
        <view class="user-card">
          <view class="avatar">
            <text class="avatar-text">{{ userStore.userInfo?.realName?.charAt(0) || 'U' }}</text>
          </view>
          <view class="user-info">
            <text class="user-name">{{ userStore.userInfo?.realName || '家长' }}</text>
            <text class="user-phone">{{ userStore.userInfo?.phone || '' }}</text>
          </view>
          <view class="edit-btn" @click="goToSettings">
            <text class="edit-icon">⚙️</text>
          </view>
        </view>
        
        <view class="stats-row">
          <view class="stat-item" @click="goToOrders">
            <text class="stat-value">{{ stats.orders }}</text>
            <text class="stat-label">我的订单</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @click="goToCertificates">
            <text class="stat-value">{{ stats.certificates }}</text>
            <text class="stat-label">荣誉证书</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @click="goToFavorites">
            <text class="stat-value">{{ stats.favorites }}</text>
            <text class="stat-label">我的收藏</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @click="goToCoupons">
            <text class="stat-value">{{ stats.coupons }}</text>
            <text class="stat-label">优惠券</text>
          </view>
        </view>
      </view>
    </view>
    
    <scroll-view class="content" scroll-y>
      <view class="menu-section">
        <view class="menu-card">
          <view class="menu-item" @click="goToBindChild">
            <text class="menu-icon">👶</text>
            <text class="menu-text">绑定孩子</text>
            <text class="menu-arrow">→</text>
          </view>
          <view class="menu-item" @click="goToLearningReport">
            <text class="menu-icon">📊</text>
            <text class="menu-text">学习报告</text>
            <text class="menu-arrow">→</text>
          </view>
          <view class="menu-item" @click="goToHomework">
            <text class="menu-icon">📝</text>
            <text class="menu-text">作业管理</text>
            <text class="menu-arrow">→</text>
          </view>
          <view class="menu-item" @click="goToAttendance">
            <text class="menu-icon">📅</text>
            <text class="menu-text">出勤记录</text>
            <text class="menu-arrow">→</text>
          </view>
        </view>
        
        <view class="menu-card">
          <view class="menu-item" @click="goToHelpCenter">
            <text class="menu-icon">💬</text>
            <text class="menu-text">帮助中心</text>
            <text class="menu-arrow">→</text>
          </view>
          <view class="menu-item" @click="goToFeedback">
            <text class="menu-icon">📮</text>
            <text class="menu-text">意见反馈</text>
            <text class="menu-arrow">→</text>
          </view>
          <view class="menu-item" @click="goToAbout">
            <text class="menu-icon">ℹ️</text>
            <text class="menu-text">关于我们</text>
            <text class="menu-arrow">→</text>
          </view>
        </view>
      </view>
      
      <view class="logout-section">
        <view class="logout-btn" @click="handleLogout">
          退出登录
        </view>
      </view>
      
      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const stats = ref({
  orders: 5,
  certificates: 2,
  favorites: 8,
  coupons: 3
})

onMounted(() => {
  userStore.loadFromStorage()
  
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/user_login/index' })
  }
})

function goToSettings() {
  uni.navigateTo({ url: '/pages/user_settings/index' })
}

function goToOrders() {
  uni.showToast({ title: '订单功能开发中', icon: 'none' })
}

function goToCertificates() {
  uni.showToast({ title: '证书功能开发中', icon: 'none' })
}

function goToFavorites() {
  uni.showToast({ title: '收藏功能开发中', icon: 'none' })
}

function goToCoupons() {
  uni.showToast({ title: '优惠券功能开发中', icon: 'none' })
}

function goToBindChild() {
  uni.navigateTo({ url: '/pages/user_bind-child/index' })
}

function goToLearningReport() {
  uni.showToast({ title: '学习报告功能开发中', icon: 'none' })
}

function goToHomework() {
  uni.showToast({ title: '作业管理功能开发中', icon: 'none' })
}

function goToAttendance() {
  uni.showToast({ title: '出勤记录功能开发中', icon: 'none' })
}

function goToHelpCenter() {
  uni.showToast({ title: '帮助中心开发中', icon: 'none' })
}

function goToFeedback() {
  uni.showToast({ title: '意见反馈开发中', icon: 'none' })
}

function goToAbout() {
  uni.showToast({ title: '关于我们开发中', icon: 'none' })
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
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: $bg-color;
}

.header {
  background: linear-gradient(135deg, $primary-color, $primary-dark);
}

.status-bar {
  height: var(--status-bar-height, 44px);
}

.profile-header {
  padding: 40rpx 32rpx 60rpx;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
}

.avatar-text {
  font-size: 48rpx;
  color: #fff;
  font-weight: 600;
}

.user-info {
  flex: 1;
  
  .user-name {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
    margin-bottom: 8rpx;
  }
  
  .user-phone {
    display: block;
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.8);
  }
}

.edit-btn {
  background: rgba(255, 255, 255, 0.2);
  padding: 16rpx;
  border-radius: 50%;
}

.edit-icon {
  font-size: 32rpx;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16rpx;
  padding: 32rpx 0;
  margin-top: 32rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #fff;
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 8rpx;
  display: block;
}

.stat-divider {
  width: 2rpx;
  background: rgba(255, 255, 255, 0.3);
}

.content {
  height: calc(100vh - var(--status-bar-height, 44px) - 320rpx - 120rpx);
  margin-top: -40rpx;
  padding: 0 32rpx;
}

.menu-section {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.menu-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid $border-color;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:active {
    background: #f8f8f8;
  }
}

.menu-icon {
  font-size: 44rpx;
  margin-right: 24rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: $text-primary;
}

.menu-arrow {
  font-size: 28rpx;
  color: $text-muted;
}

.logout-section {
  margin-top: 48rpx;
}

.logout-btn {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  text-align: center;
  font-size: 30rpx;
  color: #FF4757;
  
  &:active {
    background: #fef0f0;
  }
}

.bottom-space {
  height: 160rpx;
}
</style>