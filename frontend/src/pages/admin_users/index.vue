<template>
  <view class="admin-page">
    <view class="hd">
      <view class="hd-back" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
          stroke-linejoin="round">
          <path d="m15 18-6-6 6-6" />
        </svg>
      </view>
      <text class="hd-title">用户管理</text>
    </view>

    <view class="srch">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
        stroke-linejoin="round">
        <circle cx="11" cy="11" r="8" />
        <path d="m21 21-4.35-4.35" />
      </svg>
      <input class="srch-inp" v-model="searchQuery" placeholder="搜索用户姓名、手机号…" />
    </view>

    <view class="cnt">
      <view class="cnt-i">
        <text class="cnt-n">{{ countNormal }}</text>
        <text class="cnt-l">普通用户</text>
      </view>
      <view class="cnt-i">
        <text class="cnt-n">{{ countTeacher }}</text>
        <text class="cnt-l">在职老师</text>
      </view>
      <view class="cnt-i">
        <text class="cnt-n">{{ countPending }}</text>
        <text class="cnt-l">待审核</text>
      </view>
    </view>

    <view class="tb">
      <view class="tb-i" :class="{ on: activeTab === 0 }" @click="switchTab(0)">普通用户</view>
      <view class="tb-i" :class="{ on: activeTab === 1 }" @click="switchTab(1)">在职老师</view>
      <view class="tb-i" :class="{ on: activeTab === 2 }" @click="switchTab(2)">审核申请 <text v-if="countPending > 0"
          class="tb-badge">{{ countPending }}</text></view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="ul">
        <view class="ui" v-for="user in filteredUsers" :key="user.id">
          <view class="ui-av" :class="getAvatarClass(user)">
            {{ getInitial(user) }}
          </view>
          <view class="ui-bd">
            <view class="ui-un">
              <text class="ui-n">{{ user.realName || user.username || '用户' }}</text>
              <text v-if="user.role === 'STUDENT'" class="ui-tag t1">家长</text>
              <text v-if="user.role === 'TEACHER'" class="ui-tag t6">老师</text>
              <text v-if="user.role === 'TEACHER' && user.subject" class="ui-tag t2">{{ user.subject }}</text>
              <text v-if="user.role === 'TEACHER' && user.title" class="ui-tag t3">{{ user.title }}</text>
              <text v-if="user.status === 'PENDING'" class="ui-tag t5">待审</text>
            </view>
            <text class="ui-um">{{ getUserDesc(user) }}</text>
            <view v-if="user.status === 'PENDING'" class="rv-act">
              <view class="rv-btn vw" @click.stop="viewDetail(user)">查看</view>
              <view class="rv-btn ok" @click.stop="approveTeacher(user)">✓ 通过</view>
              <view class="rv-btn no" @click.stop="rejectTeacher(user)">✕ 驳回</view>
            </view>
            <view v-if="user.role === 'PARENT' && user.status !== 'PENDING'" class="rv-act">
              <view class="rv-btn vw" @click.stop="viewDetail(user)">查看</view>
            </view>
          </view>
          <view class="ui-ur">
            <text class="ui-stat" :class="getStatusClass(user)">{{ getStatusText(user) }}</text>
          </view>
        </view>
      </view>

      <view class="empty" v-if="filteredUsers.length === 0">
        暂无数据
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>

  <view class="detail-modal" :class="{ show: showDetailModal }" @click="closeDetailModal">
    <view class="dm-content" @click.stop>
      <view class="dm-header">
        <text class="dm-title">用户详情</text>
        <view class="dm-close" @click="closeDetailModal">✕</view>
      </view>

      <scroll-view class="dm-body" scroll-y>
        <view class="dm-avatar">
          <view class="dm-av" :class="getAvatarClass(currentUser)">
            {{ getInitial(currentUser) }}
          </view>
          <view class="dm-info">
            <text class="dm-name">{{ currentUser.realName }}</text>
            <text class="dm-role">{{ currentUser.role === 'PARENT' ? '家长' : '教师' }}</text>
          </view>
        </view>

        <view class="dm-section">
          <view class="dm-item">
            <text class="dm-label">手机号</text>
            <text class="dm-value">{{ currentUser.phone }}</text>
          </view>
          <view class="dm-item">
            <text class="dm-label">状态</text>
            <text class="dm-value" :class="getStatusClass(currentUser)">{{ getStatusText(currentUser) }}</text>
          </view>
          <view class="dm-item">
            <text class="dm-label">注册时间</text>
            <text class="dm-value">{{ formatDate(currentUser.createdAt) }}</text>
          </view>
          <view class="dm-item">
            <text class="dm-label">标签</text>
            <view class="dm-tags">
              <text v-for="tag in currentUser.tags?.split(',')?.filter(t => t.trim())" :key="tag" class="dm-tag">{{ tag
                }}</text>
              <text v-if="!currentUser.tags || currentUser.tags.split(',').filter(t => t.trim()).length === 0"
                class="dm-tag-empty">暂无标签</text>
            </view>
          </view>
        </view>

        <view class="dm-section" v-if="currentUser.role === 'PARENT'">
          <view class="dm-section-title">
            <text class="dm-st-icon">👶</text>
            <text class="dm-st-text">名下孩子（{{ children.length }}）</text>
          </view>
          <view class="dm-children" v-if="children.length > 0">
            <view class="dm-child" v-for="child in children" :key="child.id">
              <view class="dc-name">{{ child.name }}</view>
              <view class="dc-info">
                <text v-if="child.grade">{{ child.grade }}</text>
                <text v-if="child.age"> · {{ child.age }}岁</text>
                <text v-if="child.gender"> · {{ child.gender }}</text>
              </view>
            </view>
          </view>
          <view class="dm-empty" v-else>
            暂无绑定的孩子
          </view>
        </view>

        <view class="dm-section" v-if="currentUser.role === 'TEACHER'">
          <view class="dm-section-title">
            <text class="dm-st-icon">📚</text>
            <text class="dm-st-text">教师信息</text>
          </view>
          <view class="dm-empty">
            暂无详细信息
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { getUsers, approveTeacher as apiApprove, rejectTeacher as apiReject, type UserInfo } from '@/api/admin'
import { getStudentsByParentId, type Student } from '@/api/student'

