<template>
  <view class="admin-page">
    <view class="hd">
      <text class="logo"><text class="logo-em">昂立</text>管理</text>
      <view class="hd-r">
        <view class="lg-out" @click="handleLogout">退出</view>
        <text class="avatar">管</text>
      </view>
    </view>

    <view class="tb">
      <view class="tb-i" @click="switchTab(0)">
        <view class="tb-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round">
            <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
        </view>
        <text class="tb-text">首页</text>
      </view>
      <view class="tb-i" @click="switchTab(1)">
        <view class="tb-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round">
            <path d="M9 12l2 2 4-4" />
            <path d="M21.5 12a9.5 9.5 0 1 1-19 0 9.5 9.5 0 0 1 19 0z" />
          </svg>
        </view>
        <text class="tb-text">审核</text>
      </view>
      <view class="tb-i on">
        <view class="tb-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
          </svg>
        </view>
        <text class="tb-text">内容</text>
      </view>
      <view class="tb-i" @click="switchTab(3)">
        <view class="tb-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round">
            <circle cx="12" cy="12" r="3" />
            <path
              d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
          </svg>
        </view>
        <text class="tb-text">我的</text>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="qa">
        <view class="qa-i" @click="goToPublishCourse">
          <view class="ic q1">📚</view>
          <text class="qa-text">发布课程</text>
        </view>
        <view class="qa-i" @click="goToPublishArticle">
          <view class="ic q2">📝</view>
          <text class="qa-text">发布文章</text>
        </view>
        <view class="qa-i" @click="goToPublishNotice">
          <view class="ic q3">📢</view>
          <text class="qa-text">发布公告</text>
        </view>
      </view>

      <view class="sh">
        <text class="sh-title">最近的课程</text>
        <text class="sh-more" @click="goToCourseManage">更多 →</text>
      </view>

      <view class="li" v-for="course in recentCourses" :key="course.id">
        <view class="lt" :class="getIconClass(course.id)">
          <text>{{ getCourseIcon(course.id) }}</text>
        </view>
        <view class="lb">
          <text class="lb-title">{{ course.title }}</text>
          <text class="lb-desc">{{ course.teacherName }} · ¥{{ course.price }} · {{ course.enrollmentCount }}人报名</text>
        </view>
        <view class="lr">
          <text class="st s2">已上架</text>
        </view>
      </view>

      <view class="empty">
        <text>— 没有更多内容了 —</text>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getCourses, type CourseInfo } from '@/api/admin'

const userStore = useUserStore()

const recentCourses = ref<CourseInfo[]>([])

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
    const courses = await getCourses('PUBLISHED')
    recentCourses.value = courses.slice(0, 5)
  } catch (err) {
    console.error('Load content data failed:', err)
  }
}

function getCourseIcon(id: number): string {
  const icons = ['📐', '📖', '⚡', '🧪', '📘']
  return icons[id % icons.length]
}

function getIconClass(id: number): string {
  const classes = ['t1', 't2', 't3', 't4', 't1']
  return classes[id % classes.length]
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

function switchTab(index: number) {
  switch (index) {
    case 0:
      uni.redirectTo({ url: '/pages/admin_home/index' })
      break
    case 1:
      uni.redirectTo({ url: '/pages/admin_review/index' })
      break
    case 3:
      uni.redirectTo({ url: '/pages/admin_profile/index' })
      break
  }
}

function goToPublishCourse() {
  uni.navigateTo({ url: '/pages/admin_publish-course/index' })
}

function goToPublishArticle() {
  uni.showToast({ title: '发布文章功能开发中', icon: 'none' })
}

function goToPublishNotice() {
  uni.navigateTo({ url: '/pages/admin_publish-notice/index' })
}

function goToCourseManage() {
  uni.navigateTo({ url: '/pages/admin_courses/index' })
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
  justify-content: space-between;
  padding: 10px 16px;
  background: #fff;
}

.logo {
  font-size: 19px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.3px;

  .logo-em {
    color: #F05A22;
  }
}

.hd-r {
  display: flex;
  align-items: center;
  gap: 12px;
}

.lg-out {
  padding: 5px 10px;
  background: none;
  color: #6e6e73;
  border: 1px solid #e8e8ed;
  border-radius: 6px;
  font-size: 11px;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #fde8e1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #F05A22;
  font-weight: 700;
  font-size: 11px;
}

.tb {
  display: flex;
  background: #fff;
  border-bottom: 1px solid #e8e8ed;
  gap: 0;
}

.tb-i {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 12px;
  color: #6e6e73;
  border-bottom: 2px solid transparent;
  transition: all 0.15s;

  &.on {
    color: #F05A22;
    border-bottom-color: #F05A22;
    font-weight: 600;
  }
}

.tb-icon {
  width: 20px;
  height: 20px;
  margin: 0 auto 2px;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 100%;
    height: 100%;
  }
}

.tb-text {
  font-size: 12px;
}

.content {
  height: calc(100vh - 130px);
}

.qa {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 14px;
  padding: 0 16px;
}

.qa-i {
  padding: 14px 6px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e8e8ed;
  text-align: center;
  transition: transform 0.1s;

  &:active {
    transform: scale(0.97);
  }
}

.ic {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 4px;
  font-size: 16px;

  &.q1 {
    background: #fde8e1;
    color: #F05A22;
  }

  &.q2 {
    background: #e3f2fd;
    color: #2196f3;
  }

  &.q3 {
    background: #fff3e0;
    color: #ff9800;
  }
}

.qa-text {
  font-size: 11px;
  color: #1d1d1f;
  font-weight: 500;
}

.sh {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 0 16px;
}

.sh-title {
  font-size: 15px;
  font-weight: 600;
}

.sh-more {
  font-size: 12px;
  color: #6e6e73;
}

.li {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e8e8ed;
  margin: 0 16px 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.lt {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;

  &.t1 {
    background: #fde8e1;
  }

  &.t2 {
    background: #e3f2fd;
  }

  &.t3 {
    background: #fff3e0;
  }

  &.t4 {
    background: #e8f5e9;
  }
}

.lb {
  flex: 1;
  min-width: 0;
}

.lb-title {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 1px;
  display: block;
}

.lb-desc {
  font-size: 11px;
  color: #6e6e73;
  display: block;
}

.lr {
  text-align: right;
}

.st {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;

  &.s2 {
    background: #e8f5e9;
    color: #4caf50;
  }
}

.empty {
  padding: 40px 16px;
  text-align: center;
  font-size: 13px;
  color: #6e6e73;
}

.bottom-space {
  height: 20px;
}
</style>