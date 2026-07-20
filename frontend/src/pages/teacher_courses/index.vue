<template>
  <view class="teacher-page">
    <view class="hd">
      <text class="hd-title">我的课程</text>
      <text class="hd-link">全部 →</text>
    </view>

    <view class="tb">
      <text class="t" :class="{ on: activeTab === 0 }" @click="switchTab(0)">📖 进行中</text>
      <text class="t" :class="{ on: activeTab === 1 }" @click="switchTab(1)">✅ 已结束</text>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="course-list" v-if="activeTab === 0">
        <view class="cc" v-for="course in activeCourses" :key="course.id">
          <view class="cc-h" @click="toggleCourse(course.id)">
            <view class="th">{{ courseIcon(course.id) }}</view>
            <view class="in">
              <text class="cc-title">{{ course.title }} <text class="cc-update">更新进度</text></text>
              <text class="cc-meta">{{ course.studentCount }}名学生 · 已上 {{ course.completedHours }}/{{ course.totalHours }} 课时</text>
            </view>
            <text class="ar" :class="{ open: expandedCourses.includes(course.id) }">▾</text>
          </view>
          <view class="cc-b" :class="{ open: expandedCourses.includes(course.id) }">
            <view class="ci">
              <view class="ph">
                <text class="ci-title">课程进度</text>
                <text class="ci-pct" style="color: #F05A22">{{ getProgress(course) }}%</text>
              </view>
              <view class="pbr">
                <view class="fl" :style="{ width: getProgress(course) + '%' }"></view>
              </view>
              <view class="stat">
                <text class="stat-text">已完成 <text class="stat-bold">{{ course.completedHours }}</text> · 共 {{ course.totalHours }} 课时</text>
                <picker mode="selector" :range="hourOptions(course.totalHours)" @change="(e) => quickSetProgress(course.id, e)">
                  <view class="prog-sel">
                    {{ course.completedHours }} 课时
                  </view>
                </picker>
              </view>
            </view>
            <view class="ll" v-for="chapter in getCourseChapters(course.id)" :key="chapter.id">
              <view class="no" :class="{ done: chapter.isCompleted }">{{ chapter.chapterNumber }}</view>
              <view class="in">
                <text class="ln">{{ chapter.title }}</text>
                <text class="ls">{{ chapter.isCompleted ? '已完成' : '可标记' }}</text>
              </view>
              <view class="mk" :class="{ do: chapter.isCompleted, und: !chapter.isCompleted }" @click="toggleChapter(chapter)">
                {{ chapter.isCompleted ? '已完成' : '标记完成' }}
              </view>
            </view>
          </view>
        </view>
        <view class="empty-card" v-if="activeCourses.length === 0">
          <text class="empty-icon">📚</text>
          <text class="empty-text">暂无进行中的课程</text>
        </view>
      </view>

      <view class="course-list" v-if="activeTab === 1">
        <view class="cr" v-for="course in completedCourses" :key="course.id">
          <view class="th">{{ courseIcon(course.id) }}</view>
          <view class="in">
            <text class="cr-title">{{ course.title }}</text>
            <text class="cr-meta">{{ course.studentCount }}名学生 · 已结课</text>
            <text class="cr-rating">⭐ {{ course.averageRating || '4.9' }} 分 · {{ course.reviewCount || '32' }}人评价</text>
          </view>
        </view>
        <view class="empty-card" v-if="completedCourses.length === 0">
          <text class="empty-icon">✅</text>
          <text class="empty-text">暂无已结束的课程</text>
        </view>
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
      <view class="ni on">
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
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTeacherCourses, getCourseChapters, toggleChapterComplete, type TeacherCourse, type CourseChapter } from '@/api/teacher'

const userStore = useUserStore()

const activeTab = ref(0)
const expandedCourses = ref<number[]>([])
const courses = ref<TeacherCourse[]>([])
const chaptersMap = ref<Map<number, CourseChapter[]>>(new Map())

const icons = ['📚', '📘', '📐', '📕', '📗']

function courseIcon(id: number): string {
  return icons[id % icons.length]
}

function getProgress(course: TeacherCourse): number {
  if (course.totalHours === 0) return 0
  return Math.round((course.completedHours / course.totalHours) * 100)
}

function hourOptions(totalHours: number): string[] {
  const options: string[] = []
  for (let i = 0; i <= totalHours; i++) {
    options.push(`${i} 课时`)
  }
  return options
}

function getCourseChapters(courseId: number): CourseChapter[] {
  return chaptersMap.value.get(courseId) || []
}

onMounted(() => {
  userStore.loadFromStorage()
  loadCourses()
})

