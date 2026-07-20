<template>
  <view class="teacher-page">
    <view class="hd">
      <view class="hd-back" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="hd-title">教师设置</text>
      <view class="hd-placeholder"></view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="av-sec">
        <view class="aw">
          <view class="av" @click="selectAvatar">
            <text class="av-emoji">{{ currentAvatar }}</text>
          </view>
          <view class="av-cam">
            <text class="cam-icon">📷</text>
          </view>
        </view>
      </view>

      <view class="def-av">
        <view class="da" :class="{ on: currentAvatar === avatar }" v-for="avatar in avatarOptions" :key="avatar"
          @click="selectAvatar(avatar)">
          {{ avatar }}
        </view>
      </view>

      <view class="preview">
        <view class="pa">{{ currentAvatar }}</view>
        <view class="pb">
          <text class="pn">{{ teacherName }}老师</text>
          <view class="pt">
            <text class="pt-tag" v-for="(tag, idx) in tags" :key="idx">{{ tag }}</text>
          </view>
        </view>
      </view>

      <view class="fm">
        <view class="sg">
          <view class="sg-h">基本信息</view>
          <view class="sg-b">
            <view class="fg">
              <text class="fg-label">真实姓名 <text class="rq">*</text></text>
              <input class="inp" v-model="formData.realName" placeholder="请输入姓名" />
            </view>
            <view class="fg">
              <text class="fg-label">出生日期</text>
              <picker mode="date" @change="onDateChange">
                <view class="inp">
                  {{ formData.birthDate || '请选择出生日期' }}
                </view>
              </picker>
            </view>
            <view class="fg">
              <text class="fg-label">手机号 <text class="rq">*</text></text>
              <input class="inp inp-readonly" :value="formData.phone" readonly />
              <text class="hint-text">如需换绑手机号，请联系管理员</text>
            </view>
          </view>
        </view>

        <view class="sg">
          <view class="sg-h">个人标签 <text class="sg-hint">（展示在教师主页，最多3个，每个限4字）</text></view>
          <view class="sg-b">
            <view class="tags">
              <view class="tg" v-for="(tag, idx) in tags" :key="idx">
                <text class="tg-text">{{ tag }}</text>
                <view class="tg-del" @click="removeTag(idx)">✕</view>
              </view>
              <view class="tg-add" v-if="tags.length < 3" @click="showTagInput">+ 添加标签</view>
            </view>
            <view class="tag-input-row" v-if="showTagInputFlag">
              <input class="tag-input" v-model="newTag" maxlength="4" placeholder="如：奥数教练" />
              <view class="add-btn" @click="addTag">添加</view>
              <view class="add-btn cancel-btn" @click="hideTagInput">取消</view>
            </view>
            <text class="tg-hint">示例：奥数教练、优秀教师、985硕士、10年教龄、竞赛辅导…</text>
          </view>
        </view>

        <view class="btn-p" @click="doSave">保存修改</view>
      </view>

      <view class="logout-section">
        <view class="logout-btn" @click="doLogout">退出登录</view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTeacherSettings, updateTeacherSettings, type TeacherSettings } from '@/api/teacher'

const userStore = useUserStore()

const avatarOptions = ['👨‍🏫', '👩‍🏫', '🧑‍🏫', '👨‍💼', '👩‍💼', '🧑‍💼']
const currentAvatar = ref('👨‍🏫')
const showTagInputFlag = ref(false)
const newTag = ref('')

const teacherName = ref('张伟')

const tags = ref(['数学', '金牌讲师', '教龄8年'])

const formData = reactive({
  realName: '张伟',
  birthDate: '1990-05-15',
  phone: '138****8888'
})

onMounted(() => {
  userStore.loadFromStorage()
  loadSettings()
})

async function loadSettings() {
  if (!userStore.userInfo) return

  try {
    const settings = await getTeacherSettings(userStore.userInfo.userId)
    formData.realName = settings.realName
    formData.phone = settings.phone
    if (settings.tags && settings.tags.length > 0) {
      tags.value = settings.tags
    }
    if (settings.avatar) {
      currentAvatar.value = settings.avatar
    }
    teacherName.value = settings.realName
  } catch (err) {
    console.error('Load settings failed:', err)
  }
}

function selectAvatar(avatar?: string) {
  if (avatar) {
    currentAvatar.value = avatar
  }
}

function onDateChange(e: any) {
  formData.birthDate = e.detail.value
}

function showTagInput() {
  showTagInputFlag.value = true
  newTag.value = ''
}

function hideTagInput() {
  showTagInputFlag.value = false
}

function addTag() {
  const val = newTag.value.trim()
  if (!val) {
    uni.showToast({ title: '请输入标签内容', icon: 'none' })
    return
  }
  if (val.length > 4) {
    uni.showToast({ title: '每个标签不超过4个字', icon: 'none' })
    return
  }
  if (tags.value.length >= 3) {
    uni.showToast({ title: '最多只能添加3个标签', icon: 'none' })
    return
  }
  tags.value.push(val)
  hideTagInput()
  uni.showToast({ title: '已添加标签', icon: 'none' })
}

function removeTag(idx: number) {
  tags.value.splice(idx, 1)
}

