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
      <view class="tb-i on">
        <view class="tb-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round">
            <path d="M9 12l2 2 4-4" />
            <path d="M21.5 12a9.5 9.5 0 1 1-19 0 9.5 9.5 0 0 1 19 0z" />
          </svg>
        </view>
        <text class="tb-text">审核</text>
        <text class="badge">{{ pendingCount }}</text>
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

    <view class="st">
      <view class="st-i" :class="{ on: activeTab === 'course' }" @click="activeTab = 'course'">课程审核</view>
      <view class="st-i" :class="{ on: activeTab === 'teacher' }" @click="activeTab = 'teacher'">教师审核</view>
    </view>

    <scroll-view class="content" scroll-y>
      <view v-if="activeTab === 'course'" class="review-list">
        <view class="sh">
          <text class="sh-title">课程审核 <text class="sh-count">{{ pendingCourses.length }}条待审核</text></text>
        </view>

        <view class="rv-c" v-for="course in pendingCourses" :key="course.id">
          <view class="rv-h">
            <view class="ri">{{ getCourseIcon(course) }}</view>
            <view class="rb">
              <text class="rb-title">{{ course.title }}</text>
              <text class="rb-desc">{{ course.subject || '' }} · {{ course.grade || '' }} · {{ course.totalHours || 0
                }}课时 · {{
                  formatDate(course.appliedAt) }}</text>
            </view>
          </view>
          <view class="rv-b">
            <view class="t" @click="viewCourseDetail(course.id)">查看详情</view>
            <view class="t g" @click="approveCourse(course.id)">✓ 通过</view>
            <view class="t r" @click="rejectCourse(course.id)">✕ 驳回</view>
          </view>
        </view>

        <view class="empty" v-if="pendingCourses.length === 0">
          <text>暂无待审核课程</text>
        </view>
      </view>

      <view v-if="activeTab === 'teacher'" class="review-list">
        <view class="sh">
          <text class="sh-title">教师审核 <text class="sh-count">{{ pendingTeachers.length }}条待审核</text></text>
        </view>

        <view class="rv-c" v-for="teacher in pendingTeachers" :key="teacher.id">
          <view class="rv-h">
            <view class="ri">{{ teacher.realName?.charAt(0) || 'T' }}</view>
            <view class="rb">
              <text class="rb-title">{{ teacher.realName }}</text>
              <text class="rb-desc">{{ teacher.phone }} · 提交于 {{ formatDate(teacher.createdAt) }}</text>
            </view>
          </view>
          <view class="rv-b">
            <view class="t" @click="viewTeacherDetail(teacher.id)">查看详情</view>
            <view class="t g" @click="approveTeacher(teacher.id)">✓ 通过</view>
            <view class="t r" @click="rejectTeacher(teacher.id)">✕ 驳回</view>
          </view>
        </view>

        <view class="empty" v-if="pendingTeachers.length === 0">
          <text>暂无待审核教师</text>
        </view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>

    <view class="approve-modal" v-if="showApproveModal" @click="cancelApprove">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">确认通过</text>
        </view>
        <view class="modal-body">
          <text class="modal-desc">确定要通过该课程申请吗？通过后课程将自动发布。</text>
          <view class="recommend-option">
            <view class="checkbox" :class="{ checked: approveRecommend }" @click="approveRecommend = !approveRecommend">
              <text class="checkbox-icon" v-if="approveRecommend">✓</text>
            </view>
            <text class="recommend-label">是否推荐</text>
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn cancel" @click="cancelApprove">取消</view>
          <view class="modal-btn confirm" @click="confirmApprove">确定</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUsers, getCourseApplications, approveCourseApplication, rejectCourseApplication, approveTeacher as approveTeacherApi, rejectTeacher as rejectTeacherApi, type UserInfo, type CourseApplication } from '@/api/admin'

const userStore = useUserStore()

const activeTab = ref('course')
const pendingTeachers = ref<UserInfo[]>([])
const pendingCourses = ref<CourseApplication[]>([])

const pendingCount = computed(() => pendingTeachers.value.length + pendingCourses.value.length)

const showApproveModal = ref(false)
const approveCourseId = ref(0)
const approveRecommend = ref(false)

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
    const users = await getUsers()
    pendingTeachers.value = users.filter(u => u.role === 'TEACHER' && u.status === 'PENDING')

    const applications = await getCourseApplications('PENDING')
    pendingCourses.value = applications
  } catch (err) {
    console.error('Load review data failed:', err)
  }
}

