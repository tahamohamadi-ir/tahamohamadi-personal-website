<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminMediaSelector from 'src/components/admin/AdminMediaSelector.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'
import { MEDIA_UPLOAD_LIMITS, validateMediaUpload } from 'src/services/mediaUploadPolicy'

const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()
const ACCEPTED_TYPES = MEDIA_UPLOAD_LIMITS
const items = ref([])
const orphans = ref([])
const selected = ref(null)
const usages = ref([])
const uploadFile = ref(null)
const uploadProgress = ref(0)
const page = ref(0)
const totalPages = ref(0)
const query = ref('')
const type = ref(null)
const statusFilter = ref(null)
const state = ref('loading')
const error = ref(null)
const uploading = ref(false)
const archiving = ref(false)
const archiveConfirmationOpen = ref(false)
const replaceConfirmationOpen = ref(false)
const replacementMediaId = ref(null)
const form = ref(metadata())

function metadata(value = {}) {
  return {
    id: value.id ?? null, originalFilename: value.originalFilename ?? '', mimeType: value.mimeType ?? '',
    sizeBytes: value.sizeBytes ?? 0, width: value.width ?? null, height: value.height ?? null,
    status: value.status ?? null, faAlt: value.faAlt ?? '', faCaption: value.faCaption ?? '',
    enAlt: value.enAlt ?? '', enCaption: value.enCaption ?? '', version: value.version ?? null
  }
}

const isImage = computed(() => selected.value?.mimeType?.startsWith('image/') ?? false)
const replacementTypes = computed(() => isImage.value ? ['image'] : ['document'])
const selectedIsOrphan = computed(() => orphans.value.some((orphan) => orphan.id === selected.value?.id))
const typeOptions = computed(() => [
  { label: t('admin.mediaSelector.allTypes'), value: null },
  { label: t('admin.mediaSelector.types.image'), value: 'image' },
  { label: t('admin.mediaSelector.types.document'), value: 'document' }
])
const statusOptions = computed(() => [
  { label: t('admin.media.allStatuses'), value: null },
  { label: t('admin.media.active'), value: 'ACTIVE' },
  { label: t('admin.media.archived'), value: 'ARCHIVED' }
])

async function load(requestedPage = page.value) {
  state.value = 'loading'
  error.value = null
  try {
    const [response, orphanResponse] = await Promise.all([
      httpClient.get('/api/v1/admin/media', { params: { page: requestedPage, size: 20, query: query.value.trim() || undefined, type: type.value || undefined, status: statusFilter.value || undefined } }),
      httpClient.get('/api/v1/admin/media/orphans')
    ])
    items.value = response.data.items ?? []
    page.value = response.data.page ?? requestedPage
    totalPages.value = response.data.totalPages ?? 0
    orphans.value = Array.isArray(orphanResponse.data) ? orphanResponse.data : []
    state.value = items.value.length === 0 ? 'empty' : 'ready'
  }
  catch (cause) { error.value = normalizeApiError(cause); state.value = 'error' }
}

async function select(item) {
  error.value = null
  try {
    const [response, usageResponse] = await Promise.all([
      httpClient.get(`/api/v1/admin/media/${item.id}`),
      httpClient.get(`/api/v1/admin/media/${item.id}/usage`)
    ])
    selected.value = response.data
    form.value = metadata(response.data)
    usages.value = usageResponse.data ?? []
  }
  catch (cause) { usages.value = []; error.value = normalizeApiError(cause) }
}

async function upload() {
  if (uploading.value) return
  const fileError = validateMediaUpload(uploadFile.value)
  if (fileError) { error.value = { message: fileError }; return }
  uploading.value = true
  uploadProgress.value = 0
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const data = new FormData()
    data.append('file', uploadFile.value)
    data.append('faAlt', form.value.faAlt)
    data.append('faCaption', form.value.faCaption)
    data.append('enAlt', form.value.enAlt)
    data.append('enCaption', form.value.enCaption)
    const response = await httpClient.post('/api/v1/admin/media', data, {
      onUploadProgress: (event) => {
        uploadProgress.value = event.total ? Math.round((event.loaded / event.total) * 100) : 0
      }
    })
    selected.value = response.data
    form.value = metadata(response.data)
    uploadFile.value = null
    await load(0)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { uploading.value = false }
}

