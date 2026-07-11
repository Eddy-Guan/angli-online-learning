<template>
  <view class="course-page">
    <view class="header">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <text class="nav-title">选课中心</text>
      </view>
      <view class="search-bar">
        <view class="search-input-group">
          <text class="search-icon">🔍</text>
          <input 
            v-model="searchKeyword" 
            placeholder="搜索课程、老师" 
            class="search-input"
            confirm-type="search"
            @confirm="handleSearch"
          />
          <text class="search-clear" v-if="searchKeyword" @click="searchKeyword = ''">✕</text>
        </view>
      </view>
    </view>
    
    <scroll-view class="content" scroll-y>
      <view class="filter-tabs">
        <view 
          v-for="tab in tabs" 
          :key="tab.key"
          :class="['tab-item', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </view>
      </view>
      
      <view class="course-grid">
        <view 
          v-for="course in courses" 
          :key="course.id" 
          class="course-card"
          @click="goToCourseDetail(course.id)"
        >
          <image class="course-cover" :src="course.coverImage" mode="aspectFill" />
          <view class="course-tag" v-if="course.category">{{ course.category }}</view>
          <view class="course-favorite" @click.stop="toggleFavorite(course)">
            <text>{{ isFavoriteMap[course.id] ? '❤️' : '🤍' }}</text>
          </view>
          <view class="course-info">
            <text class="course-title ellipsis-2">{{ course.title }}</text>
            <view class="course-teacher">
              <image class="teacher-avatar" :src="course.teacherAvatar || 'https://via.placeholder.com/40'" mode="aspectFill" />
              <text class="teacher-name">{{ course.teacherName }}</text>
            </view>
            <view class="course-meta">
              <text class="course-duration">{{ course.totalHours }}课时</text>
              <text class="course-students">{{ course.enrollmentCount }}人已学</text>
            </view>
            <view class="course-bottom">
              <view class="price-group">
                <text class="course-price">¥{{ course.price }}</text>
                <text class="course-original" v-if="course.originalPrice && course.originalPrice > course.price">¥{{ course.originalPrice }}</text>
              </view>
              <view class="course-favorite-count">
                <text>{{ course.favoriteCount }}收藏</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getRecommendedCourses, getHotCourses, getAllPublishedCourses, searchCourses, addFavorite, removeFavorite, isFavorite, type Course } from '@/api/course'

const userStore = useUserStore()

const tabs = [
  { key: 'recommended', label: '推荐' },
  { key: 'hot', label: '热门' },
  { key: 'all', label: '全部' }
]

const activeTab = ref('recommended')
const searchKeyword = ref('')
const courses = ref<Course[]>([])
const isFavoriteMap = ref<Record<number, boolean>>({})

onMounted(async () => {
  userStore.loadFromStorage()
  await loadCourses()
})

async function loadCourses() {
  try {
    let result: Course[]
    
    if (searchKeyword.value.trim()) {
      result = await searchCourses(searchKeyword.value)
    } else {
      switch (activeTab.value) {
        case 'recommended':
          result = await getRecommendedCourses()
          break
        case 'hot':
          result = await getHotCourses()
          break
        default:
          result = await getAllPublishedCourses()
      }
    }
    
    courses.value = result
    await loadFavoriteStatus()
  } catch (err) {
    console.error('Load courses failed:', err)
  }
}

async function loadFavoriteStatus() {
  if (!userStore.userInfo) return
  
  for (const course of courses.value) {
    try {
      const result = await isFavorite(userStore.userInfo.userId, course.id)
      isFavoriteMap.value[course.id] = result.isFavorite
    } catch {
      isFavoriteMap.value[course.id] = false
    }
  }
}

async function toggleFavorite(course: Course) {
  if (!userStore.userInfo) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  
  const userId = userStore.userInfo.userId
  
  try {
    if (isFavoriteMap.value[course.id]) {
      await removeFavorite(userId, course.id)
      isFavoriteMap.value[course.id] = false
      course.favoriteCount--
    } else {
      await addFavorite(userId, course.id)
      isFavoriteMap.value[course.id] = true
      course.favoriteCount++
    }
  } catch (err) {
    console.error('Toggle favorite failed:', err)
  }
}

async function handleSearch() {
  await loadCourses()
}

watch(activeTab, () => {
  loadCourses()
})

function goToCourseDetail(id: number) {
  uni.navigateTo({ url: `/pages/user_course_detail/index?id=${id}` })
}
</script>

<style lang="scss" scoped>
.course-page {
  min-height: 100vh;
  background: $bg-color;
}

.header {
  background: #fff;
}

.status-bar {
  height: var(--status-bar-height, 44px);
}

.nav-bar {
  padding: 20rpx 32rpx;
  text-align: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $text-primary;
}

.search-bar {
  padding: 0 32rpx 24rpx;
}

.search-input-group {
  display: flex;
  align-items: center;
  background: $bg-color;
  border-radius: 40rpx;
  padding: 0 28rpx;
  height: 80rpx;
}

.search-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: $text-primary;
}

.search-clear {
  font-size: 28rpx;
  color: $text-muted;
  padding: 8rpx;
}

.content {
  height: calc(100vh - var(--status-bar-height, 44px) - 180rpx - 120rpx);
}

.filter-tabs {
  display: flex;
  background: #fff;
  padding: 16rpx 32rpx;
  gap: 32rpx;
  overflow-x: auto;
  white-space: nowrap;
  
  &::-webkit-scrollbar {
    display: none;
  }
}

.tab-item {
  font-size: 28rpx;
  color: $text-secondary;
  padding: 12rpx 0;
  position: relative;
  
  &.active {
    color: $primary-color;
    font-weight: 600;
    
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 40rpx;
      height: 4rpx;
      background: $primary-color;
      border-radius: 2rpx;
    }
  }
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  padding: 24rpx 32rpx;
}

.course-card {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  position: relative;
}

.course-cover {
  width: 100%;
  height: 200rpx;
}

.course-tag {
  position: absolute;
  top: 16rpx;
  left: 16rpx;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 20rpx;
  padding: 6rpx 16rpx;
  border-radius: 6rpx;
}

.course-favorite {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  font-size: 36rpx;
  padding: 8rpx;
}

.course-info {
  padding: 20rpx;
}

.course-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $text-primary;
  line-height: 1.4;
}

.course-teacher {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 12rpx;
}

.teacher-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
}

.teacher-name {
  font-size: 24rpx;
  color: $text-secondary;
}

.course-meta {
  display: flex;
  gap: 20rpx;
  margin-top: 12rpx;
}

.course-duration,
.course-students {
  font-size: 22rpx;
  color: $text-muted;
}

.course-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.price-group {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.course-price {
  font-size: 32rpx;
  font-weight: 700;
  color: $primary-color;
}

.course-original {
  font-size: 22rpx;
  color: $text-muted;
  text-decoration: line-through;
}

.course-favorite-count {
  font-size: 22rpx;
  color: $text-muted;
}

.bottom-space {
  height: 160rpx;
}
</style>
