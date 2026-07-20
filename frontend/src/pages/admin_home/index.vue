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
      <view class="tb-i on" @click="switchTab(0)">
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
        <text class="badge" v-if="pendingCount > 0">{{ pendingCount }}</text>
      </view>
      <view class="tb-i" @click="switchTab(2)">
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
      <view class="sg">
        <view class="sc">
          <view class="sc-icon i1">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
            </svg>
          </view>
          <text class="l">总注册用户</text>
          <text class="v">{{ stats.totalUsers || '0' }}</text>
          <text class="c">↑ 226 本月</text>
        </view>
        <view class="sc">
          <view class="sc-icon i2">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M12 2v20" />
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
            </svg>
          </view>
          <text class="l">本月营收</text>
          <text class="v">¥{{ formatNumber(stats.monthlyRevenue || 0) }}</text>
          <text class="c">↑ 15.6%</text>
        </view>
        <view class="sc">
          <view class="sc-icon i3">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
            </svg>
          </view>
          <text class="l">平均评分</text>
          <text class="v">{{ stats.averageRating || '0.0' }}</text>
          <text class="c">↑ 0.1</text>
        </view>
        <view class="sc">
          <view class="sc-icon i4">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
            </svg>
          </view>
          <text class="l">在职教师</text>
          <text class="v">{{ stats.activeTeachers || '0' }}</text>
          <text class="c">↑ 3人</text>
        </view>
        <view class="sc">
          <view class="sc-icon i5">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </view>
          <text class="l">服务学生</text>
          <text class="v">{{ stats.totalStudents || '0' }}</text>
          <text class="c">↑ 189人</text>
        </view>
      </view>

      <view class="sh">
        <text class="sh-title">快捷操作</text>
      </view>
      <view class="qa">
        <view class="qa-i" @click="goToReview">
          <view class="ic q1">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M9 12l2 2 4-4" />
            </svg>
          </view>
          <text class="qa-text">待审核</text>
        </view>
        <view class="qa-i" @click="goToPublishCourse">
          <view class="ic q2">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
            </svg>
          </view>
          <text class="qa-text">发布课程</text>
        </view>
        <view class="qa-i" @click="goToCourseManage">
          <view class="ic q3">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
            </svg>
          </view>
          <text class="qa-text">课程管理</text>
        </view>
        <view class="qa-i" @click="goToUserManage">
          <view class="ic q4">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </view>
          <text class="qa-text">用户管理</text>
        </view>
      </view>

      <view class="sh">
        <text class="sh-title">待处理事项</text>
      </view>
      <view class="li" v-for="item in pendingItems" :key="item.id">
        <view class="lt" :class="item.type">
          <text>{{ item.icon }}</text>
        </view>
        <view class="lb">
          <text class="lb-title">{{ item.title }}</text>
          <text class="lb-desc">{{ item.desc }}</text>
        </view>
        <view class="lr">
          <text class="st" :class="item.statusClass">{{ item.status }}</text>
        </view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDashboard, getUsers, getCourses, type UserInfo, type CourseInfo, type DashboardStats } from '@/api/admin'

const userStore = useUserStore()

const stats = ref<DashboardStats>({
  totalUsers: 0,
  activeTeachers: 0,
  pendingTeachers: 0,
  totalStudents: 0,
  averageRating: 0,
  monthlyRevenue: 0
})

const pendingTeachers = ref<UserInfo[]>([])
const pendingCourses = ref<CourseInfo[]>([])

const pendingCount = computed(() => pendingTeachers.value.length + pendingCourses.value.length)