const activeTab = ref(0)
const searchQuery = ref('')
const users = ref<UserInfo[]>([])

const showDetailModal = ref(false)
const currentUser = reactive<UserInfo>({
  id: 0,
  phone: '',
  realName: '',
  role: '',
  status: '',
  createdAt: '',
  tags: ''
})
const children = ref<Student[]>([])

const colors = ['c1', 'c2', 'c3', 'c4', 'c5', 'c6']

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  try {
    const data = await getUsers()
    users.value = data
  } catch {
    users.value = []
  }
}

const countNormal = computed(() => users.value.filter(u => u.role === 'PARENT').length)
const countTeacher = computed(() => users.value.filter(u => u.role === 'TEACHER' && u.status !== 'PENDING').length)
const countPending = computed(() => users.value.filter(u => u.role === 'TEACHER' && u.status === 'PENDING').length)

function switchTab(idx: number) {
  activeTab.value = idx
}

const filteredUsers = computed(() => {
  let result = users.value

  if (activeTab.value === 0) {
    result = result.filter(u => u.role === 'PARENT')
  } else if (activeTab.value === 1) {
    result = result.filter(u => u.role === 'TEACHER' && u.status !== 'PENDING')
  } else if (activeTab.value === 2) {
    result = result.filter(u => u.role === 'TEACHER' && u.status === 'PENDING')
  }

  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(u =>
      (u.username?.toLowerCase().includes(query) || u.realName?.toLowerCase().includes(query)) ||
      u.phone.includes(query)
    )
  }
  return result.slice(0, 20)
})

function getAvatarClass(user: UserInfo) {
  const index = user.id ? user.id % colors.length : 0
  return colors[index]
}

function getInitial(user: UserInfo) {
  const name = user.realName || user.username || ''
  return name.charAt(0) || '?'
}

function getUserDesc(user: UserInfo) {
  if (user.role === 'PARENT') {
    return `注册 ${formatDate(user.createdAt)}`
  } else if (user.role === 'TEACHER') {
    if (user.status === 'PENDING') {
      return `提交 ${formatDate(user.createdAt)}`
    }
    return `教龄${user.experience || 0}年 · 授课${user.courseCount || 0}门 · 评分${user.rating || 4.0}`
  }
  return ''
}