async function loadCourses() {
  if (!userStore.userInfo) return
  
  try {
    const data = await getTeacherCourses(userStore.userInfo.userId)
    courses.value = data
    
    for (const course of data) {
      if (course.status === 'ACTIVE') {
        const chapters = await getCourseChapters(course.id)
        chaptersMap.value.set(course.id, chapters)
      }
    }
  } catch (err) {
    console.error('Load courses failed:', err)
  }
}

const activeCourses = ref<TeacherCourse[]>([
  { id: 1, title: '初一数学 · 暑期培优班', subject: '数学', grade: '初一', totalHours: 16, completedHours: 8, studentCount: 38, status: 'ACTIVE', teacherId: 1, createdAt: '' },
  { id: 2, title: '初二数学 · 几何强化', subject: '数学', grade: '初二', totalHours: 12, completedHours: 6, studentCount: 31, status: 'ACTIVE', teacherId: 1, createdAt: '' },
  { id: 3, title: '初三数学 · 中考冲刺班', subject: '数学', grade: '初三', totalHours: 20, completedHours: 10, studentCount: 48, status: 'ACTIVE', teacherId: 1, createdAt: '' }
])

const completedCourses = ref<TeacherCourse[]>([
  { id: 4, title: '初一数学 · 春季培优班', subject: '数学', grade: '初一', totalHours: 16, completedHours: 16, studentCount: 35, status: 'COMPLETED', teacherId: 1, createdAt: '', endAt: '2026-06-30' },
  { id: 5, title: '初二数学 · 寒假强化班', subject: '数学', grade: '初二', totalHours: 12, completedHours: 12, studentCount: 28, status: 'COMPLETED', teacherId: 1, createdAt: '', endAt: '2026-02-15' }
])

chaptersMap.value.set(1, [
  { id: 1, courseId: 1, chapterNumber: 1, title: '有理数的运算', description: '', isCompleted: true },
  { id: 2, courseId: 1, chapterNumber: 3, title: '一元一次方程', description: '', isCompleted: true },
  { id: 3, courseId: 1, chapterNumber: 8, title: '三角形初步', description: '', isCompleted: true },
  { id: 4, courseId: 1, chapterNumber: 13, title: '实数', description: '', isCompleted: false }
])

chaptersMap.value.set(2, [
  { id: 1, courseId: 2, chapterNumber: 1, title: '全等三角形', description: '', isCompleted: true },
  { id: 2, courseId: 2, chapterNumber: 2, title: '等腰三角形', description: '', isCompleted: true },
  { id: 3, courseId: 2, chapterNumber: 3, title: '直角三角形', description: '', isCompleted: true },
  { id: 4, courseId: 2, chapterNumber: 4, title: '勾股定理', description: '', isCompleted: true },
  { id: 5, courseId: 2, chapterNumber: 5, title: '平行四边形', description: '', isCompleted: true },
  { id: 6, courseId: 2, chapterNumber: 6, title: '梯形', description: '', isCompleted: true },
  { id: 7, courseId: 2, chapterNumber: 7, title: '圆的基本性质', description: '', isCompleted: false },
  { id: 8, courseId: 2, chapterNumber: 8, title: '圆周角定理', description: '', isCompleted: false }
])

chaptersMap.value.set(3, [
  { id: 1, courseId: 3, chapterNumber: 1, title: '实数与数轴', description: '', isCompleted: true },
  { id: 2, courseId: 3, chapterNumber: 2, title: '代数式运算', description: '', isCompleted: true },
  { id: 3, courseId: 3, chapterNumber: 3, title: '方程与不等式', description: '', isCompleted: true },
  { id: 4, courseId: 3, chapterNumber: 4, title: '函数概念', description: '', isCompleted: true },
  { id: 5, courseId: 3, chapterNumber: 5, title: '一次函数', description: '', isCompleted: true },
  { id: 6, courseId: 3, chapterNumber: 6, title: '二次函数', description: '', isCompleted: true },
  { id: 7, courseId: 3, chapterNumber: 7, title: '反比例函数', description: '', isCompleted: true },
  { id: 8, courseId: 3, chapterNumber: 8, title: '几何基本定理', description: '', isCompleted: true },
  { id: 9, courseId: 3, chapterNumber: 9, title: '圆与三角形', description: '', isCompleted: true },
  { id: 10, courseId: 3, chapterNumber: 10, title: '锐角三角函数', description: '', isCompleted: true },
  { id: 11, courseId: 3, chapterNumber: 11, title: '统计初步', description: '', isCompleted: false },
  { id: 12, courseId: 3, chapterNumber: 12, title: '概率初步', description: '', isCompleted: false }
])

function switchTab(index: number) {
  activeTab.value = index
}

