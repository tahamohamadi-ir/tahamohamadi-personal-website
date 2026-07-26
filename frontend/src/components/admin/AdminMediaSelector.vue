<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

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
const items = ref([])
const loading = ref(false)
const error = ref(null)
const page = ref(0)
const totalPages = ref(0)
const query = ref('')
const type = ref(props.allowedTypes.length === 1 ? props.allowedTypes[0] : null)
const uploadFile = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)

const typeOptions = computed(() => [
  ...(props.allowedTypes.length > 1 ? [{ label: t('admin.mediaSelector.allTypes'), value: null }] : []),
  ...props.allowedTypes.map((value) => ({
    label: t(`admin.mediaSelector.types.${value}`),
    value
  }))
])
const options = computed(() => items.value.map((item) => ({
  label: t('admin.mediaSelector.optionLabel', {
    name: item.originalFilename,
    mimeType: item.mimeType
  }),
  value: item.id
})))
const selectedItems = computed(() => {
  const ids = Array.isArray(props.modelValue) ? props.modelValue : [props.modelValue]
  return items.value.filter((item) => ids.includes(item.id))
})
const resolvedLabel = computed(() => props.label ?? t('admin.mediaSelector.label'))
const canGoBack = computed(() => page.value > 0)
const canGoForward = computed(() => page.value + 1 < totalPages.value)

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
        type: type.value || undefined
      }
    })
    items.value = response.data.items ?? []
    page.value = response.data.page ?? requestedPage
    totalPages.value = response.data.totalPages ?? 0
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    loading.value = false
  }
}

function choose(value) {
  emit('update:modelValue', props.multiple ? (value ?? []) : (value ?? null))
}

function acceptsUpload(file) {
  if (!file) return true
  return props.allowedTypes.some((allowed) => allowed === 'image'
    ? file.type.startsWith('image/')
    : file.type === 'application/pdf')
}

async function uploadInFlow() {
  const validationError = validateMediaUpload(uploadFile.value)
  if (validationError) { error.value = { message: validationError }; return }
  if (!acceptsUpload(uploadFile.value)) { error.value = { message: t('admin.mediaSelector.invalidType') }; return }

  uploading.value = true
  uploadProgress.value = 0
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const data = new FormData()
    data.append('file', uploadFile.value)
    const response = await httpClient.post('/api/v1/admin/media', data, {
      onUploadProgress: (event) => {
        uploadProgress.value = event.total ? Math.round((event.loaded / event.total) * 100) : 0
      }
    })
    uploadFile.value = null
    if (props.multiple) {
      choose([...(Array.isArray(props.modelValue) ? props.modelValue : []), response.data.id])
    }
    else {
      choose(response.data.id)
    }
    await load(0)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { uploading.value = false }
}

watch([query, type], () => {
  void load(0)
})

watch(() => props.allowedTypes, (types) => {
  if (!types.includes(type.value)) {
    type.value = types.length === 1 ? types[0] : null
  }
})

onMounted(() => {
  void load()
})
</script>

<template>
  <section :aria-label="t('admin.mediaSelector.selection')" class="admin-media-selector">
    <q-form class="admin-media-selector__upload" @submit.prevent="uploadInFlow">
      <q-file v-model="uploadFile" :accept="allowedTypes.includes('image') && allowedTypes.includes('document') ? 'image/*,application/pdf' : (allowedTypes.includes('image') ? 'image/*' : 'application/pdf')" :label="t('admin.mediaSelector.uploadFile')" :disable="disable || uploading" />
      <q-btn type="submit" color="primary" no-caps :loading="uploading" :disable="disable || !uploadFile" :label="t('admin.mediaSelector.upload')" />
      <q-linear-progress v-if="uploading" :value="uploadProgress / 100" :aria-label="t('admin.mediaSelector.uploadProgress')" />
    </q-form>
    <div class="admin-media-selector__filters">
      <q-input
        v-model="query"
        debounce="300"
        clearable
        :label="t('admin.mediaSelector.search')"
        :disable="disable || loading"
      />
      <q-select
        v-if="typeOptions.length > 1"
        v-model="type"
        :options="typeOptions"
        emit-value
        map-options
        :label="t('admin.mediaSelector.type')"
        :disable="disable || loading"
      />
    </div>
    <q-select
      :model-value="modelValue"
      :options="options"
      option-label="label"
      option-value="value"
      emit-value
      map-options
      clearable
      :multiple="multiple"
      :use-chips="multiple"
      :label="resolvedLabel"
      :disable="disable || loading"
      @update:model-value="choose"
    />
    <div v-if="selectedItems.length" class="admin-media-selector__selections">
      <div v-for="selected in selectedItems" :key="selected.id" class="admin-media-selector__selection">
        <img
          v-if="selected.mimeType?.startsWith('image/')"
          class="admin-media-selector__preview"
          :src="`/api/v1/admin/media/${selected.id}/content`"
          :alt="selected.originalFilename"
        >
        <p>{{ t('admin.mediaSelector.selected', { name: selected.originalFilename }) }}</p>
      </div>
    </div>
    <p v-else-if="!loading && items.length === 0" class="admin-media-selector__empty" role="status">
      {{ t('admin.mediaSelector.empty') }}
    </p>
    <nav v-if="totalPages > 1" :aria-label="t('admin.mediaSelector.pagination')" class="admin-media-selector__pagination">
      <q-btn flat no-caps :label="t('admin.pagination.previous')" :disable="!canGoBack || loading" @click="load(page - 1)" />
      <span aria-live="polite">{{ t('admin.pagination.status', { page: page + 1, total: totalPages }) }}</span>
      <q-btn flat no-caps :label="t('admin.pagination.next')" :disable="!canGoForward || loading" @click="load(page + 1)" />
    </nav>
    <p v-if="error" class="text-negative text-caption q-mt-xs" role="alert">
      {{ error.message }}
      <q-btn flat dense no-caps class="q-ml-xs" :label="t('admin.mediaSelector.retry')" :disable="loading" @click="load(page)" />
    </p>
  </section>
</template>

<style scoped>
.admin-media-selector { display: grid; gap: var(--tm-space-3); }
.admin-media-selector__upload { align-items: end; display: grid; gap: var(--tm-space-2); grid-template-columns: minmax(0, 1fr) auto; }
.admin-media-selector__upload :deep(.q-linear-progress) { grid-column: 1 / -1; }
.admin-media-selector__filters { display: grid; gap: var(--tm-space-3); grid-template-columns: repeat(auto-fit, minmax(min(100%, 14rem), 1fr)); }
.admin-media-selector__selections { display: grid; gap: var(--tm-space-2); }
.admin-media-selector__selection { display: grid; gap: var(--tm-space-2); grid-template-columns: minmax(0, 7rem) minmax(0, 1fr); align-items: center; }
.admin-media-selector__selection p, .admin-media-selector__empty { color: var(--tm-text-secondary); margin: 0; }
.admin-media-selector__preview { aspect-ratio: 1; background: var(--tm-admin-surface-subtle); display: block; inline-size: 100%; object-fit: cover; }
.admin-media-selector__pagination { align-items: center; display: flex; flex-wrap: wrap; gap: var(--tm-space-2); }
</style>
