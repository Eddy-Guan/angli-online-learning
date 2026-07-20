<template>
  <view class="teacher-page">
    <view class="hd">
      <text class="hd-title">课程申请</text>
      <text class="hd-link" @click="goToRecords">申请记录 →</text>
    </view>

    <scroll-view class="content" scroll-y>

      <view class="success-modal" v-if="showSuccessModal" @click="closeSuccessModal">
        <view class="success-content" @click.stop>
          <view class="success-icon">✅</view>
          <text class="success-title">提交成功</text>
          <text class="success-desc">您的课程申请已提交，等待管理员审核</text>
          <view class="success-actions">
            <view class="success-btn-secondary" @click="closeSuccessModal">继续申请</view>
            <view class="success-btn-primary" @click="goToRecords">查看记录</view>
          </view>
        </view>
      </view>
      <view class="ap-steps">
        <view class="ap-step" :class="{ on: currentStep >= 1 }">
          <view class="ap-step-num">1</view>
          <text class="ap-step-lb">填写信息</text>
        </view>
        <view class="ap-step-line"></view>
        <view class="ap-step" :class="{ on: currentStep >= 2 }">
          <view class="ap-step-num">2</view>
          <text class="ap-step-lb">提交审核</text>
        </view>
        <view class="ap-step-line"></view>
        <view class="ap-step" :class="{ on: currentStep >= 3 }">
          <view class="ap-step-num">3</view>
          <text class="ap-step-lb">等待通过</text>
        </view>
      </view>

      <view class="ap-hero">
        <text class="ap-title">📝 申请新课程</text>
        <text class="ap-desc">填写课程信息，提交至管理员审核。审核通过后将自动上架发布。</text>
      </view>

      <view class="ap-sec">
        <view class="ap-sec-h">📋 课程信息</view>
        <view class="ap-sec-b">
          <view class="ap-field">
            <text class="ap-field-label">课程名称 <text class="ap-required">*</text></text>
            <input class="ap-inp" v-model="formData.title" placeholder="例：初一数学 · 思维训练班" />
          </view>
          <view class="ap-row">
            <view class="ap-field ap-field-half">
              <text class="ap-field-label">学科/年级</text>
              <picker mode="selector" :range="gradeOptions" @change="onGradeChange">
                <view class="ap-sel">
                  {{ selectedGrade || '请选择学科/年级' }}
                </view>
              </picker>
            </view>
            <view class="ap-field ap-field-half">
              <text class="ap-field-label">授课教师</text>
              <input class="ap-inp ap-inp-readonly" :value="teacherName" readonly />
            </view>
          </view>
          <view class="ap-field">
            <text class="ap-field-label">总课时数</text>
            <input class="ap-inp ap-inp-small" type="number" v-model="formData.totalHours" value="16" min="1"
              max="99" />
          </view>
          <view class="ap-field">
            <text class="ap-field-label">课程简介</text>
            <textarea class="ap-ta" v-model="formData.description" placeholder="描述课程目标、适合学生群体、课程特色..." />
          </view>
        </view>
      </view>

      <view class="ap-sec">
        <view class="ap-sec-h">📖 课程章节 <text class="ap-badge">可多添加</text></view>
        <view class="ap-sec-b">
          <view class="ap-ch-list">
            <view class="ap-ch-row" v-for="(chapter, index) in chapters" :key="index">
              <view class="ap-ch-num">{{ index + 1 }}</view>
              <input class="ap-ch-inp" v-model="chapter.title" placeholder="章节名称" />
              <input class="ap-ch-desc" v-model="chapter.description" placeholder="简介" />
              <view class="ap-ch-del" @click="removeChapter(index)" v-if="chapters.length > 1">✕</view>
            </view>
          </view>
          <view class="ap-add-ch" @click="addChapter">➕ 添加章节</view>
        </view>
      </view>

      <view class="ap-submit" @click="submitApply">📤 提交审核申请</view>

      <view class="bottom-space"></view>
    </scroll-view>

    <nav class="bn">
      <view class="ni" @click="goToHome">
        <text class="ni-icon">👨‍🏫</text>
        <text class="ni-text">主页</text>
      </view>
      <view class="ni on">
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
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { applyCourse, getApplyRecords, type CourseApplyRequest } from '@/api/teacher'

const userStore = useUserStore()

const currentStep = ref(1)
const teacherName = ref('张伟老师')
const selectedGrade = ref('')