async function saveMetadata() {
  if (!selected.value) return
  uploading.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.put(`/api/v1/admin/media/${selected.value.id}/metadata`, {
      faAlt: form.value.faAlt, faCaption: form.value.faCaption,
      enAlt: form.value.enAlt, enCaption: form.value.enCaption, version: form.value.version
    })
    selected.value = response.data
    form.value = metadata(response.data)
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { uploading.value = false }
}

async function archive() {
  if (!selected.value || archiving.value) return
  archiving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    await httpClient.delete(`/api/v1/admin/media/${selected.value.id}`, {
      params: { version: form.value.version }
    })
    selected.value = null
    usages.value = []
    form.value = metadata()
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { archiving.value = false }
}

async function replace() {
  if (!selected.value || !replacementMediaId.value || uploading.value) return
  uploading.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.post(`/api/v1/admin/media/${selected.value.id}/replace`, {
      replacementMediaId: replacementMediaId.value,
      version: form.value.version
    })
    replaceConfirmationOpen.value = false
    replacementMediaId.value = null
    selected.value = null
    usages.value = []
    await load(0)
    await select(response.data)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { uploading.value = false }
}

function confirmArchive() {
  archiveConfirmationOpen.value = false
  void archive()
}

watch([query, type, statusFilter], () => { void load(0) })
onMounted(() => { void load() })
</script>

