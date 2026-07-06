import http from '@/api/http'

export function uploadImage(file: File) {
  const form = new FormData()
  form.append('file', file)
  return http.post<{ url: string }>('/admin/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
