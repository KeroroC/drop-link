<template>
  <div class="app-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h1>Drop-Link 文件共享</h1>
          <p class="subtitle">上传文件，分享链接，随时下载</p>
        </div>
      </template>

      <FileUploader @upload-success="loadFiles" />
      <FileList :files="files" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchFileList } from './api/fileApi'
import FileUploader from './components/FileUploader.vue'
import FileList from './components/FileList.vue'

const files = ref([])

async function loadFiles() {
  try {
    const response = await fetchFileList()
    files.value = response.data.data || []
  } catch (error) {
    ElMessage.error('获取文件列表失败')
    console.error(error)
  }
}

onMounted(() => {
  loadFiles()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  background-color: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  padding: 40px 20px;
}

.main-card {
  width: 100%;
  max-width: 900px;
}

.card-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.card-header .subtitle {
  font-size: 14px;
  color: #909399;
}
</style>