const gradeOptions = [
  '初一数学', '初二数学', '初三数学',
  '初一英语', '初二英语', '初三英语',
  '初一语文', '初二语文', '初三语文',
  '初二物理', '初三物理', '初三化学'
]

const formData = reactive({
  title: '',
  subject: '',
  grade: '',
  totalHours: 16,
  description: ''
})

const chapters = ref([
  { title: '', description: '' }
])

const showSuccessModal = ref(false)

onMounted(() => {
  userStore.loadFromStorage()
})

function onGradeChange(e: any) {
  const val = gradeOptions[e.detail.value]
  selectedGrade.value = val
  const parts = val.split(' ')
  if (parts.length >= 2) {
    formData.grade = parts[0]
    formData.subject = parts[1]
  }
}

function addChapter() {
  if (chapters.value.length >= 20) {
    uni.showToast({ title: '最多添加20个章节', icon: 'none' })
    return
  }
  chapters.value.push({ title: '', description: '' })
}

function removeChapter(index: number) {
  chapters.value.splice(index, 1)
}

async function submitApply() {
  if (!formData.title) {
    uni.showToast({ title: '请输入课程名称', icon: 'none' })
    return
  }
  if (!selectedGrade.value) {
    uni.showToast({ title: '请选择学科/年级', icon: 'none' })
    return
  }
  if (!formData.totalHours || formData.totalHours < 1) {
    uni.showToast({ title: '请输入有效课时数', icon: 'none' })
    return
  }

  const validChapters = chapters.value.filter(c => c.title.trim())
  if (validChapters.length === 0) {
    uni.showToast({ title: '请至少添加一个章节', icon: 'none' })
    return
  }

  uni.showLoading({ title: '提交中...' })

  try {
    const req: CourseApplyRequest = {
      title: formData.title,
      subject: formData.subject,
      grade: formData.grade,
      totalHours: formData.totalHours,
      description: formData.description,
      chapters: validChapters.map(c => ({ title: c.title, description: c.description }))
    }

    await applyCourse(userStore.userInfo!.userId, req)

    uni.hideLoading()

    formData.title = ''
    formData.description = ''
    formData.totalHours = 16
    chapters.value = [{ title: '', description: '' }]
    selectedGrade.value = ''
    formData.subject = ''
    formData.grade = ''

    showSuccessModal.value = true
  } catch (err: any) {
    uni.hideLoading()
    const errorMsg = err?.response?.data?.message || err?.message || '提交失败，请稍后重试'
    uni.showToast({ title: errorMsg, icon: 'none', duration: 2000 })
  }
}

function closeSuccessModal() {
  showSuccessModal.value = false
}

function goToRecords() {
  uni.navigateTo({ url: '/pages/teacher_apply_records/index' })
}

function goToHome() {
  uni.navigateTo({ url: '/pages/teacher_home/index' })
}

function goToCourses() {
  uni.navigateTo({ url: '/pages/teacher_courses/index' })
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

.ap-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 32rpx 16rpx;
}

.ap-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.ap-step-num {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #eee;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 600;
}

.ap-step.on .ap-step-num {
  background: #F05A22;
  color: #fff;
}

.ap-step-lb {
  font-size: 20rpx;
  color: #999;
}

.ap-step.on .ap-step-lb {
  color: #F05A22;
  font-weight: 500;
}

.ap-step-line {
  width: 64rpx;
  height: 4rpx;
  background: #eee;
  margin: 0 8rpx;
  position: relative;
  top: -24rpx;
}

.ap-hero {
  padding: 16rpx 32rpx 8rpx;
}

.ap-title {
  font-size: 40rpx;
  font-weight: 700;
  display: block;
}

.ap-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.ap-sec {
  background: #fff;
  margin: 12rpx 24rpx;
  border-radius: 20rpx;
  border: 2rpx solid #eee;
  overflow: hidden;
}

.ap-sec-h {
  padding: 16rpx 24rpx 0;
  font-size: 26rpx;
  font-weight: 600;
}

.ap-badge {
  display: inline-block;
  padding: 4rpx 20rpx;
  border-radius: 40rpx;
  font-size: 20rpx;
  font-weight: 500;
  background: #fde8e1;
  color: #F05A22;
  margin-left: auto;
}

.ap-sec-b {
  padding: 4rpx 24rpx 20rpx;
}

.ap-field {
  margin-bottom: 16rpx;
}

.ap-field-label {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-bottom: 8rpx;
  font-weight: 500;
}

