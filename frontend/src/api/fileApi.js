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
  return api.post(`/${fileId}/verify`, { password })
}

export function checkRequiresPassword(fileId) {
  return api.get(`/${fileId}/requires-password`)
}

export async function downloadFile(fileId, password) {
  try {
    const response = await api.post(`/${fileId}/download`, { password }, {
      responseType: 'blob'
    })

    // Extract filename from Content-Disposition header
    const contentDisposition = response.headers['content-disposition']
    let filename = 'download'
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename\*=(?:UTF-8'')(.+)/i)
      if (filenameMatch) {
        filename = decodeURIComponent(filenameMatch[1])
      }
    }

    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Download failed:', error)
    throw error
  }
}

export function deleteFile(fileId) {
  return api.delete(`/${fileId}`)
}