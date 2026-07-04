<template>
  <div class="file-uploader">
    <el-upload
      ref="uploadRef"
      drag
      :auto-upload="false"
      :on-change="handleFileChange"
      :show-file-list="false"
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        将文件拖到此处，或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持任意文件，单文件最大 100MB
        </div>
      </template>
    </el-upload>

    <div class="expire-selector">
      <span class="expire-label">过期时间：</span>
      <el-radio-group v-model="expireHours">
        <el-radio :value="24">1天</el-radio>
        <el-radio :value="168">7天</el-radio>
        <el-radio :value="720">30天</el-radio>
        <el-radio :value="0">永不过期</el-radio>
      </el-radio-group>
    </div>

    <div class="password-input">
      <span class="expire-label">密码保护：</span>
      <el-input
        v-model="password"
        type="password"
        placeholder="留空则不设密码"
        show-password
        style="width: 200px"
      />
    </div>

    <div v-if="uploading" class="upload-progress">
      <el-progress
        :percentage="progress"
        :stroke-width="10"
        status="primary"
      />
      <span class="progress-text">上传中... {{ progress }}%</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '../api/fileApi'

const emit = defineEmits(['upload-success'])

const uploadRef = ref(null)
const uploading = ref(false)
const progress = ref(0)
const expireHours = ref(24)
const password = ref('')

async function handleFileChange(file) {
  if (!file || !file.raw) return

  uploading.value = true
  progress.value = 0

  try {
    await uploadFile(file.raw, expireHours.value || null, password.value || null, (event) => {
      if (event.total) {
        progress.value = Math.round((event.loaded / event.total) * 100)
      }
    })

    ElMessage.success('上传成功')
    emit('upload-success')
  } catch (error) {
    const message = error.response?.data?.message || '上传失败'
    ElMessage.error(message)
  } finally {
    uploading.value = false
    progress.value = 0
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
  }
}
</script>

<style scoped>
.file-uploader {
  margin-bottom: 20px;
}

.expire-selector {
  margin-top: 16px;
  display: flex;
  align-items: center;
}

.password-input {
  margin-top: 12px;
  display: flex;
  align-items: center;
}

.expire-label {
  font-size: 14px;
  color: #606266;
  margin-right: 8px;
}

.upload-progress {
  margin-top: 16px;
}

.progress-text {
  display: block;
  margin-top: 8px;
  color: #606266;
  font-size: 14px;
}
</style>
