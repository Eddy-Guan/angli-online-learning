<template>
  <view class="teacher-page">
    <view class="hd">
      <text class="hd-title">推送通知</text>
      <text class="hd-link" @click="goToHistory">历史记录 →</text>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="ps">
        <text class="ps-title">📨 发送课程通知</text>

        <view class="fr">
          <text class="fr-label">选择课程</text>
          <view class="cs-row">
            <picker mode="selector" :range="courseOptions" :range-key="courseOptionsKey" @change="onCourseChange">
              <view class="cs-sel">{{ selectedCourse?.title || '请选择课程' }}</view>
            </picker>
            <view class="cs-btn" @click="confirmCourse">确认</view>
          </view>
        </view>

        <view class="fr">
          <text class="fr-label">📖 今日总结</text>
          <textarea class="ta" v-model="formData.summary" placeholder="课堂内容、重点知识点、学生表现概况..."></textarea>
        </view>

        <view class="fr">
          <text class="fr-label">📝 今日作业</text>
          <textarea class="ta" v-model="formData.homework" placeholder="布置的作业内容、截止时间..."></textarea>
        </view>

        <view class="fr">
          <text class="fr-label">⭐ 表现优秀学生</text>
          <input class="gs-inp" v-model="formData.goodStudents" placeholder="输入学生姓名，用逗号或空格分隔" />
          <text class="gs-hint">课后表现突出的学生，家长会收到表扬通知</text>
        </view>

        <view class="fr">
          <text class="fr-label">推送对象</text>
          <view class="pt-row">
            <text class="pt-info">{{ pushTarget }}</text>
            <view class="pt-btn" @click="showParentList">详细</view>
          </view>
        </view>

        <view class="btn" @click="doPush">📤 发送通知</view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>

    <view class="overlay" :class="{ show: showOverlay }">
      <view class="ol-h">
        <view class="close" @click="closeParentList">✕</view>
        <input type="text" placeholder="搜索家长姓名、学生名称…" v-model="searchKeyword" />
      </view>
      <view class="pl">
        <view class="all-cb">
          <checkbox :checked="selectAll" @change="toggleAllParents" />
          <text class="all-cb-label">全部家长 ← 全选</text>
          <text class="all-cb-count">{{ parentList.length }}人</text>
        </view>
        <view class="pi" v-for="parent in parentList" :key="parent.id" @click="toggleParent(parent.id)">
          <checkbox :checked="selectedParents.includes(parent.id)" />
          <view class="pa" :class="getColorClass(parent.id)">{{ parent.name.charAt(0) }}</view>
          <view class="pb">
            <text class="pn">{{ parent.name }}</text>
            <text class="pp">{{ parent.phone }}</text>
          </view>
        </view>
        <view class="pl-add" @click="addParent">+ 添加家长</view>
      </view>
      <view class="ol-confirm">
        <view class="btn" @click="confirmParentList">确认推送对象</view>
      </view>
    </view>

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
      <view class="ni on">
        <text class="ni-icon">📨</text>
        <text class="ni-text">推送</text>
      </view>
    </nav>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { pushNotification, getTeacherCourses, getCourseParents, type PushNotificationRequest, type TeacherCourse, type CourseParent } from '@/api/teacher'

const userStore = useUserStore()

const courses = ref<TeacherCourse[]>([])
const courseOptions = computed(() => courses.value)
const courseOptionsKey = 'title'
const selectedCourse = ref<TeacherCourse | null>(null)
const showOverlay = ref(false)
const searchKeyword = ref('')
const selectAll = ref(true)

const formData = reactive({ summary: '', homework: '', goodStudents: '' })
const parentList = ref<CourseParent[]>([])
const selectedParents = ref<number[]>([])

const pushTarget = computed(() => {
  if (!selectedCourse.value) return '请先选择课程'
  if (selectAll.value) return `全部家长（${parentList.value.length}人）— ${selectedCourse.value.title}`
  return `已选择 ${selectedParents.value.length} 位家长`
})

