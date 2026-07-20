<script setup>
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

import AdminActivationActions from 'src/components/admin/AdminActivationActions.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import {
  createUnsavedChangesGuard,
  isVersionConflict,
  mapValidationErrors,
  useAdminNotifications
} from 'src/composables/adminContentInteractions'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const items = ref([])
const page = ref(0)
const totalPages = ref(0)
const state = ref('loading')
const error = ref(null)
const saving = ref(false)
const replacingForm = ref(false)
const form = ref(createForm())
const changes = createUnsavedChangesGuard(() => Promise.resolve(
  window.confirm('Discard unsaved social-link changes?')
))
const { notification, showSuccess, showError } = useAdminNotifications()

function createForm(value = {}) {
  return {
    id: value.id ?? null,
    platformCode: value.platformCode ?? '',
    url: value.url ?? '',
    sortOrder: value.sortOrder ?? 0,
    active: value.active ?? false,
    version: value.version ?? null
  }
}

function replaceForm(value = {}) {
  replacingForm.value = true
  form.value = createForm(value)
  changes.markSaved()
  nextTick(() => { replacingForm.value = false })
}

const fieldErrors = computed(() => mapValidationErrors(error.value))
const publicPreviewPath = computed(() => form.value.active ? '/en' : null)

watch(form, () => {
  if (!replacingForm.value) changes.markDirty()
}, { deep: true, flush: 'sync' })

onBeforeRouteLeave(async () => changes.confirmLeave())

function validHttpUrl(value) {
  return /^https?:\/\/.+/.test(value?.trim() ?? '') || 'Enter an http or https URL.'
}

async function load(requestedPage = page.value) {
  state.value = 'loading'
  error.value = null
  try {
    const response = await httpClient.get('/api/v1/admin/social-links', {
      params: { page: requestedPage, size: 20, sort: 'updatedAt,desc' }
    })
    items.value = response.data.items ?? []
    page.value = response.data.page ?? requestedPage
    totalPages.value = response.data.totalPages ?? 0
    state.value = items.value.length === 0 ? 'empty' : 'ready'
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    state.value = 'error'
  }
}

async function select(item) {
  if (!(await changes.confirmLeave())) return
  error.value = null
  try {
    const response = await httpClient.get(`/api/v1/admin/social-links/${item.id}`)
    replaceForm(response.data)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    showError(error.value.message)
  }
}

async function create() {
  if (!(await changes.confirmLeave())) return
  replaceForm()
  error.value = null
}

function payload() {
  return {
    platformCode: form.value.platformCode.trim(),
    url: form.value.url.trim(),
    sortOrder: Number(form.value.sortOrder),
    version: form.value.version
  }
}

async function save() {
  if (saving.value) return
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = form.value.id
      ? await httpClient.put(`/api/v1/admin/social-links/${form.value.id}`, payload())
      : await httpClient.post('/api/v1/admin/social-links', payload())
    replaceForm(response.data)
    showSuccess('Social link saved.')
    await load(page.value)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    showError(error.value.message)
  }
  finally { saving.value = false }
}

async function transition(action) {
  if (!form.value.id || saving.value) return
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.post(
      `/api/v1/admin/social-links/${form.value.id}/${action}`,
      null,
      { params: { version: form.value.version } }
    )
    replaceForm(response.data)
    showSuccess(`Social link ${action}d.`)
    await load(page.value)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    showError(error.value.message)
  }
  finally { saving.value = false }
}

onMounted(() => { void load() })
</script>

<template>
  <q-page class="q-pa-md q-pa-lg-md">
    <div class="row items-center justify-between q-col-gutter-md q-mb-lg">
      <div class="col">
        <h1 class="text-h5 q-my-none">Social links</h1>
        <p class="text-body2 text-grey-8 q-mb-none">Manage the supported platform code, public URL, and display order.</p>
      </div>
      <div class="col-auto"><q-btn color="primary" label="Create social link" @click="create" /></div>
    </div>

    <q-banner v-if="notification" :class="notification.type === 'success' ? 'bg-green-1 text-positive' : 'bg-red-1 text-negative'" class="q-mb-md" rounded role="status">
      {{ notification.message }}
    </q-banner>
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">
      {{ isVersionConflict(error) ? 'This social link changed elsewhere. Reload it before saving.' : error.message }}
    </q-banner>

    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator class="q-mb-lg">
        <q-item v-for="item in items" :key="item.id" clickable @click="select(item)">
          <q-item-section><q-item-label>{{ item.platformCode }}</q-item-label><q-item-label caption>{{ item.url }}</q-item-label></q-item-section>
          <q-item-section side><q-badge :label="item.active ? 'Active' : 'Inactive'" :color="item.active ? 'positive' : 'grey-7'" /></q-item-section>
        </q-item>
      </q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>

    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="save">
      <h2 class="text-h6 q-my-none">{{ form.id ? 'Edit social link' : 'Create social link' }}</h2>
      <q-input v-model="form.platformCode" label="Platform code" :disable="saving" :error="Boolean(fieldErrors.platformCode)" :error-message="fieldErrors.platformCode" :rules="[(value) => Boolean(value?.trim()) || 'Platform code is required.']" />
      <q-input v-model="form.url" type="url" label="Public URL" hint="Only http and https URLs are supported." :disable="saving" :error="Boolean(fieldErrors.url)" :error-message="fieldErrors.url" :rules="[validHttpUrl]" />
      <q-input v-model.number="form.sortOrder" type="number" min="0" label="Sort order" :disable="saving" :error="Boolean(fieldErrors.sortOrder)" :error-message="fieldErrors.sortOrder" />
      <div class="row q-gutter-sm">
        <q-btn type="submit" color="primary" :loading="saving" :disable="saving" label="Save social link" />
        <AdminActivationActions v-if="form.id" :active="form.active" :saving="saving" :public-preview-path="publicPreviewPath" @activate="transition('activate')" @deactivate="transition('deactivate')" />
      </div>
    </q-form>
  </q-page>
</template>
