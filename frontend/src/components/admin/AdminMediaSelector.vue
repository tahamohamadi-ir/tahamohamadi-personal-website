<script setup>
import { computed, inject, onMounted, ref } from 'vue'

import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { normalizeApiError } from 'src/services/httpClient'

const props = defineProps({
  modelValue: {
    type: [String, null],
    default: null
  },
  label: {
    type: String,
    default: 'Media asset'
  },
  disable: Boolean
})

const emit = defineEmits(['update:modelValue'])
const httpClient = inject(HTTP_CLIENT_KEY)
const items = ref([])
const loading = ref(false)
const error = ref(null)

const options = computed(() => items.value
  .filter((item) => item.status === 'ACTIVE')
  .map((item) => ({
    label: `${item.id} · ${item.mimeType}`,
    value: item.id
  })))

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
  <section aria-label="Media selection">
    <q-select
      :model-value="modelValue"
      :options="options"
      option-label="label"
      option-value="value"
      emit-value
      map-options
      clearable
      :label="label"
      :disable="disable || loading"
      @update:model-value="emit('update:modelValue', $event ?? null)"
    />
    <p v-if="error" class="text-negative text-caption q-mt-xs" role="alert">
      {{ error.message }}
      <button type="button" @click="load">Retry media list</button>
    </p>
  </section>
</template>
