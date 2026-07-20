<template>
  <view class="admin-page">
    <view class="hd">
      <view class="hd-back" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
          stroke-linejoin="round">
          <path d="m15 18-6-6 6-6" />
        </svg>
      </view>
      <text class="hd-title">发布公告</text>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="fm">
        <view class="sg">
          <view class="sg-h">公告内容</view>
          <view class="sg-b">
            <view class="fg">
              <text class="fg-label">公告标题 <text class="rq">*</text></text>
              <input class="inp" v-model="form.title" placeholder="输入公告标题" />
            </view>
            <view class="fg">
              <text class="fg-label">公告内容 <text class="rq">*</text></text>
              <textarea class="ta" v-model="form.content" placeholder="输入公告详细内容，支持换行" />
            </view>
            <view class="fg">
              <text class="fg-label">公告类型</text>
              <view class="type-list">
                <view class="type-item" :class="{ on: form.type === 'normal' }" @click="form.type = 'normal'">
                  <view class="type-icon">📋</view>
                  <text class="type-text">普通公告</text>
                </view>
                <view class="type-item" :class="{ on: form.type === 'important' }" @click="form.type = 'important'">
                  <view class="type-icon">📌</view>
                  <text class="type-text">重要通知</text>
                </view>
                <view class="type-item" :class="{ on: form.type === 'system' }" @click="form.type = 'system'">
                  <view class="type-icon">🔔</view>
                  <text class="type-text">系统公告</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="sg">
          <view class="sg-h">推送设置</view>
          <view class="sg-b">
            <view class="fg">
              <text class="fg-label">推送范围</text>
              <view class="range-list">
                <view class="range-item" :class="{ on: form.pushToUsers }"
                  @click="form.pushToUsers = !form.pushToUsers">
                  <view class="range-check" :class="{ checked: form.pushToUsers }">✓</view>
                  <text class="range-text">推送给所有用户</text>
                </view>
                <view class="range-item" :class="{ on: form.pushToTeachers }"
                  @click="form.pushToTeachers = !form.pushToTeachers">
                  <view class="range-check" :class="{ checked: form.pushToTeachers }">✓</view>
                  <text class="range-text">推送给教师</text>
                </view>
              </view>
            </view>
            <view class="fg">
              <text class="fg-label">发布时间</text>
              <view class="sw-g">
                <view class="sw" :class="{ on: form.scheduled }" @click="form.scheduled = !form.scheduled"></view>
                <text class="sw-text">{{ form.scheduled ? '定时发布' : '立即发布' }}</text>
              </view>
              <view v-if="form.scheduled" class="dt-picker">
                <picker mode="date" :value="form.scheduleDate" @change="onDateChange">
                  <view class="picker-value">
                    {{ form.scheduleDate || '选择日期' }}
                    <text class="picker-arrow">›</text>
                  </view>
                </picker>
                <picker mode="time" :value="form.scheduleTime" @change="onTimeChange">
                  <view class="picker-value">
                    {{ form.scheduleTime || '选择时间' }}
                    <text class="picker-arrow">›</text>
                  </view>
                </picker>
              </view>
            </view>
          </view>
        </view>

        <view class="btn-p" @click="doPublish">确认发布公告</view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { publishNotice } from '@/api/admin'

const form = reactive({
  title: '',
  content: '',
  type: 'normal' as 'normal' | 'important' | 'system',
  pushToUsers: true,
  pushToTeachers: true,
  scheduled: false,
  scheduleDate: '',
  scheduleTime: ''
})

function onDateChange(e: any) {
  form.scheduleDate = e.detail.value
}

function onTimeChange(e: any) {
  form.scheduleTime = e.detail.value
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.navigateTo({ url: '/pages/admin_home/index' })
  }
}

async function doPublish() {
  if (!form.title.trim()) {
    uni.showToast({ title: '请输入公告标题', icon: 'none' })
    return
  }
  if (!form.content.trim()) {
    uni.showToast({ title: '请输入公告内容', icon: 'none' })
    return
  }

  uni.showLoading({ title: '发布中...' })

  try {
    await publishNotice({
      title: form.title.trim(),
      content: form.content.trim(),
      type: form.type,
      pushToUsers: form.pushToUsers,
      pushToTeachers: form.pushToTeachers
    })

    uni.showToast({ title: '公告发布成功！', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack({ delta: 1 })
    }, 1500)
  } catch {
    uni.showToast({ title: '发布失败', icon: 'none' })
  } finally {
    uni.hideLoading()
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
  gap: 4px;
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

.content {
  height: calc(100vh - 60px);
}

.fm {
  padding: 16px;
}

.sg {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8ed;
  overflow: hidden;
  margin-bottom: 12px;
}

.sg-h {
  padding: 14px 16px 10px;
  font-size: 13px;
  font-weight: 600;
  border-bottom: 1px solid #e8e8ed;
}

.sg-b {
  padding: 10px 16px 14px;
}

.fg {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-bottom: 12px;

  &:last-child {
    margin-bottom: 0;
  }
}

.fg-label {
  font-size: 12px;
  font-weight: 600;
}

.rq {
  color: #dc2626;
  margin-left: 2px;
}

.inp {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #e8e8ed;
  border-radius: 10px;
  font-size: 14px;

  &::placeholder {
    color: #999;
  }
}

.ta {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #e8e8ed;
  border-radius: 10px;
  font-size: 14px;
  resize: vertical;
  min-height: 150px;
  line-height: 1.6;

  &::placeholder {
    color: #999;
  }
}

.type-list {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
}

.type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px 8px;
  border: 1.5px solid #e8e8ed;
  border-radius: 12px;
  background: #fff;
  transition: all 0.2s;

  &.on {
    border-color: #F05A22;
    background: #fff7f3;
  }
}

.type-icon {
  font-size: 24px;
}

.type-text {
  font-size: 12px;
  color: #6e6e73;

  .on & {
    color: #F05A22;
    font-weight: 600;
  }
}

.range-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.range-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border: 1.5px solid #e8e8ed;
  border-radius: 12px;
  background: #fff;

  &.on {
    border-color: #F05A22;
    background: #fff7f3;
  }
}

.range-check {
  width: 20px;
  height: 20px;
  border: 1.5px solid #e8e8ed;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: transparent;

  &.checked {
    background: #F05A22;
    border-color: #F05A22;
    color: #fff;
  }
}

.range-text {
  font-size: 14px;
  color: #1d1d1f;
}

.sw-g {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sw {
  width: 44px;
  height: 24px;
  border-radius: 12px;
  background: #e8e8ed;
  position: relative;
  flex-shrink: 0;

  &.on {
    background: #F05A22;
  }

  &::after {
    content: '';
    position: absolute;
    top: 2px;
    left: 2px;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #fff;
    transition: transform 0.2s;
  }

  &.on::after {
    transform: translateX(20px);
  }
}

.sw-text {
  font-size: 13px;
  color: #6e6e73;
}

.dt-picker {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 8px;
}

.picker-value {
  padding: 11px 14px;
  border: 1px solid #e8e8ed;
  border-radius: 10px;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #6e6e73;
}

.picker-arrow {
  font-size: 16px;
  color: #999;
}

.btn-p {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #F05A22, #ff7a45);
  color: #fff;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-top: 12px;
}

.bottom-space {
  height: 40px;
}
</style>