onMounted(() => { userStore.loadFromStorage(); loadCourses() })

async function loadCourses() {
  if (!userStore.userInfo?.userId) return
  try { courses.value = await getTeacherCourses(userStore.userInfo.userId, 'PUBLISHED') }
  catch (err) { uni.showToast({ title: '加载课程失败', icon: 'none' }) }
}

async function loadParents() {
  if (!selectedCourse.value) return
  try {
    parentList.value = await getCourseParents(selectedCourse.value.id)
    selectedParents.value = parentList.value.map(p => p.id)
    selectAll.value = true
  } catch (err) { uni.showToast({ title: '加载家长列表失败', icon: 'none' }) }
}

function onCourseChange(e: any) { selectedCourse.value = courseOptions.value[e.detail.value] }

function confirmCourse() {
  if (!selectedCourse.value) { uni.showToast({ title: '请先选择课程', icon: 'none' }); return }
  loadParents()
  uni.showToast({ title: '课程已确认', icon: 'success' })
}

function showParentList() {
  if (!selectedCourse.value) { uni.showToast({ title: '请先选择课程', icon: 'none' }); return }
  showOverlay.value = true
}

function closeParentList() { showOverlay.value = false }

function toggleAllParents(e: any) {
  selectAll.value = e.detail.value
  selectedParents.value = selectAll.value ? parentList.value.map(p => p.id) : []
}

function toggleParent(id: number) {
  const index = selectedParents.value.indexOf(id)
  if (index > -1) selectedParents.value.splice(index, 1)
  else selectedParents.value.push(id)
  selectAll.value = selectedParents.value.length === parentList.value.length
}

function getColorClass(id: number) { return ['c1', 'c2', 'c3', 'c4', 'c5'][id % 5] }

function addParent() { uni.showToast({ title: '添加家长功能开发中', icon: 'none' }) }

function confirmParentList() {
  closeParentList()
  uni.showToast({ title: `已更新推送对象（${selectedParents.value.length}人）`, icon: 'none' })
}

async function doPush() {
  if (!selectedCourse.value) { uni.showToast({ title: '请选择课程', icon: 'none' }); return }
  if (!formData.summary && !formData.homework) { uni.showToast({ title: '请填写总结或作业内容', icon: 'none' }); return }

  uni.showLoading({ title: '发送中...' })
  try {
    const req: PushNotificationRequest = {
      courseId: selectedCourse.value.id,
      summary: formData.summary,
      homework: formData.homework,
      goodStudents: formData.goodStudents
    }
    await pushNotification(userStore.userInfo!.userId, req)
    uni.hideLoading()
    uni.showToast({ title: '发送成功', icon: 'success' })
    formData.summary = ''; formData.homework = ''; formData.goodStudents = ''
  } catch (err) { uni.hideLoading(); uni.showToast({ title: '发送失败', icon: 'none' }) }
}

function goToHome() { uni.navigateTo({ url: '/pages/teacher_home/index' }) }
function goToApply() { uni.navigateTo({ url: '/pages/teacher_apply/index' }) }
function goToCourses() { uni.navigateTo({ url: '/pages/teacher_courses/index' }) }
function goToHistory() { uni.navigateTo({ url: '/pages/teacher_push_history/index' }) }
</script>