function getStatusClass(user: UserInfo) {
  if (user.status === 'ACTIVE') return 's1'
  if (user.status === 'PENDING') return 's4'
  if (user.status === 'DISABLED') return 's3'
  return 's4'
}

function getStatusText(user: UserInfo) {
  if (user.role === 'TEACHER') {
    if (user.status === 'ACTIVE') return '在职'
    if (user.status === 'PENDING') return '待审'
    if (user.status === 'DISABLED') return '已离职'
  } else {
    if (user.status === 'ACTIVE') return '已认证'
    if (user.status === 'PENDING') return '待认证'
    if (user.status === 'DISABLED') return '已禁用'
  }
  return '未知'
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.navigateTo({ url: '/pages/admin_home/index' })
  }
}

function closeDetailModal() {
  showDetailModal.value = false
}

async function viewDetail(user: UserInfo) {
  currentUser.id = user.id
  currentUser.phone = user.phone
  currentUser.realName = user.realName
  currentUser.role = user.role
  currentUser.status = user.status
  currentUser.createdAt = user.createdAt
  currentUser.tags = (user as any).tags || ''

  if (user.role === 'PARENT') {
    try {
      children.value = await getStudentsByParentId(user.id)
    } catch {
      children.value = []
    }
  } else {
    children.value = []
  }

  showDetailModal.value = true
}

