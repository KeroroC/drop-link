import axios from 'axios'

const api = axios.create({
  baseURL: '/api/files',
  timeout: 300000  // 5 minutes, sufficient for large file uploads
})

export function fetchFileList() {
  return api.get('/')
}

export function uploadFile(file, expireHours, password, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  if (expireHours !== null && expireHours !== undefined) {
    formData.append('expireHours', expireHours)
  }
  if (password) {
    formData.append('password', password)
  }

  return api.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

export function verifyPassword(fileId, password) {
  return api.get(`/${fileId}/verify`, { params: { password } })
}

export function downloadFile(fileId, password) {
  const params = password ? `?password=${encodeURIComponent(password)}` : ''
  window.open(`/api/files/${fileId}/download${params}`, '_blank')
}

export function deleteFile(fileId) {
  return api.delete(`/${fileId}`)
}