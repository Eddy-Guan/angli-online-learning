<template>
  <view class="admin-page">
    <view class="hd">
      <view class="hd-back" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
          stroke-linejoin="round">
          <path d="m15 18-6-6 6-6" />
        </svg>
      </view>
      <text class="hd-title">课程管理</text>
      <view class="hd-r">
        <view class="hd-add" @click="goToPublishCourse">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
        </view>
      </view>
    </view>

    <view class="srch">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
        stroke-linejoin="round">
        <circle cx="11" cy="11" r="8" />
        <path d="m21 21-4.35-4.35" />
      </svg>
      <input class="srch-input" placeholder="搜索课程名称、教师…" v-model="searchQuery" @input="handleSearch" />
    </view>

    <view class="st">
      <view class="st-i" :class="{ on: statusTab === 0 }" @click="statusTab = 0">全部</view>
      <view class="st-i" :class="{ on: statusTab === 1 }" @click="statusTab = 1">已上架</view>
      <view class="st-i" :class="{ on: statusTab === 2 }" @click="statusTab = 2">审核中</view>
      <view class="st-i" :class="{ on: statusTab === 3 }" @click="statusTab = 3">已结束</view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="cl">
        <view class="ci" v-for="course in filteredCourses" :key="course.id">
          <view class="ci-h">
            <view class="co" :class="getColorClass(course.id)">
              <text>{{ getCourseIcon(course.id) }}</text>
            </view>
            <view class="cb">
              <text class="cb-title">{{ course.title }}</text>
              <text class="cb-desc">{{ course.teacherName }} · {{ course.totalHours }}课时</text>
            </view>
            <text class="status" :class="getStatusClass(course.status)">{{ getStatusText(course.status) }}</text>
          </view>
          <view class="ci-b">
            <view class="info">
              <text class="info-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                  stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                </svg>
                {{ course.enrollmentCount }}人
              </text>
              <text class="info-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                  stroke-linejoin="round">
                  <path d="M12 2v20" />
                  <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
                </svg>
                ¥{{ course.price }}
              </text>
              <text class="info-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                  stroke-linejoin="round">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
                </svg>
                {{ course.totalHours }}课时
              </text>
            </view>
            <view class="act">
              <view class="act-btn view" @click="viewCourse(course.id)">查看</view>
              <view class="act-btn edit" @click="editCourse(course.id)">编辑</view>
              <view class="act-btn" :class="getActionClass(course.status)" @click="handleAction(course)">
                {{ getActionText(course.status) }}
              </view>
            </view>
          </view>
        </view>

        <view class="empty" v-if="filteredCourses.length === 0">
          <text>暂无课程</text>
        </view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getCourses, unpublishCourse, approveCourse, type CourseInfo } from '@/api/admin'

const userStore = useUserStore()

const searchQuery = ref('')
const statusTab = ref(0)
const courses = ref<CourseInfo[]>([])

const filteredCourses = computed(() => {
  let result = courses.value

  if (statusTab.value === 1) {
    result = result.filter(c => c.status === 'PUBLISHED')
  } else if (statusTab.value === 2) {
    result = result.filter(c => c.status === 'PENDING')
  } else if (statusTab.value === 3) {
    result = result.filter(c => c.status === 'ENDED')
  }

  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(c =>
      c.title.toLowerCase().includes(query) ||
      c.teacherName.toLowerCase().includes(query)
    )
  }

  return result
})

onMounted(() => {
  userStore.loadFromStorage()
  loadData()
})

watch(statusTab, () => {
  loadData()
})

async function loadData() {
  try {
    const result = await getCourses()
    courses.value = result
  } catch (err) {
    console.error('Load courses failed:', err)
  }
}

function handleSearch() {
}

function getCourseIcon(id: number): string {
  const icons = ['📐', '📖', '⚡', '🧪', '📘', '🎨']
  return icons[id % icons.length]
}

function getColorClass(id: number): string {
  const classes = ['c1', 'c2', 'c3', 'c4', 'c1', 'c2']
  return classes[id % classes.length]
}

function getStatusClass(status: string): string {
  switch (status) {
    case 'PUBLISHED': return 's1'
    case 'PENDING': return 's2'
    case 'ENDED': return 's3'
    default: return 's3'
  }
}

