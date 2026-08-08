export const MEDIA_UPLOAD_LIMITS = Object.freeze({
  'image/png': 10 * 1024 * 1024,
  'image/jpeg': 10 * 1024 * 1024,
  'image/webp': 10 * 1024 * 1024,
  'application/pdf': 20 * 1024 * 1024
})

export const MEDIA_UPLOAD_SIZE_ERROR = 'File exceeds the supported size limit.'

export function mediaTypeForMime(mimeType) {
  if (mimeType?.startsWith('image/')) return 'image'
  if (mimeType === 'application/pdf') return 'document'
  return null
}

export function validateMediaUpload(file, allowedTypes = ['image', 'document']) {
  if (!file) return 'Select a file to upload.'
  if (!Object.hasOwn(MEDIA_UPLOAD_LIMITS, file.type)) return 'Unsupported media type.'
  if (!allowedTypes.includes(mediaTypeForMime(file.type))) return 'Unsupported media type.'
  if (file.size > MEDIA_UPLOAD_LIMITS[file.type]) return MEDIA_UPLOAD_SIZE_ERROR
  if (file.name.includes('..') || /[\\/\u0000]/.test(file.name)) return 'Unsafe filename.'
  return null
}
