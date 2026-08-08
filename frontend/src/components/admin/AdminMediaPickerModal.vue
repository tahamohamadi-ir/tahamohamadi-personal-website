<script setup>
import { computed, inject, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'
import { validateMediaUpload } from 'src/services/mediaUploadPolicy'

const props = defineProps({
  modelValue: Boolean,
  allowedTypes: {
    type: Array,
    default: () => ['image', 'document']
  },
  selectedId: {
    type: [String, Array, null],
    default: null
  },
  multiple: Boolean
})

const emit = defineEmits(['update:modelValue', 'select'])
const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()

const items = ref([])
const loading = ref(false)
const error = ref(null)
const page = ref(0)
const totalPages = ref(0)
const query = ref('')
const selectedType = ref(props.allowedTypes.length === 1 ? props.allowedTypes[0] : null)
const activeAsset = ref(null)
const selectedAssetIds = ref([])
const uploadFile = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)

const typeOptions = computed(() => [
  ...(props.allowedTypes.length > 1 ? [{ label: t('admin.mediaSelector.allTypes'), value: null }] : []),
  ...props.allowedTypes.map((val) => ({
    label: t(`admin.mediaSelector.types.${val}`),
    value: val
  }))
])

const canGoBack = computed(() => page.value > 0)
const canGoForward = computed(() => page.value + 1 < totalPages.value)
const hasSelection = computed(() => selectedAssetIds.value.length > 0)