<style lang="scss" scoped>
.teacher-page { min-height: 100vh; background: #F5F5F5; }
.hd { display: flex; align-items: center; justify-content: space-between; padding: 60rpx 32rpx 24rpx; background: #fff; font-size: 36rpx; font-weight: 700; }
.hd-link { font-size: 26rpx; color: #F05A22; }
.content { height: calc(100vh - 200rpx); }
.ps { padding: 32rpx; }
.ps-title { font-size: 30rpx; font-weight: 600; display: block; margin-bottom: 24rpx; }
.fr { margin-bottom: 20rpx; }
.fr-label { display: block; font-size: 24rpx; color: #999; margin-bottom: 8rpx; }
.cs-row { display: flex; gap: 16rpx; align-items: center; }
.cs-sel { flex: 1; padding: 20rpx; border: 2rpx solid #eee; border-radius: 20rpx; font-size: 26rpx; background: #fff; }
.cs-btn { padding: 20rpx 32rpx; background: #fde8e1; color: #F05A22; border: 2rpx solid #F05A22; border-radius: 20rpx; font-size: 26rpx; font-weight: 600; white-space: nowrap; }
.ta { width: 100%; padding: 20rpx; border: 2rpx solid #eee; border-radius: 20rpx; font-size: 26rpx; min-height: 160rpx; }
.gs-inp { width: 100%; padding: 22rpx 28rpx; border: 2rpx solid #eee; border-radius: 20rpx; font-size: 26rpx; }
.gs-hint { font-size: 22rpx; color: #999; margin-top: 8rpx; display: block; }
.pt-row { display: flex; align-items: center; justify-content: space-between; background: #fafafa; border: 2rpx solid #eee; border-radius: 20rpx; padding: 20rpx 28rpx; }
.pt-info { font-size: 26rpx; color: #333; }
.pt-btn { padding: 12rpx 28rpx; background: #fff; color: #F05A22; border: 2rpx solid #F05A22; border-radius: 12rpx; font-size: 24rpx; font-weight: 500; }
.btn { display: block; width: 100%; padding: 24rpx; background: #F05A22; color: #fff; border-radius: 20rpx; font-size: 28rpx; text-align: center; margin-top: 24rpx; }
.bottom-space { height: 160rpx; }
.overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: #fff; z-index: 200; display: none; overflow-y: auto; }
.overlay.show { display: block; }
.ol-h { display: flex; align-items: center; gap: 20rpx; padding: 20rpx 32rpx; border-bottom: 2rpx solid #eee; position: sticky; top: 0; background: #fff; }
.close { font-size: 40rpx; color: #999; padding: 8rpx; line-height: 1; }
.ol-h input { flex: 1; padding: 16rpx 24rpx; border: 2rpx solid #eee; border-radius: 16rpx; font-size: 26rpx; }
.pl { padding: 16rpx 32rpx; display: flex; flex-direction: column; gap: 12rpx; }
.all-cb { display: flex; align-items: center; gap: 16rpx; padding: 20rpx 0 16rpx; border-bottom: 2rpx solid #eee; margin-bottom: 8rpx; }
.all-cb-label { font-size: 26rpx; font-weight: 600; color: #333; }
.all-cb-count { font-size: 22rpx; color: #999; margin-left: auto; }
.pi { display: flex; align-items: center; gap: 20rpx; padding: 20rpx 24rpx; border-radius: 20rpx; }
.pi:active { background: #f5f5f5; }
.pa { width: 72rpx; height: 72rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 26rpx; font-weight: 600; color: #fff; flex-shrink: 0; }
.pa.c1 { background: #F05A22; }
.pa.c2 { background: #2196f3; }
.pa.c3 { background: #4caf50; }
.pa.c4 { background: #9c27b0; }
.pa.c5 { background: #ff9800; }
.pb { flex: 1; }
.pn { font-size: 26rpx; font-weight: 500; display: block; }
.pp { font-size: 22rpx; color: #999; display: block; }
.pl-add { margin-top: 16rpx; padding: 24rpx; text-align: center; border: 3rpx dashed #eee; border-radius: 20rpx; color: #F05A22; font-size: 26rpx; font-weight: 500; }
.ol-confirm { padding: 24rpx 32rpx; border-top: 2rpx solid #eee; position: sticky; bottom: 0; background: #fff; }
.bn { position: fixed; bottom: 0; left: 0; right: 0; display: flex; justify-content: space-around; padding: 12rpx 0 32rpx; background: #fff; border-top: 2rpx solid #eee; z-index: 10; }
.ni { display: flex; flex-direction: column; align-items: center; gap: 4rpx; font-size: 20rpx; color: #999; }
.ni.on { color: #F05A22; }
.ni-icon { font-size: 40rpx; }
.ni-text { font-size: 20rpx; }
</style>