const pendingItems = computed(() => {
  const items: any[] = []

  pendingCourses.value.slice(0, 2).forEach((course, index) => {
    items.push({
      id: 'course-' + course.id,
      icon: '📘',
      type: 't1',
      title: '课程审核',
      desc: `${course.teacherName}老师提交了"${course.title}"`,
      status: '待审核',
      statusClass: 's1'
    })
  })

  pendingTeachers.value.slice(0, 2).forEach((teacher, index) => {
    items.push({
      id: 'teacher-' + teacher.id,
      icon: '👤',
      type: 't2',
      title: '教师入驻',
      desc: `${teacher.realName}老师完成了实名认证，等待审核`,
      status: '待审核',
      statusClass: 's1'
    })
  })

  return items
})

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
    const dashboardData = await getDashboard()
    stats.value = {
      totalUsers: dashboardData.totalUsers || 0,
      activeTeachers: dashboardData.activeTeachers || 0,
      pendingTeachers: dashboardData.pendingTeachers || 0,
      totalStudents: dashboardData.totalStudents || 0,
      averageRating: dashboardData.averageRating || 0,
      monthlyRevenue: dashboardData.monthlyRevenue || 0
    }

    const users = await getUsers()
    pendingTeachers.value = users.filter(u => u.role === 'TEACHER' && u.status === 'PENDING')

    const courses = await getCourses('PENDING')
    pendingCourses.value = courses
  } catch (err) {
    console.error('Load admin data failed:', err)
  }
}

function formatNumber(num: number): string {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
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
      break
    case 1:
      uni.navigateTo({ url: '/pages/admin_review/index' })
      break
    case 2:
      uni.navigateTo({ url: '/pages/admin_content/index' })
      break
    case 3:
      uni.navigateTo({ url: '/pages/admin_profile/index' })
      break
  }
}

function goToReview() {
  uni.navigateTo({ url: '/pages/admin_review/index' })
}

function goToPublishCourse() {
  uni.navigateTo({ url: '/pages/admin_publish-course/index' })
}

function goToCourseManage() {
  uni.navigateTo({ url: '/pages/admin_courses/index' })
}

function goToUserManage() {
  uni.navigateTo({ url: '/pages/admin_users/index' })
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
  position: relative;

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

  .on & {
    color: #F05A22;
  }
}

.tb-text {
  font-size: 12px;
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #dc2626;
  color: #fff;
  border-radius: 9px;
  font-size: 10px;
  font-weight: 600;
  position: absolute;
  top: 6px;
  right: 25%;
}

.content {
  height: calc(100vh - 130px);
}

.sg {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 14px;
  padding: 0 16px;
}

.sc {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8ed;
  padding: 16px;
}

.sc-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;

  svg {
    width: 18px;
    height: 18px;
  }

  &.i1 {
    background: #fde8e1;
    color: #F05A22;
  }

  &.i2 {
    background: #e3f2fd;
    color: #2196f3;
  }

  &.i3 {
    background: #fff3e0;
    color: #ff9800;
  }

  &.i4 {
    background: #e8f5e9;
    color: #4caf50;
  }

  &.i5 {
    background: #f3e5f5;
    color: #9c27b0;
  }
}

.l {
  font-size: 11px;
  color: #6e6e73;
  margin-bottom: 2px;
  display: block;
}

.v {
  font-size: 24px;
  font-weight: 700;
  color: #1d1d1f;
  display: block;
}

.c {
  font-size: 11px;
  color: #34c759;
  margin-top: 2px;
  display: block;

  &.down {
    color: #ff4d4d;
  }
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

.qa {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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

  svg {
    width: 16px;
    height: 16px;
  }

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

  &.q4 {
    background: #e8f5e9;
    color: #4caf50;
  }
}

.qa-text {
  font-size: 11px;
  color: #1d1d1f;
  font-weight: 500;
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
    background: #fce4ec;
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

  &.s1 {
    background: #fde8e1;
    color: #F05A22;
  }

  &.s2 {
    background: #e8f5e9;
    color: #4caf50;
  }

  &.s3 {
    background: #fce4ec;
    color: #e91e63;
  }

  &.s4 {
    background: #F5F5F7;
    color: #6e6e73;
  }
}

.bottom-space {
  height: 20px;
}
</style>