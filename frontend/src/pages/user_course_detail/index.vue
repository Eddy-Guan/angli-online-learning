<template>
  <view class="course-detail-page">
    <view class="header">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <view class="back-btn" @click="goBack" @tap="goBack" @click.stop="goBack">
          <text>←</text>
        </view>
        <text class="nav-title">课程详情</text>
        <view class="share-btn">
          <text>⋯</text>
        </view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="course-cover-section">
        <image class="course-cover" :src="course.coverImage || '/api/images/default-course.svg'" mode="aspectFill" />
        <view class="course-overlay">
          <view class="course-category">{{ course.category }}</view>
          <view class="course-favorite" @click="toggleFavorite">
            <text>{{ isFavoriteRef ? '❤️' : '🤍' }}</text>
          </view>
        </view>
      </view>

      <view class="course-info-card">
        <text class="course-title">{{ course.title }}</text>
        <view class="course-meta">
          <view class="teacher-info">
            <view v-if="isEmoji(course.teacherAvatar)" class="teacher-avatar-emoji">{{ course.teacherAvatar }}</view>
            <image v-else class="teacher-avatar"
              :src="course.teacherAvatar || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=teacher%20avatar%20professional%20portrait&image_size=square&width=60&height=60'"
              mode="aspectFill" />
            <text class="teacher-name">{{ course.teacherName }}</text>
          </view>
          <view class="course-stats">
            <text class="stat-item">{{ course.totalHours }}课时</text>
            <text class="stat-divider">|</text>
            <text class="stat-item">{{ course.enrollmentCount }}人已学</text>
          </view>
        </view>
        <view class="course-price-row">
          <view class="price-group" v-if="!isPurchased">
            <text class="course-price">¥{{ course.price }}</text>
            <text class="course-original" v-if="course.originalPrice && course.originalPrice > course.price">¥{{
              course.originalPrice }}</text>
          </view>
          <view class="purchased-tag" v-else>
            <text>✓ 已购买</text>
          </view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-header">
          <text class="section-title">课程简介</text>
        </view>
        <text class="course-description">{{ course.description }}</text>
      </view>

      <view class="section-card">
        <view class="section-header">
          <text class="section-title">课程目录</text>
        </view>
        <view class="chapter-list">
          <view class="chapter-item" v-for="(chapter, index) in chapters" :key="chapter.id">
            <view class="chapter-header" @click="toggleChapter(index)">
              <text class="chapter-order">{{ chapter.chapterOrder || index + 1 }}</text>
              <text class="chapter-name">{{ chapter.chapterName }}</text>
              <text class="chapter-duration">{{ chapter.duration }}</text>
              <text class="chapter-arrow">{{ expandedChapters[index] ? '▼' : '▶' }}</text>
            </view>
            <view class="chapter-content" v-if="expandedChapters[index]">
              <text>{{ chapter.content }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-header">
          <text class="section-title">课程评价</text>
          <text class="section-more">查看全部 →</text>
        </view>
        <view class="evaluation-list" v-if="evaluations.length > 0">
          <view class="evaluation-item" v-for="item in evaluations" :key="item.id">
            <view class="evaluation-header">
              <view class="evaluator-info">
                <text class="evaluator-name">用户{{ item.userId }}</text>
                <view class="rating-stars">
                  <text v-for="i in 5" :key="i">{{ i <= item.rating ? '★' : '☆' }}</text>
                </view>
              </view>
              <text class="evaluation-date">{{ item.createdAt?.split('T')[0] }}</text>
            </view>
            <text class="evaluation-comment">{{ item.comment }}</text>
          </view>
        </view>
        <view class="empty-evaluation" v-else>
          <text>暂无评价</text>
        </view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="bar-left">
        <view class="bar-item" @click="goToFavorites">
          <text>❤️</text>
          <text class="bar-label">收藏</text>
        </view>
        <view class="bar-item" @click="goToShare">
          <text>📤</text>
          <text class="bar-label">分享</text>
        </view>
      </view>
      <view class="bar-right">
        <view class="btn-secondary" @click="goToContact">联系老师</view>
        <view class="btn-primary" v-if="!isPurchased" @click="handleBuy">立即购买</view>
        <view class="btn-primary" v-else @click="goToLearn">去学习</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { getCourseById, addFavorite, removeFavorite, isFavorite, getCourseChapters, getCourseEvaluations, type Course, type CourseChapter, type Evaluation } from '@/api/course'
import { createOrder, payOrder, getOrdersByUserId, type PayResult } from '@/api/order'

const userStore = useUserStore()

const course = ref<Course>({
  id: 0,
  title: '',
  description: '',
  coverImage: '',
  price: 0,
  originalPrice: 0,
  totalHours: 0,
  teacherName: '',
  teacherAvatar: '',
  status: '',
  category: '',
  enrollmentCount: 0,
  favoriteCount: 0,
  createdAt: ''
})

const isFavoriteRef = ref(false)
const isPurchased = ref(false)
const chapters = ref<CourseChapter[]>([])
const expandedChapters = ref<Record<number, boolean>>({})
const evaluations = ref<Evaluation[]>([])

function isEmoji(str: string): boolean {
  if (!str) return false
  const emojiRegex = /[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F900}-\u{1F9FF}]/u
  return emojiRegex.test(str)
}

