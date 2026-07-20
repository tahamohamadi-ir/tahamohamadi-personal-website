<script setup>
import { inject, onMounted, ref } from 'vue'

import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const items = ref([])
const state = ref('loading')
const error = ref(null)

async function load() {
  state.value = 'loading'
  error.value = null

  try {
    const response = await httpClient.get('/api/v1/admin/pages', {
      params: { page: 0, size: 100 }
    })

    items.value = response.data.items
    state.value = items.value.length === 0 ? 'empty' : 'ready'
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    state.value = 'error'
  }
}

onMounted(load)
</script>

<template>
  <q-page class="q-pa-md q-pa-lg-md">
    <div class="row items-center justify-between q-col-gutter-md q-mb-lg">
      <div class="col">
        <h1 class="text-h5 q-my-none">Managed pages</h1>
        <p class="text-body2 text-grey-8 q-mb-none">
          About and Research translations are managed independently.
        </p>
      </div>
    </div>

    <q-banner v-if="state === 'error'" class="bg-red-1 text-negative" rounded role="alert">
      {{ error?.message }}
      <template #action>
        <q-btn flat color="negative" label="Retry" @click="load" />
      </template>
    </q-banner>
    <q-skeleton v-else-if="state === 'loading'" type="rect" height="12rem" />
    <q-banner v-else-if="state === 'empty'" class="bg-grey-2" rounded>
      No managed pages are available yet.
    </q-banner>
    <q-list v-else bordered separator>
      <q-item v-for="page in items" :key="page.id">
        <q-item-section>
          <q-item-label>{{ page.pageKey }}</q-item-label>
          <q-item-label caption>
            {{ page.fa?.title }} · {{ page.en?.title }}
          </q-item-label>
        </q-item-section>
        <q-item-section side>
          <q-badge :label="page.status" :color="page.status === 'PUBLISHED' ? 'positive' : 'grey-7'" />
        </q-item-section>
      </q-item>
    </q-list>
  </q-page>
</template>