function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`
}

function getMediaUrl(item) {
  if (!item?.id) return ''
  return `/api/v1/admin/media/${item.id}/content`
}

async function load(requestedPage = 0) {
  loading.value = true
  error.value = null
  try {
    const response = await httpClient.get('/api/v1/admin/media', {
      params: {
        page: requestedPage,
        size: 18,
        status: 'ACTIVE',
        query: query.value.trim() || undefined,
        type: selectedType.value || undefined
      }
    })
    items.value = response.data.items ?? []
    page.value = response.data.page ?? requestedPage
    totalPages.value = response.data.totalPages ?? 0

    syncSelectedAssets()
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    loading.value = false
  }
}

function selectedIds(value = props.selectedId) {
  if (Array.isArray(value)) return value.filter((id) => typeof id === 'string')
  return typeof value === 'string' ? [value] : []
}

function syncSelectedAssets() {
  const currentIds = selectedIds()
  selectedAssetIds.value = props.multiple ? currentIds : currentIds.slice(0, 1)
  activeAsset.value = items.value.find((item) => item.id === selectedAssetIds.value.at(-1)) ?? null
}

function selectAsset(asset) {
  if (props.multiple) {
    selectedAssetIds.value = selectedAssetIds.value.includes(asset.id)
      ? selectedAssetIds.value.filter((id) => id !== asset.id)
      : [...selectedAssetIds.value, asset.id]
    activeAsset.value = selectedAssetIds.value.includes(asset.id) ? asset : null
    return
  }
  selectedAssetIds.value = [asset.id]
  activeAsset.value = asset
}

function confirmSelection() {
  if (!hasSelection.value) return
  emit('select', props.multiple ? [...selectedAssetIds.value] : selectedAssetIds.value[0])
  emit('update:modelValue', false)
}

function closeModal() {
  emit('update:modelValue', false)
}

function acceptsUpload(file) {
  if (!file) return false
  return !validateMediaUpload(file, props.allowedTypes)
}

async function handleUpload() {
  if (!uploadFile.value) return
  if (!acceptsUpload(uploadFile.value)) {
    error.value = { message: t('admin.mediaSelector.invalidType') }
    return
  }

  uploading.value = true
  uploadProgress.value = 0
  error.value = null

  try {
    await primeCsrfToken(httpClient)
    const formData = new FormData()
    formData.append('file', uploadFile.value)

    const response = await httpClient.post('/api/v1/admin/media', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (evt) => {
        if (evt.total) uploadProgress.value = Math.round((evt.loaded * 100) / evt.total)
      }
    })

    uploadFile.value = null
    await load(0)
    if (response.data?.id) {
      const created = items.value.find((item) => item.id === response.data.id)
      if (created) selectAsset(created)
    }
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    uploading.value = false
  }
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    syncSelectedAssets()
    void load(0)
  }
}, { immediate: true })

watch(() => props.selectedId, () => {
  if (props.modelValue) syncSelectedAssets()
}, { deep: true })
</script>

<template>
  <q-dialog :model-value="modelValue" persistent max-width="1100px" @update:model-value="closeModal">
    <q-card class="media-picker-modal">
      <header class="media-picker-modal__header">
        <div class="text-h6 text-weight-bold">{{ t('admin.mediaSelector.dialogTitle') }}</div>
        <q-btn flat round icon="close" @click="closeModal" />
      </header>

      <q-banner v-if="error" class="bg-red-1 text-negative q-ma-md" rounded>
        {{ error.message }}
      </q-banner>

      <!-- Upload Banner -->
      <div class="media-picker-modal__uploader q-px-md q-pt-sm">
        <q-file
          v-model="uploadFile"
          :accept="allowedTypes.includes('image') && allowedTypes.includes('document') ? 'image/*,application/pdf' : (allowedTypes.includes('image') ? 'image/*' : 'application/pdf')"
          outlined
          dense
          clearable
          :label="t('admin.mediaSelector.uploadFile')"
          class="full-width"
          @update:model-value="handleUpload"
        >
          <template #prepend>
            <q-icon name="cloud_upload" />
          </template>
        </q-file>
        <q-linear-progress v-if="uploading" :value="uploadProgress / 100" color="primary" class="q-mt-xs" />
      </div>

      <!-- Search & Filter Bar -->
      <div class="media-picker-modal__toolbar q-px-md q-py-sm">
        <q-input
          v-model="query"
          outlined
          dense
          clearable
          :placeholder="t('admin.mediaSelector.searchPlaceholder')"
          class="media-picker-modal__search"
          @keyup.enter="load(0)"
        >
          <template #append>
            <q-btn flat round icon="search" @click="load(0)" />
          </template>
        </q-input>
        <q-select
          v-model="selectedType"
          outlined
          dense
          emit-value
          map-options
          :options="typeOptions"
          class="media-picker-modal__type-select"
          @update:model-value="load(0)"
        />
      </div>

      <!-- Main Body: Grid + Inspector Sidebar -->
      <div class="media-picker-modal__body">
        <div class="media-picker-modal__grid-container">
          <q-inner-loading :showing="loading" />

          <div v-if="!loading && items.length === 0" class="media-picker-modal__empty">
            <q-icon name="perm_media" size="48px" color="grey-6" />
            <div class="text-subtitle1 text-weight-medium q-mt-sm">{{ t('admin.mediaSelector.empty') }}</div>
            <p class="text-caption text-grey-7">{{ t('admin.mediaSelector.emptyHelp') }}</p>
          </div>

          <div v-else class="media-picker-modal__grid">
            <article
              v-for="item in items"
              :key="item.id"
              class="media-picker-modal__card"
              :class="{ 'media-picker-modal__card--active': selectedAssetIds.includes(item.id) }"
              @click="selectAsset(item)"
            >
              <div class="media-picker-modal__card-thumb">
                <img
                  v-if="item.mimeType.startsWith('image/')"
                  :src="getMediaUrl(item)"
                  :alt="item.faAlt || item.originalFilename"
                  loading="lazy"
                />
                <q-icon v-else name="insert_drive_file" size="40px" color="primary" />
              </div>
              <div class="media-picker-modal__card-meta">
                <span class="media-picker-modal__filename" :title="item.originalFilename">{{ item.originalFilename }}</span>
                <span class="media-picker-modal__size">{{ formatFileSize(item.sizeBytes) }}</span>
              </div>
            </article>
          </div>
        </div>

        <!-- Sidebar Inspector -->
        <aside class="media-picker-modal__inspector">
          <div v-if="activeAsset" class="media-picker-modal__details">
            <div class="media-picker-modal__preview">
              <img
                v-if="activeAsset.mimeType.startsWith('image/')"
                :src="getMediaUrl(activeAsset)"
                :alt="activeAsset.originalFilename"
              />
              <q-icon v-else name="description" size="64px" color="primary" />
            </div>

            <div class="media-picker-modal__info q-mt-md">
              <div class="text-subtitle2 text-weight-bold truncate" :title="activeAsset.originalFilename">
                {{ activeAsset.originalFilename }}
              </div>
              <div class="text-caption text-grey-7">
                {{ activeAsset.mimeType }} • {{ formatFileSize(activeAsset.sizeBytes) }}
              </div>
              <div v-if="activeAsset.width && activeAsset.height" class="text-caption text-grey-7">
                {{ activeAsset.width }} × {{ activeAsset.height }} px
              </div>

              <div class="q-mt-sm">
                <q-chip
                  dense
                  :color="activeAsset.faAlt ? 'positive' : 'warning'"
                  text-color="white"
                  size="11px"
                >
                  FA: {{ activeAsset.faAlt ? '✓' : '●' }}
                </q-chip>
                <q-chip
                  dense
                  :color="activeAsset.enAlt ? 'positive' : 'warning'"
                  text-color="white"
                  size="11px"
                >
                  EN: {{ activeAsset.enAlt ? '✓' : '●' }}
                </q-chip>
              </div>
            </div>
          </div>
          <div v-else class="media-picker-modal__no-selection">
            <q-icon name="touch_app" size="32px" color="grey-5" />
            <div class="text-caption text-grey-6 q-mt-xs">{{ t('admin.mediaSelector.selectPrompt') }}</div>
          </div>
        </aside>
      </div>

      <!-- Pagination & Footer -->
      <footer class="media-picker-modal__footer">
        <div class="media-picker-modal__pagination">
          <q-btn flat icon="chevron_left" :disable="!canGoBack" @click="load(page - 1)" />
          <span class="text-caption">{{ page + 1 }} / {{ totalPages || 1 }}</span>
          <q-btn flat icon="chevron_right" :disable="!canGoForward" @click="load(page + 1)" />
        </div>
        <div class="media-picker-modal__actions">
          <q-btn flat :label="t('admin.actions.cancel')" @click="closeModal" />
          <q-btn
            color="primary"
            unelevated
            :disable="!hasSelection"
            :label="t('admin.mediaSelector.confirm')"
            @click="confirmSelection"
          />
        </div>
      </footer>
    </q-card>
  </q-dialog>
</template>

<style scoped>
.media-picker-modal {
  display: flex;
  flex-direction: column;
  height: 80vh;
  max-height: 750px;
  width: 100%;
}
.media-picker-modal__header {
  align-items: center;
  border-bottom: 1px solid var(--tm-admin-border, #e0e0e0);
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
}
.media-picker-modal__toolbar {
  align-items: center;
  display: flex;
  gap: 12px;
}
.media-picker-modal__search {
  flex: 1;
}
.media-picker-modal__type-select {
  width: 180px;
}
.media-picker-modal__body {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.media-picker-modal__grid-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  position: relative;
}
.media-picker-modal__grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
}
.media-picker-modal__card {
  border: 1px solid var(--tm-admin-border, #e2e8f0);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.2s ease;
}
.media-picker-modal__card:hover {
  border-color: var(--q-primary);
  transform: translateY(-2px);
}
.media-picker-modal__card--active {
  border-color: var(--q-primary);
  box-shadow: 0 0 0 2px var(--q-primary);
}
.media-picker-modal__card-thumb {
  align-items: center;
  aspect-ratio: 1 / 1;
  background: var(--tm-surface-subtle, #f8fafc);
  display: flex;
  justify-content: center;
  overflow: hidden;
}
.media-picker-modal__card-thumb img {
  height: 100%;
  object-fit: cover;
  width: 100%;
}
.media-picker-modal__card-meta {
  display: flex;
  flex-direction: column;
  padding: 6px 8px;
}
.media-picker-modal__filename {
  font-size: 12px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.media-picker-modal__size {
  color: var(--tm-text-secondary, #64748b);
  font-size: 10px;
}
.media-picker-modal__empty {
  align-items: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 16px;
  text-align: center;
}
.media-picker-modal__inspector {
  border-left: 1px solid var(--tm-admin-border, #e0e0e0);
  padding: 16px;
  width: 280px;
}
.media-picker-modal__preview {
  align-items: center;
  aspect-ratio: 16 / 10;
  background: var(--tm-surface-subtle, #f8fafc);
  border-radius: 6px;
  display: flex;
  justify-content: center;
  overflow: hidden;
}
.media-picker-modal__preview img {
  height: 100%;
  object-fit: contain;
  width: 100%;
}
.media-picker-modal__no-selection {
  align-items: center;
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: center;
  text-align: center;
}
.media-picker-modal__footer {
  align-items: center;
  border-top: 1px solid var(--tm-admin-border, #e0e0e0);
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
}
.media-picker-modal__actions {
  display: flex;
  gap: 8px;
}
</style>