onLoad(async (options) => {
  const id = parseInt(options?.id || '1')
  await loadCourse(id)
})

onMounted(() => {
  userStore.loadFromStorage()
})

async function loadCourse(id: number) {
  try {
    const [result, courseChapters, courseEvaluations] = await Promise.all([
      getCourseById(id),
      getCourseChapters(id),
      getCourseEvaluations(id)
    ])
    course.value = result
    chapters.value = courseChapters
    evaluations.value = courseEvaluations

    if (userStore.userInfo) {
      const favoriteStatus = await isFavorite(userStore.userInfo.userId, id)
      isFavoriteRef.value = favoriteStatus.isFavorite

      await checkPurchasedStatus(id)
    }
  } catch (err) {
    console.error('Load course failed:', err)
  }
}

async function checkPurchasedStatus(courseId: number) {
  isPurchased.value = false

  if (!userStore.userInfo) return

  try {
    const orders = await getOrdersByUserId(userStore.userInfo.userId)
    const paidOrders = orders.filter(o => o.status === 'PAID')
    isPurchased.value = paidOrders.some(o => o.courseId === courseId)
  } catch (err) {
    console.error('Check purchased status failed:', err)
  }
}

function toggleChapter(index: number) {
  expandedChapters.value[index] = !expandedChapters.value[index]
}

async function toggleFavorite() {
  if (!userStore.userInfo) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  try {
    if (isFavoriteRef.value) {
      await removeFavorite(userStore.userInfo.userId, course.value.id)
      isFavoriteRef.value = false
    } else {
      await addFavorite(userStore.userInfo.userId, course.value.id)
      isFavoriteRef.value = true
    }
  } catch (err) {
    console.error('Toggle favorite failed:', err)
  }
}

async function handleBuy() {
  if (!userStore.userInfo) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  uni.showModal({
    title: '确认购买',
    content: `确认购买课程《${course.value.title}》，价格 ¥${course.value.price}`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const order = await createOrder(userStore.userInfo.userId, { courseId: course.value.id })

          uni.showLoading({ title: '支付中...' })

          await new Promise(resolve => setTimeout(resolve, 1500))

          const payResult = await payOrder(order.id)

          uni.hideLoading()

          if (payResult.success) {
            userStore.addPurchasedCourse(course.value.id)
            uni.showToast({ title: '购买成功', icon: 'success' })
            setTimeout(() => {
              uni.switchTab({ url: '/pages/user_learn/index' })
            }, 1500)
          } else {
            uni.showToast({ title: '支付失败', icon: 'none' })
          }
        } catch (err: any) {
          uni.hideLoading()
          const errorMsg = err.message || (err.data?.message) || '购买失败'
          uni.showToast({ title: errorMsg, icon: 'none' })
        }
      }
    }
  })
}

function goBack() {
  uni.switchTab({
    url: '/pages/user_course/index',
    fail: () => {
      uni.redirectTo({
        url: '/pages/user_course/index',
        fail: () => {
          if (typeof window !== 'undefined' && window.history && window.history.length > 1) {
            window.history.back()
          } else {
            window.location.href = '/pages/user_course/index'
          }
        }
      })
    }
  })
}

function goToFavorites() {
  uni.showToast({ title: '收藏功能开发中', icon: 'none' })
}

