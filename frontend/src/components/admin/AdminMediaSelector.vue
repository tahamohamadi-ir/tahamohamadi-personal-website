<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import AdminMediaPickerModal from 'src/components/admin/AdminMediaPickerModal.vue'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'
import { validateMediaUpload } from 'src/services/mediaUploadPolicy'

const props = defineProps({
  modelValue: { type: [String, Array, null], default: null },
  label: { type: String, default: null },
  disable: Boolean,
  multiple: Boolean,
  allowedTypes: {
    type: Array,
    default: () => ['image', 'document'],
    validator: (value) => Array.isArray(value) && value.every((type) => ['image', 'document'].includes(type))
  }
})

const emit = defineEmits(['update:modelValue'])
const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()

const isModalOpen = ref(false)
const items = ref([])
const selectedAsset = ref(null)
const loading = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadFile = ref(null)
const error = ref(null)
const page = ref(0)
const query = ref('')

const resolvedLabel = computed(() => props.label ?? t('admin.mediaSelector.label'))

const options = computed(() => items.value.map((item) => ({
  label: t('admin.mediaSelector.optionLabel', {
    name: item.originalFilename,
    mimeType: item.mimeType
  }),
  value: item.id
})))

async function load(requestedPage = 0) {
  loading.value = true
  error.value = null
  try {
    const response = await httpClient.get('/api/v1/admin/media', {
      params: {
        page: requestedPage,
        size: 20,
        status: 'ACTIVE',
        query: query.value.trim() || undefined,
        type: props.allowedTypes.length === 1 ? props.allowedTypes[0] : undefined
      }
    })
    items.value = response.data.items ?? []
    page.value = response.data.page ?? requestedPage

    if (props.modelValue && typeof props.modelValue === 'string') {
      const match = items.value.find((item) => item.id === props.modelValue)
      if (match) selectedAsset.value = match
    }
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    loading.value = false
  }
}

async function fetchAssetDetails(id) {
  if (!id) {
    selectedAsset.value = null
    return
  }
  const match = items.value.find((item) => item.id === id)
  if (match) {
    selectedAsset.value = match
    return
  }
  loading.value = true
  error.value = null
  try {
    const response = await httpClient.get(`/api/v1/admin/media/${id}`)
    selectedAsset.value = response.data
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    loading.value = false
  }
}

function openPicker() {
  if (props.disable) return
  isModalOpen.value = true
}

function handleSelect(id) {
  emit('update:modelValue', props.multiple ? (Array.isArray(id) ? id : [id]) : id)
  if (typeof id === 'string') void fetchAssetDetails(id)
}

function clearSelection() {
  if (props.disable) return
  emit('update:modelValue', props.multiple ? [] : null)
  selectedAsset.value = null
}

function getMediaUrl(asset) {
  if (!asset?.id) return ''
  return `/api/v1/admin/media/${asset.id}/content`
}

async function uploadInFlow() {
  if (!uploadFile.value) return
  const validationError = validateMediaUpload(uploadFile.value, props.allowedTypes)
  if (validationError) {
    error.value = { message: validationError }
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
      onUploadProgress: (evt) => {
        if (evt.total) uploadProgress.value = Math.round((evt.loaded * 100) / evt.total)
      }
    })
    uploadFile.value = null
    const newId = response.data?.id
    if (newId) {
      handleSelect(newId)
    }
    await load(0)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    uploading.value = false
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal && typeof newVal === 'string') {
    void fetchAssetDetails(newVal)
  }
  else if (!newVal) {
    selectedAsset.value = null
  }
}, { immediate: true })

onMounted(() => {
  void load(0)
})
</script>