.ap-required {
  color: #F05A22;
}

.ap-inp {
  width: 100%;
  padding: 20rpx 24rpx;
  border: 2rpx solid #eee;
  border-radius: 20rpx;
  font-size: 26rpx;
}

.ap-inp-readonly {
  background: #f5f5f5;
  color: #999;
}

.ap-inp-small {
  max-width: 240rpx;
}

.ap-row {
  display: flex;
  gap: 20rpx;
}

.ap-field-half {
  flex: 1;
}

.ap-sel {
  width: 100%;
  padding: 20rpx 24rpx;
  border: 2rpx solid #eee;
  border-radius: 20rpx;
  font-size: 26rpx;
  background: #fff;
  position: relative;

  &::after {
    content: '▼';
    position: absolute;
    right: 24rpx;
    top: 50%;
    transform: translateY(-50%);
    font-size: 20rpx;
    color: #999;
  }
}

.ap-ta {
  width: 100%;
  padding: 20rpx 24rpx;
  border: 2rpx solid #eee;
  border-radius: 20rpx;
  font-size: 26rpx;
  min-height: 140rpx;
}

.ap-ch-list {
  padding: 0;
}

.ap-ch-row {
  display: flex;
  gap: 12rpx;
  padding: 16rpx 24rpx;
  margin: 0;
  align-items: center;
  border-bottom: 2rpx solid #f5f5f5;
}

.ap-ch-row:last-child {
  border-bottom: none;
}

.ap-ch-num {
  width: 44rpx;
  height: 44rpx;
  background: #fde8e1;
  color: #F05A22;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 22rpx;
  flex-shrink: 0;
}

.ap-ch-inp {
  flex: 1;
  padding: 16rpx 20rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  font-size: 26rpx;
}

.ap-ch-desc {
  flex: 1;
  padding: 16rpx 20rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  font-size: 24rpx;
}

.ap-ch-del {
  width: 52rpx;
  height: 52rpx;
  background: #ff4d4d;
  color: #fff;
  border-radius: 50%;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ap-add-ch {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 28rpx;
  border: 2rpx dashed #eee;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #F05A22;
  margin-top: 16rpx;
}

.ap-submit {
  display: block;
  width: calc(100% - 48rpx);
  padding: 26rpx;
  background: linear-gradient(135deg, #F05A22, #e04d16);
  color: #fff;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 600;
  text-align: center;
  margin: 16rpx 24rpx;
}

.ap-rec-card {
  background: #fff;
  border: 2rpx solid #eee;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
}

.ap-rec-h {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
}

.ap-rec-title {
  font-size: 28rpx;
  font-weight: 600;
  flex: 1;
}

.ap-rec-st {
  display: inline-block;
  padding: 4rpx 20rpx;
  border-radius: 40rpx;
  font-size: 20rpx;
  font-weight: 500;
  margin-left: 16rpx;
}

.ap-rec-st.pending {
  background: #fff7e6;
  color: #fa8c16;
  border: 2rpx solid #ffd591;
}

.ap-rec-st.approved {
  background: #f6ffed;
  color: #52c41a;
  border: 2rpx solid #b7eb8f;
}

.ap-rec-st.rejected {
  background: #fff2f0;
  color: #ff4d4d;
  border: 2rpx solid #ffccc7;
}

.ap-rec-meta {
  font-size: 22rpx;
  color: #999;
  display: block;
}

.ap-rec-ch {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 8rpx;
}

.ap-ch-tag {
  padding: 4rpx 16rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  font-size: 20rpx;
  color: #999;
}

.ap-empty {
  text-align: center;
  padding: 64rpx 32rpx;
}

.ap-empty-ico {
  font-size: 80rpx;
  display: block;
  margin-bottom: 24rpx;
}

.ap-empty-text {
  font-size: 24rpx;
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

.success-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.success-content {
  width: 600rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 60rpx 40rpx;
  text-align: center;
}

.success-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.success-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.success-desc {
  font-size: 28rpx;
  color: #999;
  display: block;
  margin-bottom: 40rpx;
  line-height: 1.6;
}

.success-actions {
  display: flex;
  gap: 24rpx;
}

.success-btn-secondary {
  flex: 1;
  padding: 24rpx;
  background: #f5f5f5;
  color: #666;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.success-btn-primary {
  flex: 1;
  padding: 24rpx;
  background: linear-gradient(135deg, #F05A22, #e04d16);
  color: #fff;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 500;
}
</style>