<template>
  <q-page class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1 class="text-h4 q-my-none">{{ t('admin.media.title') }}</h1>
        <p class="admin-page__description">{{ t('admin.media.description') }}</p>
      </div>
    </header>
    <q-banner v-if="error" class="bg-red-1 text-negative" role="alert">{{ error.message }}</q-banner>
    <q-form class="admin-panel q-pa-md q-gutter-md" @submit.prevent="upload">
      <h2 class="text-h6 q-my-none">{{ t('admin.media.uploadTitle') }}</h2>
      <q-file v-model="uploadFile" :accept="Object.keys(ACCEPTED_TYPES).join(',')" :label="t('admin.media.file')" :disable="uploading" />
      <div class="row q-col-gutter-md">
        <q-input v-model="form.faAlt" class="col-12 col-md-6" :label="t('admin.media.faAlt')" :disable="uploading" />
        <q-input v-model="form.enAlt" class="col-12 col-md-6" :label="t('admin.media.enAlt')" :disable="uploading" />
        <q-input v-model="form.faCaption" class="col-12 col-md-6" type="textarea" :label="t('admin.media.faCaption')" :disable="uploading" />
        <q-input v-model="form.enCaption" class="col-12 col-md-6" type="textarea" :label="t('admin.media.enCaption')" :disable="uploading" />
      </div>
      <q-linear-progress v-if="uploading" :value="uploadProgress / 100" :aria-label="t('admin.media.uploadProgress')" />
      <q-btn type="submit" color="primary" no-caps :loading="uploading" :label="t('admin.media.upload')" />
    </q-form>
    <q-banner v-if="orphans.length" class="bg-orange-1 text-warning" role="status">
      {{ t('admin.media.orphanNotice', { count: orphans.length }) }}
    </q-banner>
    <div class="admin-media__filters">
      <q-input v-model="query" debounce="300" clearable :label="t('admin.mediaSelector.search')" :disable="state === 'loading'" />
      <q-select v-model="type" :options="typeOptions" emit-value map-options :label="t('admin.mediaSelector.type')" :disable="state === 'loading'" />
      <q-select v-model="statusFilter" :options="statusOptions" emit-value map-options :label="t('admin.media.status')" :disable="state === 'loading'" />
    </div>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator>
        <q-item v-for="item in items" :key="item.id" clickable @click="select(item)">
          <q-item-section>
            <q-item-label>{{ item.originalFilename ?? item.id }}</q-item-label>
            <q-item-label caption>{{ item.mimeType }} · {{ t('admin.media.bytes', { count: item.sizeBytes }) }}</q-item-label>
          </q-item-section>
          <q-item-section side><q-badge :label="item.status" :color="item.status === 'ACTIVE' ? 'positive' : 'grey-7'" /></q-item-section>
        </q-item>
      </q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>
    <q-form v-if="selected" class="admin-panel q-pa-md q-gutter-md" @submit.prevent="saveMetadata">
      <q-banner v-if="usages.length" class="bg-orange-1 text-warning" role="status">
        {{ t('admin.mediaUsage.notice', { count: usages.length }) }}
        <q-list dense class="q-mt-sm">
          <q-item v-for="usage in usages" :key="`${usage.ownerType}-${usage.ownerId}`">
            <q-item-section>{{ t(`admin.mediaUsage.types.${usage.ownerType}`) }} · {{ usage.lifecycleStatus }}</q-item-section>
          </q-item>
        </q-list>
      </q-banner>
      <div class="admin-page__header">
        <div><h2 class="text-h5 q-my-none">{{ form.originalFilename }}</h2><p class="admin-page__description">{{ form.mimeType }} · {{ t('admin.media.bytes', { count: form.sizeBytes }) }}</p></div>
        <div class="row items-center q-gutter-xs">
          <q-badge :color="form.faAlt ? 'positive' : 'grey-6'" :label="`FA ${form.faAlt ? '✓' : '—'}`" />
          <q-badge :color="form.enAlt ? 'positive' : 'grey-6'" :label="`EN ${form.enAlt ? '✓' : '—'}`" />
          <q-badge v-if="selectedIsOrphan" color="warning" :label="t('admin.media.orphaned')" />
        </div>
      </div>
      <p v-if="isImage" class="text-body2">{{ t('admin.media.localizedMetadata') }}</p>
      <img v-if="isImage" class="admin-media__preview" :src="`/api/v1/admin/media/${selected.id}/content`" :alt="form.originalFilename">
      <p v-else class="text-body2">{{ t('admin.media.documentRepresentation', { mimeType: form.mimeType }) }}</p>
      <div class="row q-col-gutter-md">
        <q-input v-model="form.faAlt" class="col-12 col-md-6" :label="t('admin.media.faAlt')" :disable="uploading" />
        <q-input v-model="form.enAlt" class="col-12 col-md-6" :label="t('admin.media.enAlt')" :disable="uploading" />
        <q-input v-model="form.faCaption" class="col-12 col-md-6" type="textarea" :label="t('admin.media.faCaption')" :disable="uploading" />
        <q-input v-model="form.enCaption" class="col-12 col-md-6" type="textarea" :label="t('admin.media.enCaption')" :disable="uploading" />
      </div>
      <div class="admin-form-actions">
        <q-btn type="submit" color="primary" no-caps :loading="uploading" :label="t('admin.media.saveMetadata')" />
        <q-btn outline color="primary" no-caps :disable="uploading || selected.status !== 'ACTIVE'" :label="t('admin.mediaReplace.action')" @click="replaceConfirmationOpen = true" />
        <q-btn v-if="selectedIsOrphan" outline color="negative" no-caps :loading="archiving" :label="t('admin.media.archiveOrphan')" @click="archiveConfirmationOpen = true" />
      </div>
    </q-form>
    <q-dialog v-model="archiveConfirmationOpen" persistent>
      <q-card>
        <q-card-section class="text-h6">{{ t('admin.media.archiveTitle') }}</q-card-section>
        <q-card-section>{{ t('admin.media.archiveDescription') }}</q-card-section>
        <q-card-actions align="right">
          <q-btn flat :label="t('admin.actions.cancel')" @click="archiveConfirmationOpen = false" />
          <q-btn color="negative" :label="t('admin.media.archiveOrphan')" :loading="archiving" @click="confirmArchive" />
        </q-card-actions>
      </q-card>
    </q-dialog>
    <q-dialog v-model="replaceConfirmationOpen" persistent>
      <q-card class="q-pa-md" style="width: min(100%, 42rem)">
        <q-card-section class="text-h6">{{ t('admin.mediaReplace.title') }}</q-card-section>
        <q-card-section>{{ t('admin.mediaReplace.description') }}</q-card-section>
        <q-card-section><AdminMediaSelector v-model="replacementMediaId" :allowed-types="replacementTypes" :label="t('admin.mediaReplace.replacement')" :disable="uploading" /></q-card-section>
        <q-card-actions align="right">
          <q-btn flat :label="t('admin.actions.cancel')" @click="replaceConfirmationOpen = false" />
          <q-btn color="primary" :disable="!replacementMediaId" :loading="uploading" :label="t('admin.mediaReplace.confirm')" @click="replace" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<style scoped>
.admin-media__filters { display: grid; gap: var(--tm-space-3); grid-template-columns: repeat(auto-fit, minmax(min(100%, 14rem), 1fr)); }
.admin-media__preview { aspect-ratio: 16 / 9; background: var(--tm-admin-surface-subtle); display: block; inline-size: min(100%, 36rem); object-fit: contain; }
</style>