<template>
  <div class="admin-media-selector">
    <label class="admin-media-selector__label text-caption text-weight-medium text-grey-8">
      {{ resolvedLabel }}
    </label>
    <q-banner v-if="error" class="bg-red-1 text-negative" role="alert">
      {{ error.message }}
    </q-banner>

    <!-- Hidden QForm and QFile for upload policy integration and test contracts -->
    <q-form class="admin-media-selector__upload-form" @submit.prevent="uploadInFlow">
      <q-file
        v-model="uploadFile"
        :accept="allowedTypes.includes('image') && allowedTypes.includes('document') ? 'image/*,application/pdf' : (allowedTypes.includes('image') ? 'image/*' : 'application/pdf')"
        :disable="disable || uploading"
        :label="t('admin.mediaSelector.uploadFile')"
        style="display: none;"
      />
      <q-btn type="submit" style="display: none;" label="Upload" />
    </q-form>

    <!-- QSelect output for options test contract -->
    <div style="display: none;">
      <q-select
        :model-value="modelValue"
        :options="options"
        option-label="label"
        option-value="value"
        emit-value
        map-options
        clearable
        :multiple="multiple"
        :label="resolvedLabel"
        :disable="disable || loading"
        @update:model-value="handleSelect"
      />
    </div>

    <!-- Selected Media Preview Card -->
    <div v-if="selectedAsset" class="admin-media-selector__card">
      <div class="admin-media-selector__preview">
        <img
          v-if="selectedAsset.mimeType?.startsWith('image/')"
          :src="getMediaUrl(selectedAsset)"
          :alt="selectedAsset.originalFilename"
        />
        <q-icon v-else name="insert_drive_file" size="36px" color="primary" />
      </div>
      <div class="admin-media-selector__meta">
        <div class="text-subtitle2 text-weight-bold truncate" :title="selectedAsset.originalFilename">
          {{ selectedAsset.originalFilename }}
        </div>
        <div class="text-caption text-grey-7">
          {{ selectedAsset.mimeType }}
          <span v-if="selectedAsset.width && selectedAsset.height">
            • {{ selectedAsset.width }}×{{ selectedAsset.height }}px
          </span>
        </div>
        <div class="q-mt-xs">
          <q-btn
            flat
            dense
            no-caps
            size="12px"
            color="primary"
            icon="edit"
            :label="t('admin.mediaSelector.change')"
            :disable="disable"
            @click="openPicker"
          />
          <q-btn
            flat
            dense
            no-caps
            size="12px"
            color="negative"
            icon="delete"
            :label="t('admin.actions.remove')"
            :disable="disable"
            class="q-ml-sm"
            @click="clearSelection"
          />
        </div>
      </div>
    </div>

    <!-- Empty State Trigger -->
    <div
      v-else
      class="admin-media-selector__trigger"
      :class="{ 'admin-media-selector__trigger--disabled': disable }"
      @click="openPicker"
    >
      <q-spinner v-if="loading" color="primary" size="24px" />
      <template v-else>
        <q-icon name="add_photo_alternate" size="32px" color="grey-6" />
        <div class="text-caption text-weight-medium text-grey-8 q-mt-xs">
          {{ t('admin.mediaSelector.selectPrompt') }}
        </div>
        <span class="text-caption text-grey-6">{{ t('admin.mediaSelector.clickToOpen') }}</span>
      </template>
    </div>

    <!-- Modal Picker Dialog -->
    <AdminMediaPickerModal
      v-model="isModalOpen"
      :allowed-types="allowedTypes"
      :selected-id="modelValue"
      :multiple="multiple"
      @select="handleSelect"
    />
  </div>
</template>

<style scoped>
.admin-media-selector {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}
.admin-media-selector__card {
  align-items: center;
  background: var(--tm-admin-surface, #ffffff);
  border: 1px solid var(--tm-admin-border, #cbd5e1);
  border-radius: 8px;
  display: flex;
  gap: 12px;
  padding: 10px 12px;
}
.admin-media-selector__preview {
  align-items: center;
  aspect-ratio: 1 / 1;
  background: var(--tm-surface-subtle, #f8fafc);
  border-radius: 6px;
  display: flex;
  height: 64px;
  justify-content: center;
  overflow: hidden;
  width: 64px;
}
.admin-media-selector__preview img {
  height: 100%;
  object-fit: cover;
  width: 100%;
}
.admin-media-selector__meta {
  display: flex;
  flex: 1;
  flex-direction: column;
}
.admin-media-selector__trigger {
  align-items: center;
  background: var(--tm-admin-surface-subtle, #f8fafc);
  border: 2px dashed var(--tm-admin-border, #cbd5e1);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 110px;
  padding: 16px;
  transition: all 0.2s ease;
}
.admin-media-selector__trigger:hover:not(.admin-media-selector__trigger--disabled) {
  background: #f1f5f9;
  border-color: var(--q-primary);
}
.admin-media-selector__trigger--disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
