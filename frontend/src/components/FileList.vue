<template>
  <div class="file-list">
    <el-table :data="files" stripe style="width: 100%">
      <el-table-column prop="originalName" label="文件名" min-width="200" />
      <el-table-column label="大小" width="120" align="right">
        <template #default="{ row }">
          {{ formatSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="下载次数" width="100" align="center">
        <template #default="{ row }">
          {{ row.downloadCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="上传时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatTime(row.uploadTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleDownload(row.fileId)">
            下载
          </el-button>
          <el-button type="danger" link @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="files.length === 0" class="empty-state">
      <el-empty description="暂无文件" />
    </div>
  </div>
</template>

<script setup>
import { downloadFile, deleteFile } from '../api/fileApi'
import { ElMessage, ElMessageBox } from 'element-plus'

const emit = defineEmits(['delete-success'])

defineProps({
  files: {
    type: Array,
    required: true
  }
})

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB'
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function handleDownload(fileId) {
  downloadFile(fileId)
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${row.originalName}" 吗？`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteFile(row.fileId)
    ElMessage.success('删除成功')
    emit('delete-success')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.file-list {
  margin-top: 20px;
}

.empty-state {
  padding: 40px 0;
}
</style>
