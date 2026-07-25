<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { normalizeApiError } from 'src/services/httpClient'

const props = defineProps({
  modelValue: {
    type: [String, null],
    default: null
  },
  label: {
    type: String,
    default: null
  },
  disable: Boolean
})

const emit = defineEmits(['update:modelValue'])
const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()
const items = ref([])
const loading = ref(false)
const error = ref(null)

const options = computed(() => items.value
  .filter((item) => item.status === 'ACTIVE')
  .map((item) => ({
    label: t('admin.mediaSelector.optionLabel', {
      name: item.originalFilename || item.id,
      mimeType: item.mimeType
    }),
    value: item.id
  })))
const resolvedLabel = computed(() => props.label ?? t('admin.mediaSelector.label'))

async function load() {
  loading.value = true
  error.value = null

  try {
    const response = await httpClient.get('/api/v1/admin/media', {
      params: { page: 0, size: 100 }
    })
    items.value = response.data.items ?? []
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    loading.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <section :aria-label="t('admin.mediaSelector.selection')">
    <q-select
      :model-value="modelValue"
      :options="options"
      option-label="label"
      option-value="value"
      emit-value
      map-options
      clearable
      :label="resolvedLabel"
      :disable="disable || loading"
      @update:model-value="emit('update:modelValue', $event ?? null)"
    />
    <p v-if="error" class="text-negative text-caption q-mt-xs" role="alert">
      {{ error.message }}
      <q-btn flat dense no-caps class="q-ml-xs" :label="t('admin.mediaSelector.retry')" :disable="loading" @click="load" />
    </p>
  </section>
</template>