async function approveTeacher(user: UserInfo) {
  try {
    await apiApprove(user.id)
    user.status = 'ACTIVE'
    uni.showToast({ title: '审核通过', icon: 'success' })
    loadUsers()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function rejectTeacher(user: UserInfo) {
  try {
    await apiReject(user.id)
    user.status = 'REJECTED'
    uni.showToast({ title: '已驳回', icon: 'success' })
    loadUsers()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
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
  padding: 10px 16px;
  background: #fff;
}

.hd-back {
  display: flex;
  align-items: center;
  padding: 4px 0;
  color: #1d1d1f;
  font-size: 15px;

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
  margin-right: 28px;
}

.srch {
  margin: 10px 16px;
  position: relative;

  svg {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    width: 16px;
    height: 16px;
    color: #6e6e73;
  }
}

.srch-inp {
  width: 100%;
  padding: 9px 12px 9px 36px;
  border: 1px solid #e8e8ed;
  border-radius: 20px;
  background: #fff;
  font-size: 13px;
  color: #1d1d1f;

  &::placeholder {
    color: #6e6e73;
  }
}

.cnt {
  border: 1px solid #e8e8ed;
  background: #fff;
  border-radius: 12px;
  margin: 0 16px 10px;
  padding: 12px 14px;
  display: flex;
  gap: 12px;
}

.cnt-i {
  flex: 1;
  text-align: center;
  position: relative;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: 0;
    top: 4px;
    bottom: 4px;
    width: 1px;
    background: #e8e8ed;
  }
}

.cnt-n {
  font-size: 22px;
  font-weight: 700;
  color: #F05A22;
}

.cnt-l {
  font-size: 11px;
  color: #6e6e73;
  margin-top: 1px;
}

.tb {
  display: flex;
  margin: 0 16px 8px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e8e8ed;
  overflow: hidden;
}

.tb-i {
  flex: 1;
  text-align: center;
  padding: 9px 0;
  font-size: 12px;
  font-weight: 500;
  color: #6e6e73;
  position: relative;

  &.on {
    background: #F05A22;
    color: #fff;
    font-weight: 600;
  }

  &:not(.on):active {
    background: #fde8e1;
    color: #F05A22;
  }
}

.tb-badge {
  background: #dc2626;
  color: #fff;
  border-radius: 9px;
  padding: 1px 6px;
  font-size: 10px;
  margin-left: 2px;
}

.content {
  height: calc(100vh - 260px);
}

.ul {
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ui {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8ed;
  padding: 12px;
}

.ui-av {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: #fff;

  &.c1 {
    background: #F05A22;
  }

  &.c2 {
    background: #2196f3;
  }

  &.c3 {
    background: #4caf50;
  }

  &.c4 {
    background: #9c27b0;
  }

  &.c5 {
    background: #ff9800;
  }

  &.c6 {
    background: #e91e63;
  }
}

.ui-bd {
  flex: 1;
  min-width: 0;
}

.ui-un {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.ui-n {
  font-size: 13px;
  font-weight: 600;
}

.ui-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 500;

  &.t1 {
    background: #fde8e1;
    color: #F05A22;
  }

  &.t2 {
    background: #e3f2fd;
    color: #2196f3;
  }

  &.t3 {
    background: #e8f5e9;
    color: #4caf50;
  }

  &.t4 {
    background: #fff3e0;
    color: #ff9800;
  }

  &.t5 {
    background: #fce4ec;
    color: #e91e63;
  }

  &.t6 {
    background: #F05A22;
    color: #fff;
  }
}

.ui-um {
  font-size: 11px;
  color: #6e6e73;
  margin-top: 2px;
}

.ui-ur {
  text-align: right;
  flex-shrink: 0;
}

.ui-stat {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;

  &.s1 {
    background: #e8f5e9;
    color: #4caf50;
  }

  &.s2 {
    background: #fde8e1;
    color: #F05A22;
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

.rv-act {
  display: flex;
  gap: 6px;
  margin-top: 4px;
}

.rv-btn {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;

  &.vw {
    background: #fde8e1;
    color: #F05A22;
  }

  &.ok {
    background: #e8f5e9;
    color: #4caf50;
  }

  &.no {
    background: #fce4ec;
    color: #e91e63;
  }
}

.empty {
  padding: 40px;
  text-align: center;
  font-size: 13px;
  color: #6e6e73;
}

.bottom-space {
  height: 20px;
}

.detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;

  &.show {
    opacity: 1;
    pointer-events: auto;
  }
}

.dm-content {
  width: 100%;
  max-height: 85vh;
  background: #fff;
  border-radius: 24px 24px 0 0;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  .show & {
    transform: translateY(0);
  }
}

.dm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.dm-title {
  font-size: 18px;
  font-weight: 700;
  color: #1d1d1f;
}

.dm-close {
  font-size: 28px;
  color: #999;
  padding: 4px;
  line-height: 1;
}

.dm-body {
  max-height: calc(85vh - 68px);
  padding: 20px 24px;
}

.dm-avatar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0 24px;
  border-bottom: 1px solid #f0f0f0;
}

.dm-av {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

  &.c1 {
    background: linear-gradient(135deg, #F05A22, #ff7849);
  }

  &.c2 {
    background: linear-gradient(135deg, #2196f3, #64b5f6);
  }

  &.c3 {
    background: linear-gradient(135deg, #4caf50, #81c784);
  }

  &.c4 {
    background: linear-gradient(135deg, #9c27b0, #ba68c8);
  }

  &.c5 {
    background: linear-gradient(135deg, #ff9800, #ffb74d);
  }

  &.c6 {
    background: linear-gradient(135deg, #e91e63, #f48fb1);
  }
}

.dm-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dm-name {
  font-size: 22px;
  font-weight: 700;
  color: #1d1d1f;
}

.dm-role {
  font-size: 14px;
  color: #6e6e73;
  padding: 4px 12px;
  background: #f5f5f7;
  border-radius: 12px;
  display: inline-block;
  width: fit-content;
}

.dm-section {
  padding: 20px 0;
}

.dm-item {
  display: flex;
  flex-direction: column;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.dm-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.dm-value {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;

  &.s1 {
    color: #4caf50;
  }

  &.s2 {
    color: #F05A22;
  }

  &.s3 {
    color: #e91e63;
  }

  &.s4 {
    color: #6e6e73;
  }
}

.dm-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
}

.dm-tag {
  font-size: 13px;
  padding: 5px 14px;
  background: #fde8e1;
  color: #F05A22;
  border-radius: 20px;
  font-weight: 500;
}

.dm-tag-empty {
  font-size: 13px;
  color: #999;
  font-style: italic;
}

.dm-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 700;
  color: #1d1d1f;
}

.dm-st-icon {
  font-size: 18px;
}

.dm-st-text {
  font-weight: 600;
}

.dm-children {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dm-child {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.dc-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.dc-info {
  font-size: 13px;
  color: #6e6e73;
}

.dm-empty {
  padding: 30px;
  text-align: center;
  color: #999;
  font-size: 14px;
  background: #fafafa;
  border-radius: 12px;
}
</style>