async function doSave() {
  if (!formData.realName) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }

  uni.showLoading({ title: '保存中...' })

  try {
    const data: Partial<TeacherSettings> = {
      realName: formData.realName,
      avatar: currentAvatar.value,
      tags: tags.value
    }

    await updateTeacherSettings(userStore.userInfo!.userId, data)

    uni.hideLoading()
    uni.showToast({ title: '保存成功', icon: 'success' })
    teacherName.value = formData.realName
  } catch (err) {
    uni.hideLoading()
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

function goBack() {
  uni.navigateBack()
}

function doLogout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.teacher-page {
  min-height: 100vh;
  background: #F5F5F7;
}

.hd {
  display: flex;
  align-items: center;
  padding: 60rpx 32rpx 24rpx;
  background: #fff;
}

.hd-back {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 0;
}

.back-icon {
  font-size: 56rpx;
  color: #333;
}

.hd-title {
  flex: 1;
  text-align: center;
  font-size: 36rpx;
  font-weight: 700;
}

.hd-placeholder {
  width: 80rpx;
}

.content {
  height: calc(100vh - 120rpx);
}

.av-sec {
  padding: 48rpx 32rpx 24rpx;
  text-align: center;
  background: #fff;
}

.aw {
  position: relative;
  display: inline-block;
}

.av {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #fde8e1;
}

.av-emoji {
  font-size: 64rpx;
}

.av-cam {
  position: absolute;
  bottom: 0;
  right: -4rpx;
  width: 56rpx;
  height: 56rpx;
  background: #F05A22;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid #fff;
}

.cam-icon {
  font-size: 28rpx;
}

.def-av {
  display: flex;
  gap: 20rpx;
  justify-content: center;
  padding: 16rpx 32rpx 28rpx;
  background: #fff;
}

.da {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  cursor: pointer;
  border: 5rpx solid transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  flex-shrink: 0;
}

.da.on {
  border-color: #F05A22;
  box-shadow: 0 0 0 6rpx #fde8e1;
}

.da.d1 {
  background: #fde8e1;
}

.da.d2 {
  background: #e3f2fd;
}

.da.d3 {
  background: #fff3e0;
}

.da.d4 {
  background: #e8f5e9;
}

.da.d5 {
  background: #f3e5f5;
}

.da.d6 {
  background: #fce4ec;
}

.preview {
  padding: 24rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  border: 2rpx solid #eee;
  margin: 16rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.pa {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fde8e1;
  font-size: 40rpx;
  flex-shrink: 0;
}

.pb {
  flex: 1;
}

.pn {
  font-size: 28rpx;
  font-weight: 600;
  display: block;
}

.pt {
  margin-top: 4rpx;
}

.pt-tag {
  display: inline-block;
  padding: 2rpx 12rpx;
  background: #fde8e1;
  border-radius: 8rpx;
  font-size: 22rpx;
  color: #F05A22;
  margin-right: 8rpx;
}

.fm {
  padding: 32rpx;
}

.sg {
  background: #fff;
  border-radius: 24rpx;
  border: 2rpx solid #eee;
  overflow: hidden;
  margin-bottom: 24rpx;
}

.sg-h {
  padding: 28rpx 32rpx 20rpx;
  font-size: 26rpx;
  font-weight: 600;
}

.sg-hint {
  font-size: 22rpx;
  color: #999;
  font-weight: 400;
}

.sg-b {
  padding: 20rpx 32rpx 28rpx;
}

.fg {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  margin-bottom: 24rpx;
}

.fg:last-child {
  margin-bottom: 0;
}

.fg-label {
  font-size: 24rpx;
  font-weight: 600;
}

.rq {
  color: #dc2626;
  margin-left: 4rpx;
}

.inp {
  width: 100%;
  padding: 22rpx 28rpx;
  border: 2rpx solid #eee;
  border-radius: 20rpx;
  font-size: 28rpx;
}

.inp-readonly {
  background: #f5f5f5;
  color: #999;
}

.hint-text {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.tags {
  margin-top: 8rpx;
}

.tg {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 24rpx;
  background: #fde8e1;
  color: #F05A22;
  border-radius: 12rpx;
  font-size: 26rpx;
  font-weight: 500;
  margin: 0 12rpx 12rpx 0;
}

.tg-del {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: #F05A22;
  color: #fff;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tg-add {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  background: #fff;
  color: #999;
  border: 3rpx dashed #eee;
  border-radius: 12rpx;
  font-size: 26rpx;
}

.tag-input-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.tag-input {
  flex: 1;
  padding: 18rpx 24rpx;
  border: 2rpx solid #eee;
  border-radius: 20rpx;
  font-size: 26rpx;
  max-width: 240rpx;
  text-align: center;
}

.add-btn {
  padding: 18rpx 28rpx;
  background: #fde8e1;
  color: #F05A22;
  border: 2rpx solid #F05A22;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.cancel-btn {
  background: #fff;
  color: #999;
  border-color: #eee;
}

.tg-hint {
  font-size: 22rpx;
  color: #999;
  margin-top: 12rpx;
  display: block;
}

.btn-p {
  width: 100%;
  padding: 28rpx;
  background: linear-gradient(135deg, #F05A22, #ff7a45);
  color: #fff;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  text-align: center;
}

.logout-section {
  padding: 0 32rpx;
  margin-top: 16rpx;
}

.logout-btn {
  width: 100%;
  padding: 28rpx;
  background: #fff;
  color: #dc2626;
  border: 2rpx solid #dc2626;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  text-align: center;
}

.bottom-space {
  height: 60rpx;
}
</style>