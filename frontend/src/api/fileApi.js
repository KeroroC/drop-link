import axios from 'axios'

const api = axios.create({
  baseURL: '/api/files',
  timeout: 300000  // 5 minutes, sufficient for large file uploads
})

export function fetchFileList() {
  return api.get('/')
}

export function uploadFile(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)

  return api.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

export function downloadFile(fileId) {
  window.open(`/api/files/${fileId}/download`, '_blank')
}