function toggleCourse(courseId: number) {
  const index = expandedCourses.value.indexOf(courseId)
  if (index > -1) {
    expandedCourses.value.splice(index, 1)
  } else {
    expandedCourses.value.push(courseId)
  }
}

function quickSetProgress(courseId: number, e: any) {
  const hours = e.detail.value
  const course = courses.value.find(c => c.id === courseId) || activeCourses.value.find(c => c.id === courseId)
  if (course) {
    course.completedHours = hours
  }
}

async function toggleChapter(chapter: CourseChapter) {
  chapter.isCompleted = !chapter.isCompleted
  
  try {
    await toggleChapterComplete(chapter.courseId, chapter.id)
  } catch (err) {
    console.error('Toggle chapter failed:', err)
  }
}

function goToHome() {
  uni.navigateTo({ url: '/pages/teacher_home/index' })
}

function goToApply() {
  uni.navigateTo({ url: '/pages/teacher_apply/index' })
}

function goToPush() {
  uni.navigateTo({ url: '/pages/teacher_push/index' })
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

.hd-link {
  font-size: 26rpx;
  color: #999;
}

.tb {
  display: flex;
  background: #fff;
  padding: 0 32rpx;
  border-bottom: 2rpx solid #eee;
}

.t {
  font-size: 28rpx;
  padding: 20rpx 28rpx;
  border-bottom: 4rpx solid transparent;
  color: #999;
}

.t.on {
  color: #F05A22;
  border-bottom-color: #F05A22;
  font-weight: 600;
}

.content {
  height: calc(100vh - 200rpx);
}

.course-list {
  padding: 16rpx;
}

.cc {
  margin: 16rpx 0;
  background: #fff;
  border-radius: 24rpx;
  border: 2rpx solid #eee;
  overflow: hidden;
}

.cc-h {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 28rpx;
}

.th {
  width: 96rpx;
  height: 96rpx;
  background: #fde8e1;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  flex-shrink: 0;
}

.in {
  flex: 1;
}

.cc-title {
  font-size: 28rpx;
  font-weight: 600;
  display: block;
  margin-bottom: 4rpx;
}

.cc-update {
  font-size: 22rpx;
  color: #F05A22;
  font-weight: 400;
}

.cc-meta {
  font-size: 24rpx;
  color: #999;
  display: block;
}

.ar {
  font-size: 28rpx;
  color: #999;
  transition: transform 0.2s;
}

.ar.open {
  transform: rotate(180deg);
}

.cc-b {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.cc-b.open {
  max-height: 1200rpx;
}

.ci {
  margin: 0 28rpx 28rpx;
  padding: 24rpx;
  background: #fafafa;
  border-radius: 20rpx;
}

.ph {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.ci-title {
  font-size: 26rpx;
  font-weight: 600;
}

.ci-pct {
  font-size: 26rpx;
  font-weight: 600;
}

.pbr {
  height: 8rpx;
  background: #fde8e1;
  border-radius: 4rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.fl {
  height: 100%;
  background: #F05A22;
  border-radius: 4rpx;
  transition: width 0.3s;
}

.stat {
  display: flex;
  gap: 24rpx;
  align-items: center;
}

.stat-text {
  font-size: 22rpx;
  color: #999;
}

.stat-bold {
  font-weight: 600;
  color: #333;
}

.prog-sel {
  padding: 6rpx 16rpx;
  border: 2rpx solid #eee;
  border-radius: 8rpx;
  font-size: 22rpx;
  background: #fff;
}

.ll {
  margin: 0 28rpx 8rpx;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: 26rpx;
  border: 2rpx solid #eee;
  background: #fff;
}

.no {
  width: 44rpx;
  height: 44rpx;
  background: #fde8e1;
  color: #F05A22;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 20rpx;
  flex-shrink: 0;
}

.no.done {
  background: #F05A22;
  color: #fff;
}

.ln {
  font-weight: 500;
  display: block;
}

.ls {
  font-size: 22rpx;
  color: #999;
  display: block;
}

.mk {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
}

.mk.do {
  background: #F05A22;
  color: #fff;
}

.mk.und {
  background: #f5f5f5;
  color: #999;
  border: 2rpx solid #eee;
}

.cr {
  background: #fff;
  margin: 16rpx 32rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  border: 2rpx solid #eee;
  display: flex;
  gap: 24rpx;
}

.cr-title {
  font-size: 28rpx;
  font-weight: 600;
  display: block;
  margin-bottom: 4rpx;
}

.cr-meta {
  font-size: 24rpx;
  color: #999;
  display: block;
}

.cr-rating {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-top: 12rpx;
}

.empty-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 80rpx;
  text-align: center;
  margin: 32rpx;
}

.empty-icon {
  font-size: 80rpx;
  display: block;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
  margin-top: 24rpx;
  display: block;
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