function getStatusText(status: string): string {
  switch (status) {
    case 'PUBLISHED': return '已上架'
    case 'PENDING': return '审核中'
    case 'ENDED': return '已结束'
    default: return '未知'
  }
}

function getActionClass(status: string): string {
  switch (status) {
    case 'PUBLISHED': return 'del'
    case 'PENDING': return 'edit'
    case 'ENDED': return 'del disabled'
    default: return 'del'
  }
}

function getActionText(status: string): string {
  switch (status) {
    case 'PUBLISHED': return '下架'
    case 'PENDING': return '上架'
    case 'ENDED': return '已结束'
    default: return '下架'
  }
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.navigateTo({ url: '/pages/admin_home/index' })
  }
}

function goToPublishCourse() {
  uni.navigateTo({ url: '/pages/admin_publish-course/index' })
}

function viewCourse(id: number) {
  uni.showToast({ title: '查看课程详情', icon: 'none' })
}

function editCourse(id: number) {
  uni.showToast({ title: '编辑课程功能开发中', icon: 'none' })
}

async function handleAction(course: CourseInfo) {
  if (course.status === 'ENDED') return

  const actionText = course.status === 'PUBLISHED' ? '下架' : '上架'
  uni.showModal({
    title: `确认${actionText}`,
    content: `确定要${actionText}课程「${course.title}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          if (course.status === 'PUBLISHED') {
            await unpublishCourse(course.id)
          } else {
            await approveCourse(course.id)
          }
          uni.showToast({ title: `${actionText}成功`, icon: 'success' })
          loadData()
        } catch {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
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

.hd-r {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hd-add {
  padding: 4px;
  color: #1d1d1f;

  svg {
    width: 22px;
    height: 22px;
    color: #6e6e73;
  }
}

.srch {
  margin: 10px 16px;
  position: relative;
}

.srch svg {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: #6e6e73;
}

.srch-input {
  width: 100%;
  padding: 9px 12px 9px 36px;
  border: 1px solid #e8e8ed;
  border-radius: 20px;
  background: #fff;
  font-size: 13px;
}

.st {
  display: flex;
  margin: 0 16px 8px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e8e8ed;
  overflow: hidden;
}

.st-i {
  flex: 1;
  text-align: center;
  padding: 9px 0;
  font-size: 12px;
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
  height: calc(100vh - 200px);
}

.cl {
  padding: 0 16px 16px;
}

.ci {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8ed;
  margin-bottom: 10px;
  overflow: hidden;
}

.ci-h {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px;
}

.co {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;

  &.c1 {
    background: #fde8e1;
  }

  &.c2 {
    background: #e3f2fd;
  }

  &.c3 {
    background: #fff3e0;
  }

  &.c4 {
    background: #e8f5e9;
  }
}

.cb {
  flex: 1;
  min-width: 0;
}

.cb-title {
  font-size: 13px;
  font-weight: 600;
  display: block;
}

.cb-desc {
  font-size: 11px;
  color: #6e6e73;
  margin-top: 1px;
  display: block;
}

.status {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  margin-left: auto;
  flex-shrink: 0;

  &.s1 {
    background: #e8f5e9;
    color: #4caf50;
  }

  &.s2 {
    background: #fff3e0;
    color: #ff9800;
  }

  &.s3 {
    background: #F5F5F7;
    color: #6e6e73;
  }
}

.ci-b {
  padding: 0 14px 14px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.info {
  flex: 1;
  font-size: 11px;
  color: #6e6e73;
  padding: 6px 0;
  display: flex;
  gap: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 2px;

  svg {
    width: 12px;
    height: 12px;
  }
}

.act {
  display: flex;
  gap: 6px;
}

.act-btn {
  padding: 5px 10px;
  border-radius: 5px;
  font-size: 11px;
  font-weight: 500;

  &.view {
    background: #F5F5F7;
    color: #6e6e73;
  }

  &.edit {
    background: #fde8e1;
    color: #F05A22;
  }

  &.del {
    background: #fce4ec;
    color: #e91e63;
  }

  &.disabled {
    background: #F5F5F7;
    color: #6e6e73;
  }

  &:active {
    opacity: 0.8;
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
</style>