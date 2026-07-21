<script setup>
import { computed, inject, onMounted, ref } from 'vue'

import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'
import { MEDIA_UPLOAD_LIMITS, validateMediaUpload } from 'src/services/mediaUploadPolicy'

const httpClient = inject(HTTP_CLIENT_KEY)
const ACCEPTED_TYPES = MEDIA_UPLOAD_LIMITS
const items = ref([])
const selected = ref(null)
const uploadFile = ref(null)
const uploadProgress = ref(0)
const page = ref(0)
const totalPages = ref(0)
const state = ref('loading')
const error = ref(null)
const uploading = ref(false)
const form = ref(metadata())

function metadata(value = {}) { return { id: value.id ?? null, originalFilename: value.originalFilename ?? '', mimeType: value.mimeType ?? '', sizeBytes: value.sizeBytes ?? 0, width: value.width ?? null, height: value.height ?? null, status: value.status ?? null, faAlt: value.faAlt ?? '', faCaption: value.faCaption ?? '', enAlt: value.enAlt ?? '', enCaption: value.enCaption ?? '', version: value.version ?? null } }
const isImage = computed(() => selected.value?.mimeType?.startsWith('image/') ?? false)

async function load(requestedPage = page.value) {
  state.value = 'loading'; error.value = null
  try { const response = await httpClient.get('/api/v1/admin/media', { params: { page: requestedPage, size: 20 } }); items.value = response.data.items ?? []; page.value = response.data.page ?? requestedPage; totalPages.value = response.data.totalPages ?? 0; state.value = items.value.length === 0 ? 'empty' : 'ready' }
  catch (cause) { error.value = normalizeApiError(cause); state.value = 'error' }
}
async function select(item) { error.value = null; try { const response = await httpClient.get(`/api/v1/admin/media/${item.id}`); selected.value = response.data; form.value = metadata(response.data) } catch (cause) { error.value = normalizeApiError(cause) } }
async function upload() {
  const duplicate = uploading.value
  if (duplicate) return
  const fileError = validateMediaUpload(uploadFile.value)
  if (fileError) { error.value = { message: fileError }; return }
  uploading.value = true; uploadProgress.value = 0; error.value = null
  try { await primeCsrfToken(httpClient); const data = new FormData(); data.append('file', uploadFile.value); data.append('faAlt', form.value.faAlt); data.append('faCaption', form.value.faCaption); data.append('enAlt', form.value.enAlt); data.append('enCaption', form.value.enCaption); const response = await httpClient.post('/api/v1/admin/media', data, { onUploadProgress: (event) => { uploadProgress.value = event.total ? Math.round((event.loaded / event.total) * 100) : 0 } }); selected.value = response.data; form.value = metadata(response.data); uploadFile.value = null; await load(0) }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { uploading.value = false }
}
async function saveMetadata() { if (!selected.value) return; uploading.value = true; error.value = null; try { await primeCsrfToken(httpClient); const response = await httpClient.put(`/api/v1/admin/media/${selected.value.id}/metadata`, { faAlt: form.value.faAlt, faCaption: form.value.faCaption, enAlt: form.value.enAlt, enCaption: form.value.enCaption, version: form.value.version }); selected.value = response.data; form.value = metadata(response.data); await load(page.value) } catch (cause) { error.value = normalizeApiError(cause) } finally { uploading.value = false } }
onMounted(() => { void load() })
</script>

<template>
  <q-page class="q-pa-md q-pa-lg-md"><h1 class="text-h5 q-mt-none">Media</h1><p class="text-body2 text-grey-8">Upload only PNG, JPEG, WebP, or PDF files. Browser checks do not replace backend validation.</p><q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">{{ error.message }}</q-banner><q-form class="q-gutter-md q-mb-xl" @submit.prevent="upload"><q-file v-model="uploadFile" :accept="Object.keys(ACCEPTED_TYPES).join(',')" label="Media file" :disable="uploading" /><q-input v-model="form.faAlt" label="Persian alt text" :disable="uploading" /><q-input v-model="form.faCaption" type="textarea" label="Persian caption" :disable="uploading" /><q-input v-model="form.enAlt" label="English alt text" :disable="uploading" /><q-input v-model="form.enCaption" type="textarea" label="English caption" :disable="uploading" /><q-linear-progress v-if="uploading" :value="uploadProgress / 100" aria-label="Upload progress" /><div class="row q-gutter-sm"><q-btn type="submit" color="primary" :loading="uploading" label="Upload media" /><q-btn v-if="error" flat label="Clear upload error" @click="error = null" /></div></q-form><AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" /><template v-else><q-list bordered separator><q-item v-for="item in items" :key="item.id" clickable @click="select(item)"><q-item-section><q-item-label>{{ item.id }}</q-item-label><q-item-label caption>{{ item.mimeType }} · {{ item.sizeBytes }} bytes</q-item-label></q-item-section><q-item-section side><q-badge :label="item.status" :color="item.status === 'ACTIVE' ? 'positive' : 'grey-7'" /></q-item-section></q-item></q-list><AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" /></template><q-form v-if="selected" class="q-mt-xl q-gutter-md" @submit.prevent="saveMetadata"><h2 class="text-h6 q-my-none">{{ form.originalFilename }}</h2><p v-if="isImage">Image preview is unavailable until the image has a public reference.</p><p v-else>Document representation: {{ form.mimeType }}</p><p class="text-caption">A public URL is available only after the media is publicly referenced.</p><q-input v-model="form.faAlt" label="Persian alt text" :disable="uploading" /><q-input v-model="form.faCaption" type="textarea" label="Persian caption" :disable="uploading" /><q-input v-model="form.enAlt" label="English alt text" :disable="uploading" /><q-input v-model="form.enCaption" type="textarea" label="English caption" :disable="uploading" /><q-btn type="submit" color="primary" :loading="uploading" label="Save metadata" /></q-form></q-page>
</template>
