<template>
  <view class="settings-page">
    <view class="header">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <view class="back-btn" @click="goBack">
          <text class="back-icon">←</text>
        </view>
        <text class="nav-title">设置</text>
        <view class="placeholder"></view>
      </view>
    </view>
    
    <scroll-view class="content" scroll-y>
      <view class="menu-card">
        <view class="menu-item" @click="goToEditProfile">
          <text class="menu-icon">👤</text>
          <text class="menu-text">编辑资料</text>
          <text class="menu-arrow">→</text>
        </view>
        <view class="menu-item" @click="goToChangePassword">
          <text class="menu-icon">🔒</text>
          <text class="menu-text">修改密码</text>
          <text class="menu-arrow">→</text>
        </view>
        <view class="menu-item" @click="goToNotification">
          <text class="menu-icon">🔔</text>
          <text class="menu-text">消息通知</text>
          <text class="menu-arrow">→</text>
        </view>
      </view>
      
      <view class="menu-card">
        <view class="menu-item" @click="goToPrivacy">
          <text class="menu-icon">🛡️</text>
          <text class="menu-text">隐私设置</text>
          <text class="menu-arrow">→</text>
        </view>
        <view class="menu-item" @click="goToTerms">
          <text class="menu-icon">📄</text>
          <text class="menu-text">用户协议</text>
          <text class="menu-arrow">→</text>
        </view>
        <view class="menu-item" @click="goToAbout">
          <text class="menu-icon">ℹ️</text>
          <text class="menu-text">关于我们</text>
          <text class="menu-arrow">→</text>
        </view>
      </view>
      
      <view class="logout-section">
        <view class="logout-btn" @click="doLogout">退出登录</view>
      </view>
      
      <view class="version-info">
        <text class="version-text">版本号：v1.0.0</text>
      </view>
      
      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

function goBack() {
  uni.navigateBack()
}

function goToEditProfile() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function goToChangePassword() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function goToNotification() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function goToPrivacy() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function goToTerms() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function goToAbout() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function doLogout() {
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
.settings-page {
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
  padding: 20rpx 32rpx;
}

.back-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 44rpx;
  color: $text-primary;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $text-primary;
}

.placeholder {
  width: 80rpx;
}

.content {
  height: calc(100vh - var(--status-bar-height, 44px) - 80rpx);
  padding: 32rpx;
}

.menu-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
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
  margin-bottom: 24rpx;
}

.logout-btn {
  width: 100%;
  padding: 28rpx;
  background: #fff;
  color: #dc2626;
  border: 2rpx solid #dc2626;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
  text-align: center;
}

.version-info {
  text-align: center;
  padding: 48rpx;
}

.version-text {
  font-size: 26rpx;
  color: $text-muted;
}

.bottom-space {
  height: 60rpx;
}
</style>