function getCourseIcon(course: CourseApplication): string {
  const icons = ['📐', '📖', '⚡', '🧪', '📘', '🎨']
  return icons[course.id % icons.length]
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}-${date.getDate()}`
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
    case 2:
      uni.redirectTo({ url: '/pages/admin_content/index' })
      break
    case 3:
      uni.redirectTo({ url: '/pages/admin_profile/index' })
      break
  }
}

async function approveCourse(id: number) {
  approveCourseId.value = id
  approveRecommend.value = false
  showApproveModal.value = true
}

async function confirmApprove() {
  try {
    await approveCourseApplication(approveCourseId.value, approveRecommend.value)
    uni.showToast({ title: '审核通过', icon: 'success' })
    showApproveModal.value = false
    loadData()
  } catch (err: any) {
    const msg = err?.response?.data?.message || '操作失败'
    uni.showToast({ title: msg, icon: 'none' })
  }
}

function cancelApprove() {
  showApproveModal.value = false
}

async function rejectCourse(id: number) {
  uni.showModal({
    title: '确认驳回',
    content: '确定要驳回该课程申请吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await rejectCourseApplication(id, '不符合上架要求')
          uni.showToast({ title: '已驳回', icon: 'none' })
          loadData()
        } catch (err: any) {
          const msg = err?.response?.data?.message || '操作失败'
          uni.showToast({ title: msg, icon: 'none' })
        }
      }
    }
  })
}

async function approveTeacher(id: number) {
  uni.showModal({
    title: '确认通过',
    content: '确定要通过该教师审核吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await approveTeacherApi(id)
          uni.showToast({ title: '审核通过', icon: 'success' })
          loadData()
        } catch {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

async function rejectTeacher(id: number) {
  uni.showModal({
    title: '确认驳回',
    content: '确定要驳回该教师申请吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await rejectTeacherApi(id)
          uni.showToast({ title: '已驳回', icon: 'none' })
          loadData()
        } catch {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

function viewCourseDetail(id: number) {
  uni.showToast({ title: '课程详情功能开发中', icon: 'none' })
}

function viewTeacherDetail(id: number) {
  uni.showToast({ title: '教师详情功能开发中', icon: 'none' })
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

.st {
  display: flex;
  background: #fff;
  margin: 10px 16px;
  border-radius: 10px;
  border: 1px solid #e8e8ed;
  overflow: hidden;
}

.st-i {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 14px;
  font-weight: 500;
  color: #6e6e73;
  cursor: pointer;
  transition: all 0.15s;

  &.on {
    background: #F05A22;
    color: #fff;
    font-weight: 600;
  }
}

.content {
  height: calc(100vh - 180px);
}

.sh {
  padding: 10px 16px;
}

.sh-title {
  font-size: 15px;
  font-weight: 600;
}

.sh-count {
  font-size: 12px;
  color: #6e6e73;
  font-weight: 400;
  margin-left: 4px;
}

.rv-c {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8ed;
  margin: 0 16px 10px;
  overflow: hidden;
}

.rv-h {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px;
}

.ri {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fde8e1;
  font-size: 16px;
  flex-shrink: 0;
  color: #F05A22;
}

.rb {
  flex: 1;
}

.rb-title {
  font-size: 13px;
  font-weight: 600;
  display: block;
}

.rb-desc {
  font-size: 11px;
  color: #6e6e73;
  margin-top: 1px;
  display: block;
}

.rv-b {
  padding: 0 14px 14px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.t {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 11px;
  background: #fde8e1;
  color: #F05A22;
  font-weight: 500;

  &.g {
    background: #e8f5e9;
    color: #4caf50;
  }

  &.r {
    background: #fce4ec;
    color: #e91e63;
  }

  &:active {
    opacity: 0.8;
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

.approve-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 80%;
  max-width: 320px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}

.modal-header {
  padding: 20px 20px 0;
  text-align: center;
}

.modal-title {
  font-size: 17px;
  font-weight: 600;
}

.modal-body {
  padding: 20px;
}

.modal-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  display: block;
  margin-bottom: 20px;
}

.recommend-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

.checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid #ddd;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &.checked {
    background: #F05A22;
    border-color: #F05A22;
  }
}

.checkbox-icon {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}

.recommend-label {
  font-size: 14px;
  color: #333;
}

.modal-footer {
  display: flex;
  border-top: 1px solid #eee;
}

.modal-btn {
  flex: 1;
  padding: 16px;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  transition: background 0.2s;

  &.cancel {
    color: #666;
    border-right: 1px solid #eee;

    &:active {
      background: #f5f5f5;
    }
  }

  &.confirm {
    color: #F05A22;

    &:active {
      background: #fff5f0;
    }
  }
}
</style>