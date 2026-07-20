<template>
  <view class="teacher-page">
    <view class="hd">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="hd-title">申请记录</text>
      <view class="placeholder"></view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="filter-bar">
        <view class="filter-item" :class="{ on: filterStatus === '' }" @click="filterStatus = ''">全部</view>
        <view class="filter-item" :class="{ on: filterStatus === 'PENDING' }" @click="filterStatus = 'PENDING'">审核中</view>
        <view class="filter-item" :class="{ on: filterStatus === 'APPROVED' }" @click="filterStatus = 'APPROVED'">已通过</view>
        <view class="filter-item" :class="{ on: filterStatus === 'REJECTED' }" @click="filterStatus = 'REJECTED'">已驳回</view>
      </view>

      <view class="record-list">
        <view class="record-card" v-for="record in filteredRecords" :key="record.id">
          <view class="record-header">
            <text class="record-title">{{ record.title }}</text>
            <text class="record-status" :class="record.status.toLowerCase()">{{ statusText(record.status) }}</text>
          </view>
          <view class="record-info">
            <text class="info-item">📚 {{ record.subject || '-' }} · {{ record.grade || '-' }}</text>
            <text class="info-item">⏱️ {{ record.totalHours || 0 }}课时</text>
            <text class="info-item">📅 {{ formatDate(record.appliedAt) }}</text>
          </view>
          <view class="record-chapters">
            <text class="chapter-tag" v-for="(ch, idx) in record.chapters.slice(0, 5)" :key="idx">{{ ch }}</text>
            <text class="chapter-tag more" v-if="record.chapters.length > 5">+{{ record.chapters.length - 5 }}</text>
          </view>
          <view class="record-footer" v-if="record.rejectReason">
            <text class="reject-reason">驳回原因：{{ record.rejectReason }}</text>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="filteredRecords.length === 0">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无{{ filterStatus ? statusText(filterStatus) : '' }}申请记录</text>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>

    <nav class="bn">
      <view class="ni" @click="goToHome">
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
import { getApplyRecords } from '@/api/teacher'

const userStore = useUserStore()

const filterStatus = ref('')
const applyRecords = ref<any[]>([])

const filteredRecords = computed(() => {
  if (!filterStatus.value) return applyRecords.value
  return applyRecords.value.filter(r => r.status === filterStatus.value)
})

onMounted(() => {
  userStore.loadFromStorage()
  loadRecords()
})

async function loadRecords() {
  if (!userStore.userInfo) return
  
  try {
    const records = await getApplyRecords(userStore.userInfo.userId)
    applyRecords.value = records.map(r => {
      let chapters = []
      if (r.chaptersJson) {
        try {
          const parsed = JSON.parse(r.chaptersJson)
          chapters = parsed.map((c: any) => c.title)
        } catch (e) {
          chapters = []
        }
      }
      return {
        ...r,
        chapters
      }
    })
  } catch (err) {
    console.error('Load apply records failed:', err)
    uni.showToast({ title: '加载申请记录失败', icon: 'none' })
  }
}

function statusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: '审核中',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function goBack() {
  uni.navigateBack()
}

function goToHome() {
  uni.redirectTo({ url: '/pages/teacher_home/index' })
}

function goToApply() {
  uni.redirectTo({ url: '/pages/teacher_apply/index' })
}

function goToCourses() {
  uni.redirectTo({ url: '/pages/teacher_courses/index' })
}

function goToPush() {
  uni.redirectTo({ url: '/pages/teacher_push/index' })
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
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 40rpx;
  color: #333;
}

.hd-title {
  font-size: 36rpx;
  font-weight: 700;
}

.placeholder {
  width: 64rpx;
}

.filter-bar {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  background: #fff;
  margin-bottom: 12rpx;
}

.filter-item {
  padding: 16rpx 32rpx;
  background: #f5f5f5;
  border-radius: 40rpx;
  font-size: 24rpx;
  color: #666;

  &.on {
    background: #F05A22;
    color: #fff;
    font-weight: 500;
  }
}

.record-list {
  padding: 0 24rpx;
}

.record-card {
  background: #fff;
  border-radius: 20rpx;
  border: 2rpx solid #eee;
  padding: 28rpx;
  margin-bottom: 20rpx;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.record-title {
  font-size: 30rpx;
  font-weight: 600;
  flex: 1;
}

.record-status {
  display: inline-block;
  padding: 6rpx 24rpx;
  border-radius: 40rpx;
  font-size: 22rpx;
  font-weight: 500;
  margin-left: 16rpx;

  &.pending {
    background: #fff7e6;
    color: #fa8c16;
    border: 2rpx solid #ffd591;
  }

  &.approved {
    background: #f6ffed;
    color: #52c41a;
    border: 2rpx solid #b7eb8f;
  }

  &.rejected {
    background: #fff2f0;
    color: #ff4d4d;
    border: 2rpx solid #ffccc7;
  }
}

.record-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.info-item {
  font-size: 24rpx;
  color: #999;
}

.record-chapters {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.chapter-tag {
  padding: 6rpx 20rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  font-size: 22rpx;
  color: #666;

  &.more {
    background: #fde8e1;
    color: #F05A22;
  }
}

.record-footer {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 2rpx solid #f5f5f5;
}

.reject-reason {
  font-size: 24rpx;
  color: #ff4d4d;
}

.empty-state {
  text-align: center;
  padding: 120rpx 32rpx;
}

.empty-icon {
  font-size: 120rpx;
  display: block;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
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