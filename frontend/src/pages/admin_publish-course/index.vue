<template>
  <view class="admin-page">
    <view class="hd">
      <view class="hd-back" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
          stroke-linejoin="round">
          <path d="m15 18-6-6 6-6" />
        </svg>
      </view>
      <text class="hd-title">发布课程</text>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="fm">
        <view class="sg">
          <view class="sg-h">基本信息</view>
          <view class="sg-b">
            <view class="fg">
              <text class="fg-label">课程名称 <text class="rq">*</text></text>
              <input class="inp" v-model="form.title" placeholder="例：初一数学 · 暑期培优班" />
            </view>
            <view class="row2">
              <view class="fg">
                <text class="fg-label">学科/年级 <text class="rq">*</text></text>
                <picker :value="categoryIndex" :range="categoryOptions" @change="onCategoryChange">
                  <view class="picker-value">
                    {{ categoryOptions[categoryIndex] || '请选择' }}
                    <text class="picker-arrow">›</text>
                  </view>
                </picker>
              </view>
              <view class="fg">
                <text class="fg-label">授课教师 <text class="rq">*</text></text>
                <picker :value="teacherIndex" :range="teacherOptions" @change="onTeacherChange">
                  <view class="picker-value">
                    {{ teacherOptions[teacherIndex] || '请选择' }}
                    <text class="picker-arrow">›</text>
                  </view>
                </picker>
              </view>
            </view>
            <view class="row2">
              <view class="fg">
                <text class="fg-label">总课时</text>
                <input class="inp" type="number" v-model="form.totalHours" placeholder="16" />
              </view>
              <view class="fg">
                <text class="fg-label">课程定价 <text class="rq">*</text></text>
                <input class="inp" type="number" v-model="form.price" placeholder="1280" />
              </view>
            </view>
            <view class="fg">
              <text class="fg-label">购买赠送积分</text>
              <input class="inp" type="number" v-model="form.points" placeholder="100" />
              <text class="hint">学员购买后自动获赠的积分</text>
            </view>
          </view>
        </view>

        <view class="sg">
          <view class="sg-h">课程详情</view>
          <view class="sg-b">
            <view class="fg">
              <text class="fg-label">课程简介 <text class="rq">*</text></text>
              <textarea class="ta" v-model="form.description" placeholder="描述课程内容、适合学生群体、教学目标..." />
            </view>
            <view class="fg">
              <text class="fg-label">课程大纲 / 章节</text>
              <view class="chapter-list">
                <view class="ch-row" v-for="(_, index) in form.chapters" :key="index">
                  <view class="ch-idx">{{ index + 1 }}</view>
                  <input class="inp ch-inp" v-model="form.chapters[index]" placeholder="输入章节名称" />
                  <view class="ch-del" @click="removeChapter(index)" v-if="form.chapters.length > 1">✕</view>
                </view>
              </view>
              <view class="ch-add" @click="addChapter">+ 添加章节</view>
            </view>
          </view>
        </view>

        <view class="sg">
          <view class="sg-h">封面与上架</view>
          <view class="sg-b">
            <view class="fg">
              <text class="fg-label">课程封面</text>
              <view class="up-area" @click="chooseCover">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                  stroke-linejoin="round">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                  <circle cx="8.5" cy="8.5" r="1.5" />
                  <polyline points="21 15 16 10 5 21" />
                </svg>
                <text class="up-text">上传封面图片（建议 16:9）</text>
              </view>
            </view>
            <view class="fg">
              <text class="fg-label">上架设置</text>
              <view class="sw-g">
                <view class="sw" :class="{ on: form.publishImmediately }"
                  @click="form.publishImmediately = !form.publishImmediately"></view>
                <text class="sw-text">{{ form.publishImmediately ? '发布后立即上架' : '发布后需审核' }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="btn-p" @click="doPublish">确认发布课程</view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { createCourse } from '@/api/admin'

const categoryOptions = ['初一数学', '初二数学', '初三数学', '初一英语', '初二英语', '初三英语', '初二物理', '初三物理', '初三化学', '高一数学', '高一化学']
const teacherOptions = ['张伟', '李芳', '王强', '陈敏', '赵丽']

const categoryIndex = ref(0)
const teacherIndex = ref(0)

const form = reactive({
  title: '',
  category: '',
  teacherName: '',
  totalHours: '',
  price: '',
  points: '100',
  description: '',
  chapters: ['', '', ''],
  coverImage: '',
  publishImmediately: true
})

function onCategoryChange(e: any) {
  categoryIndex.value = e.detail.value
  form.category = categoryOptions[e.detail.value]
}

function onTeacherChange(e: any) {
  teacherIndex.value = e.detail.value
  form.teacherName = teacherOptions[e.detail.value]
}

function addChapter() {
  form.chapters.push('')
}

function removeChapter(index: number) {
  form.chapters.splice(index, 1)
}

function chooseCover() {
  uni.showToast({ title: '图片上传功能开发中', icon: 'none' })
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
    uni.showToast({ title: '请输入课程名称', icon: 'none' })
    return
  }
  if (!form.category) {
    uni.showToast({ title: '请选择学科/年级', icon: 'none' })
    return
  }
  if (!form.teacherName) {
    uni.showToast({ title: '请选择授课教师', icon: 'none' })
    return
  }
  if (!form.price) {
    uni.showToast({ title: '请输入课程定价', icon: 'none' })
    return
  }

  try {
    await createCourse({
      title: form.title,
      description: form.description,
      category: form.category,
      price: parseFloat(form.price),
      originalPrice: parseFloat(form.price),
      totalHours: parseInt(form.totalHours) || 16,
      coverImage: form.coverImage,
      teacherName: form.teacherName,
      chapters: form.chapters.filter(c => c.trim()),
      points: parseInt(form.points) || 0
    })

    uni.showToast({ title: `课程《${form.title}》发布成功！`, icon: 'success' })
    setTimeout(() => {
      uni.navigateBack({ delta: 1 })
    }, 1500)
  } catch {
    uni.showToast({ title: '发布失败', icon: 'none' })
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

.picker-value {
  width: 100%;
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

.ta {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #e8e8ed;
  border-radius: 10px;
  font-size: 14px;
  resize: vertical;
  min-height: 80px;
  line-height: 1.6;

  &::placeholder {
    color: #999;
  }
}

.hint {
  font-size: 11px;
  color: #6e6e73;
  margin-top: 2px;
}

.row2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ch-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ch-idx {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #fde8e1;
  color: #F05A22;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.ch-inp {
  flex: 1;
  padding: 9px 12px;
  border: 1px solid #e8e8ed;
  border-radius: 10px;
  font-size: 13px;
}

.ch-del {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #F5F5F7;
  border: 1px solid #e8e8ed;
  color: #6e6e73;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  flex-shrink: 0;
}

.ch-add {
  width: 100%;
  padding: 10px;
  text-align: center;
  border: 1.5px dashed #e8e8ed;
  border-radius: 10px;
  background: #fff;
  color: #F05A22;
  font-size: 13px;
  font-weight: 500;
  margin-top: 4px;
}

.up-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 20px;
  border: 1.5px dashed #e8e8ed;
  border-radius: 10px;

  svg {
    width: 24px;
    height: 24px;
    color: #999;
  }
}

.up-text {
  font-size: 12px;
  color: #999;
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