<template>
  <view class="notification-page">
    <view class="hd">
      <view class="hd-back" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
          stroke-linejoin="round">
          <path d="m15 18-6-6 6-6" />
        </svg>
      </view>
      <text class="hd-title">消息通知</text>
      <view class="hd-action" @click="markAllRead" v-if="unreadCount > 0">
        <text class="hd-action-text">全部已读</text>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="timeline">
        <view class="group" v-for="group in groupedMessages" :key="group.date">
          <view class="group-date">{{ group.date }}</view>
          <view class="ml">
            <view class="mi" :class="{ unread: !msg.isRead }" v-for="msg in group.messages" :key="msg.id"
              @click="viewMessage(msg)">
              <view class="mi-icon" :class="getTypeClass(msg.type)">
                <text class="mi-icon-text">{{ getTypeIcon(msg.type) }}</text>
              </view>
              <view class="mi-content">
                <view class="mi-header">
                  <text class="mi-title">{{ msg.title }}</text>
                  <text class="mi-time">{{ formatTime(msg.createdAt) }}</text>
                </view>
                <text class="mi-desc">{{ msg.content }}</text>
              </view>
              <view class="mi-badge" v-if="!msg.isRead"></view>
            </view>
          </view>
        </view>
      </view>

      <view class="empty" v-if="messages.length === 0">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无消息通知</text>
        <text class="empty-sub">有新消息时会在这里显示</text>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMessages, markAsRead, markAllMessagesRead, type MessageItem } from '@/api/message'

const userStore = useUserStore()

const messages = ref<MessageItem[]>([])
const unreadCount = ref(0)

const groupedMessages = computed(() => {
  const groups: { date: string; messages: MessageItem[] }[] = []
  const dateMap = new Map<string, MessageItem[]>()

  messages.value.forEach(msg => {
    const date = formatDate(msg.createdAt)
    if (!dateMap.has(date)) {
      dateMap.set(date, [])
    }
    dateMap.get(date)!.push(msg)
  })

  dateMap.forEach((msgs, date) => {
    groups.push({ date, messages: msgs })
  })

  groups.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
  return groups
})

function formatDate(dateStr: string): string {
  if (!dateStr) return '未知时间'
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  } else {
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${month}月${day}日`
  }
}

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

function getTypeClass(type: string): string {
  switch (type) {
    case 'DAILY_SUMMARY': return 't-summary'
    case 'HOMEWORK': return 't-homework'
    case 'NOTIFICATION': return 't-notification'
    default: return 't-default'
  }
}

function getTypeIcon(type: string): string {
  switch (type) {
    case 'DAILY_SUMMARY': return '📖'
    case 'HOMEWORK': return '📝'
    case 'NOTIFICATION': return '🔔'
    default: return '📬'
  }
}

async function loadMessages() {
  try {
    const userId = userStore.userInfo?.userId || 0
    const data = await getMessages(userId, 0, 50)
    messages.value = data.list || []
    unreadCount.value = data.unreadCount || 0
  } catch {
    messages.value = []
    unreadCount.value = 0
  }
}

function viewMessage(msg: MessageItem) {
  if (!msg.isRead) {
    markAsRead(msg.id)
    msg.isRead = true
    unreadCount.value--
  }
  uni.showModal({
    title: msg.title,
    content: msg.content,
    showCancel: false,
    confirmText: '知道了'
  })
}

async function markAllRead() {
  try {
    const userId = userStore.userInfo?.userId || 0
    await markAllMessagesRead(userId)
    messages.value.forEach(msg => {
      msg.isRead = true
    })
    unreadCount.value = 0
    uni.showToast({ title: '已全部标记为已读', icon: 'success' })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/user_home/index' })
  }
}

onMounted(() => {
  userStore.loadFromStorage()
  loadMessages()
})
</script>

<style lang="scss" scoped>
.notification-page {
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
  margin-right: 40px;
}

.hd-action {
  padding: 6px 12px;
}

.hd-action-text {
  font-size: 14px;
  color: #F05A22;
  font-weight: 500;
}

.content {
  height: calc(100vh - 60px);
}

.timeline {
  padding: 12px;
}

.group-date {
  font-size: 12px;
  color: #999;
  padding: 8px 0;
  margin-bottom: 4px;
}

.ml {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 12px;
}

.mi {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #f5f5f7;
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #fafafa;
  }

  &.unread {
    background: #fff9f7;
  }
}

.mi-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.t-summary {
    background: linear-gradient(135deg, #667eea, #764ba2);
  }

  &.t-homework {
    background: linear-gradient(135deg, #4facfe, #00f2fe);
  }

  &.t-notification {
    background: linear-gradient(135deg, #F05A22, #ff7a45);
  }

  &.t-default {
    background: linear-gradient(135deg, #43e97b, #38f9d7);
  }
}

.mi-icon-text {
  font-size: 20px;
}

.mi-content {
  flex: 1;
  min-width: 0;
}

.mi-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.mi-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mi-time {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
  flex-shrink: 0;
}

.mi-desc {
  font-size: 13px;
  color: #6e6e73;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.mi-badge {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #F05A22;
  flex-shrink: 0;
  margin-top: 8px;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.empty-sub {
  font-size: 13px;
  color: #999;
}

.bottom-space {
  height: 40px;
}
</style>