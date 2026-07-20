<template>
  <view class="teacher-page">
    <view class="hd">
      <view class="hd-back" @click="goBack">←</view>
      <text class="hd-title">推送历史记录</text>
      <view class="hd-placeholder"></view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="empty" v-if="records.length === 0">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无推送记录</text>
      </view>

      <view class="record-list" v-else>
        <view class="course-group" v-for="(group, courseTitle) in groupedRecords" :key="courseTitle">
          <view class="cg-header">
            <text class="cg-icon">📚</text>
            <text class="cg-title">{{ courseTitle }}</text>
            <text class="cg-count">{{ group.length }}次推送</text>
          </view>
          <view class="record-item" v-for="record in group" :key="record.id" @click="showDetail(record)">
            <view class="ri-header">
              <text class="ri-time">{{ formatTime(record.createdAt) }}</text>
            </view>
            <view class="ri-body">
              <view class="ri-info" v-if="record.summary">
                <text class="ri-label">📖 总结：</text>
                <text class="ri-content">{{ record.summary.slice(0, 50) }}{{ record.summary.length > 50 ? '...' : '' }}</text>
              </view>
              <view class="ri-info" v-if="record.homework">
                <text class="ri-label">📝 作业：</text>
                <text class="ri-content">{{ record.homework.slice(0, 50) }}{{ record.homework.length > 50 ? '...' : '' }}</text>
              </view>
              <view class="ri-info" v-if="record.goodStudents">
                <text class="ri-label">⭐ 优秀学生：</text>
                <text class="ri-content">{{ record.goodStudents }}</text>
              </view>
            </view>
            <view class="ri-footer">
              <text class="ri-count">推送人数：{{ record.targetCount }}人</text>
              <text class="ri-arrow">›</text>
            </view>
          </view>
        </view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>

    <view class="overlay" :class="{ show: showDetailOverlay }">
      <view class="ol-header">
        <view class="ol-back" @click="closeDetail">← 返回</view>
        <text class="ol-title">推送详情</text>
        <view class="ol-placeholder"></view>
      </view>
      <scroll-view class="ol-content" scroll-y v-if="detail">
        <view class="detail-section">
          <text class="ds-title">📚 课程信息</text>
          <view class="ds-item">
            <text class="ds-label">课程名称</text>
            <text class="ds-value">{{ detail.record.courseTitle }}</text>
          </view>
          <view class="ds-item">
            <text class="ds-label">推送时间</text>
            <text class="ds-value">{{ detail.record.createdAt }}</text>
          </view>
          <view class="ds-item">
            <text class="ds-label">推送人数</text>
            <text class="ds-value">{{ detail.record.targetCount }}人</text>
          </view>
        </view>

        <view class="detail-section" v-if="detail.record.summary">
          <text class="ds-title">📖 今日总结</text>
          <text class="ds-text">{{ detail.record.summary }}</text>
        </view>

        <view class="detail-section" v-if="detail.record.homework">
          <text class="ds-title">📝 今日作业</text>
          <text class="ds-text">{{ detail.record.homework }}</text>
        </view>

        <view class="detail-section" v-if="detail.record.goodStudents">
          <text class="ds-title">⭐ 表现优秀学生</text>
          <text class="ds-text">{{ detail.record.goodStudents }}</text>
        </view>

        <view class="detail-section" v-if="detail.targetUsers && detail.targetUsers.length > 0">
          <text class="ds-title">👨‍👩‍👧‍👦 推送对象列表</text>
          <view class="tu-list">
            <view class="tu-item" v-for="user in detail.targetUsers" :key="user.id">
              <view class="tu-avatar">{{ user.realName.charAt(0) }}</view>
              <view class="tu-info">
                <text class="tu-name">{{ user.realName }}</text>
                <text class="tu-phone">{{ user.phone }}</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getPushRecords, getPushRecordDetail, type PushRecord, type PushRecordDetail } from '@/api/teacher'

const userStore = useUserStore()
const records = ref<PushRecord[]>([])
const showDetailOverlay = ref(false)
const detail = ref<PushRecordDetail | null>(null)

const groupedRecords = computed(() => {
  const groups: Record<string, PushRecord[]> = {}
  records.value.forEach(record => {
    const key = record.courseTitle || '未分类'
    if (!groups[key]) {
      groups[key] = []
    }
    groups[key].push(record)
  })
  return groups
})

onMounted(() => {
  userStore.loadFromStorage()
  loadRecords()
})

async function loadRecords() {
  if (!userStore.userInfo?.userId) return
  try {
    records.value = await getPushRecords(userStore.userInfo.userId)
  } catch (err) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

function formatTime(timeStr: string) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

async function showDetail(record: PushRecord) {
  if (!userStore.userInfo?.userId) return
  try {
    detail.value = await getPushRecordDetail(record.id, userStore.userInfo.userId)
    showDetailOverlay.value = true
  } catch (err) {
    uni.showToast({ title: '获取详情失败', icon: 'none' })
  }
}

function closeDetail() {
  showDetailOverlay.value = false
  detail.value = null
}

function goBack() {
  uni.navigateBack()
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
  font-size: 36rpx;
  font-weight: 700;
}

.hd-back {
  font-size: 40rpx;
  color: #333;
  padding: 8rpx 16rpx;
}

.hd-placeholder {
  width: 80rpx;
}

.content {
  height: calc(100vh - 120rpx);
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.record-list {
  padding: 24rpx 32rpx;
}

.course-group {
  margin-bottom: 32rpx;
}

.cg-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 24rpx;
  background: linear-gradient(135deg, #F05A22 0%, #FF7043 100%);
  border-radius: 16rpx;
  margin-bottom: 16rpx;
}

.cg-icon {
  font-size: 28rpx;
}

.cg-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
  flex: 1;
}

.cg-count {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.2);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.record-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 12rpx;
}

.record-item:last-child {
  margin-bottom: 0;
}

.ri-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.ri-time {
  font-size: 24rpx;
  color: #999;
}

.ri-body {
  margin-bottom: 16rpx;
}

.ri-info {
  display: flex;
  margin-bottom: 8rpx;
}

.ri-label {
  font-size: 24rpx;
  color: #F05A22;
  flex-shrink: 0;
}

.ri-content {
  font-size: 24rpx;
  color: #666;
  flex: 1;
}

.ri-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 2rpx solid #f5f5f5;
}

.ri-count {
  font-size: 22rpx;
  color: #999;
}

.ri-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.bottom-space {
  height: 60rpx;
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  z-index: 200;
  display: none;
}

.overlay.show {
  display: flex;
  flex-direction: column;
}

.ol-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60rpx 32rpx 24rpx;
  background: #fff;
  border-bottom: 2rpx solid #eee;
}

.ol-back {
  font-size: 28rpx;
  color: #F05A22;
}

.ol-title {
  font-size: 32rpx;
  font-weight: 600;
}

.ol-placeholder {
  width: 80rpx;
}

.ol-content {
  flex: 1;
  padding: 24rpx 32rpx;
}

.detail-section {
  background: #fafafa;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.ds-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
  display: block;
}

.ds-item {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
}

.ds-label {
  font-size: 24rpx;
  color: #999;
}

.ds-value {
  font-size: 24rpx;
  color: #333;
}

.ds-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}

.tu-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.tu-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 16rpx;
  background: #fff;
  border-radius: 16rpx;
}

.tu-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #F05A22;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 600;
}

.tu-info {
  flex: 1;
}

.tu-name {
  font-size: 26rpx;
  font-weight: 500;
  display: block;
}

.tu-phone {
  font-size: 22rpx;
  color: #999;
  display: block;
}
</style>