function goToShare() {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

function goToContact() {
  uni.showToast({ title: '联系老师功能开发中', icon: 'none' })
}

function goToLearn() {
  uni.switchTab({ url: '/pages/user_learn/index' })
}
</script>

<style lang="scss" scoped>
.course-detail-page {
  min-height: 100vh;
  background: $bg-color;
}

.header {
  background: #fff;
  position: relative;
  z-index: 100;
}

.status-bar {
  height: var(--status-bar-height, 44px);
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 32rpx;
}

.back-btn,
.share-btn {
  font-size: 40rpx;
  color: $text-primary;
  padding: 12rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: $text-primary;
}

.content {
  height: calc(100vh - var(--status-bar-height, 44px) - 80rpx - 120rpx);
}

.course-cover-section {
  position: relative;
  height: 400rpx;
}

.course-cover {
  width: 100%;
  height: 100%;
}

.course-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.course-category {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
}

.course-favorite {
  font-size: 48rpx;
}

.course-info-card {
  background: #fff;
  margin-top: -40rpx;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx;
  position: relative;
}

.course-title {
  font-size: 40rpx;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.4;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.teacher-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.teacher-avatar-emoji {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fde8e1;
  font-size: 32rpx;
}

.teacher-name {
  font-size: 28rpx;
  color: $text-secondary;
}

.course-stats {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.stat-item {
  font-size: 26rpx;
  color: $text-muted;
}

.stat-divider {
  color: $border-color;
}

.course-price-row {
  margin-top: 24rpx;
}

.price-group {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.course-price {
  font-size: 48rpx;
  font-weight: 700;
  color: $primary-color;
}

.purchased-tag {
  background: #52c41a;
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  padding: 16rpx 32rpx;
  border-radius: 8rpx;
}

.course-original {
  font-size: 28rpx;
  color: $text-muted;
  text-decoration: line-through;
}

.section-card {
  background: #fff;
  margin: 24rpx 0;
  padding: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-primary;
}

.section-more {
  font-size: 26rpx;
  color: $text-secondary;
}

.course-description {
  font-size: 28rpx;
  color: $text-secondary;
  line-height: 1.8;
}

.chapter-list {
  border-top: 2rpx solid $border-color;
}

.chapter-item {
  padding: 24rpx 0;
  border-bottom: 2rpx solid $border-color;

  &:last-child {
    border-bottom: none;
  }
}

.chapter-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.chapter-order {
  width: 48rpx;
  height: 48rpx;
  background: $primary-light;
  color: $primary-color;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 600;
}

.chapter-name {
  flex: 1;
  font-size: 28rpx;
  color: $text-primary;
}

.chapter-duration {
  font-size: 24rpx;
  color: $text-muted;
}

.chapter-arrow {
  font-size: 20rpx;
  color: $text-muted;
}

.chapter-content {
  margin-top: 16rpx;
  margin-left: 64rpx;
  padding: 16rpx;
  background: $bg-color;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: $text-secondary;
  line-height: 1.6;
}

.evaluation-list {
  border-top: 2rpx solid $border-color;
}

.evaluation-item {
  padding: 24rpx 0;
  border-bottom: 2rpx solid $border-color;

  &:last-child {
    border-bottom: none;
  }
}

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.evaluator-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.evaluator-name {
  font-size: 28rpx;
  color: $text-primary;
}

.rating-stars {
  font-size: 24rpx;
  color: #FFD700;
}

.evaluation-date {
  font-size: 24rpx;
  color: $text-muted;
}

.evaluation-comment {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: $text-secondary;
  line-height: 1.6;
}

.empty-evaluation {
  text-align: center;
  padding: 48rpx;
  font-size: 28rpx;
  color: $text-muted;
}

.bottom-space {
  height: 160rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.bar-left {
  display: flex;
  gap: 40rpx;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  font-size: 36rpx;

  .bar-label {
    font-size: 22rpx;
    color: $text-secondary;
  }
}

.bar-right {
  display: flex;
  gap: 20rpx;
}

.btn-secondary {
  padding: 20rpx 32rpx;
  border: 2rpx solid $primary-color;
  color: $primary-color;
  border-radius: 40rpx;
  font-size: 28rpx;
}

.btn-primary {
  padding: 20rpx 48rpx;
  background: $primary-color;
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
}
</style>
