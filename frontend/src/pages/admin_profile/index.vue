<template>
  <view class="admin-page">
    <view class="hd">
      <view class="hd-back" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
          stroke-linejoin="round">
          <path d="m15 18-6-6 6-6" />
        </svg>
      </view>
      <text class="hd-title">个人中心</text>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="pf">
        <view class="pf-av">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
        </view>
        <view class="pf-info">
          <text class="pf-n">{{ userInfo.username }}</text>
          <text class="pf-r">系统管理员</text>
        </view>
        <view class="pf-edit" @click="goToEditProfile">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round">
            <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z" />
          </svg>
        </view>
      </view>

      <view class="ml">
        <view class="mi">
          <view class="mi-icon i1">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M12 20h9" />
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
            </svg>
          </view>
          <text class="mi-text">发布的课程</text>
          <text class="mi-v">12</text>
          <text class="mi-arrow">›</text>
        </view>
        <view class="mi" @click="goToUserManage">
          <view class="mi-icon i2">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M16 18v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="8.5" cy="7" r="4" />
              <path d="M20 21v-2a4 4 0 0 0-3-3.87" />
              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
            </svg>
          </view>
          <text class="mi-text">管理的用户</text>
          <text class="mi-v">{{ stats.totalUsers || '0' }}</text>
          <text class="mi-arrow">›</text>
        </view>
        <view class="mi">
          <view class="mi-icon i3">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path
                d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.121 2.121 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" />
            </svg>
          </view>
          <text class="mi-text">修改密码</text>
          <text class="mi-arrow">›</text>
        </view>
        <view class="mi">
          <view class="mi-icon i4">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M12 20h9" />
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
            </svg>
          </view>
          <text class="mi-text">系统设置</text>
          <text class="mi-arrow">›</text>
        </view>
        <view class="mi">
          <view class="mi-icon i5">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <circle cx="12" cy="12" r="10" />
              <path d="M12 16v-4" />
              <path d="M12 8h.01" />
            </svg>
          </view>
          <text class="mi-text">关于我们</text>
          <text class="mi-arrow">›</text>
        </view>
      </view>

      <view class="btn-l" @click="doLogout">退出登录</view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAdminStats } from '@/api/admin'

const userStore = useUserStore()

const userInfo = reactive({
  username: '',
  role: ''
})

const stats = reactive({
  totalUsers: 0
})

onMounted(() => {
  userInfo.username = userStore.user?.username || '管理员'
  loadStats()
})

async function loadStats() {
  try {
    const data = await getAdminStats()
    stats.totalUsers = data.totalUsers || 0
  } catch {
    stats.totalUsers = 0
  }
}

function goToEditProfile() {
  uni.showToast({ title: '编辑个人信息', icon: 'none' })
}

function goToUserManage() {
  uni.navigateTo({ url: '/pages/admin_users/index' })
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.navigateTo({ url: '/pages/admin_home/index' })
  }
}

function doLogout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.reLaunch({ url: '/pages/user_login/index' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.admin-page {
  min-height: 100vh;
  background: #F5F5F7;
}

.hd {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: #fff;
}

.hd-back {
  display: flex;
  align-items: center;
  padding: 4px 0;
  color: #1d1d1f;
  font-size: 15px;
  margin-right: 8px;

  svg {
    width: 22px;
    height: 22px;
  }
}

.hd-title {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.3px;
  margin-right: 30px;
}

.content {
  height: calc(100vh - 60px);
}

.pf {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 24px 16px;
  background: linear-gradient(135deg, #F05A22, #ff7a45);
  margin: 12px;
  border-radius: 16px;
}

.pf-av {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg {
    width: 32px;
    height: 32px;
    color: #fff;
  }
}

.pf-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pf-n {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.pf-r {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.pf-edit {
  padding: 8px;
  color: #fff;

  svg {
    width: 20px;
    height: 20px;
  }
}

.ml {
  margin: 0 12px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}

.mi {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #f5f5f7;

  &:last-child {
    border-bottom: none;
  }
}

.mi-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg {
    width: 20px;
    height: 20px;
    color: #fff;
  }

  &.i1 {
    background: linear-gradient(135deg, #667eea, #764ba2);
  }

  &.i2 {
    background: linear-gradient(135deg, #f093fb, #f5576c);
  }

  &.i3 {
    background: linear-gradient(135deg, #4facfe, #00f2fe);
  }

  &.i4 {
    background: linear-gradient(135deg, #43e97b, #38f9d7);
  }

  &.i5 {
    background: linear-gradient(135deg, #fa709a, #fee140);
  }
}

.mi-text {
  flex: 1;
  font-size: 15px;
  color: #1d1d1f;
}

.mi-v {
  font-size: 13px;
  color: #6e6e73;
  margin-right: 4px;
}

.mi-arrow {
  font-size: 18px;
  color: #d1d1d6;
}

.btn-l {
  margin: 20px 12px;
  padding: 14px;
  background: #fff;
  border-radius: 12px;
  font-size: 16px;
  text-align: center;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.bottom-space {
  height: 40px;
}
</style>