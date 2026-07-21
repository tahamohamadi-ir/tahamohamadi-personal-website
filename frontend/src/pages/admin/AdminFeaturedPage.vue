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
const publications = ref([])
const projects = ref([])
const page = ref(0)
const totalPages = ref(0)
const state = ref('loading')
const error = ref(null)
const saving = ref(false)
const replacingForm = ref(false)
const form = ref(createForm())
const changes = createUnsavedChangesGuard(() => Promise.resolve(
  window.confirm('Discard unsaved featured-content changes?')
))
const { notification, showSuccess, showError } = useAdminNotifications()

function createForm(value = {}) {
  return {
    id: value.id ?? null,
    slotKey: value.slotKey ?? 'home',
    targetType: value.targetType ?? 'PORTFOLIO_PROJECT',
    targetId: value.targetId ?? null,
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
const publicPreviewPath = computed(() => (
  form.value.active && form.value.slotKey === 'home' ? '/en' : null
))
const targetOptions = computed(() => {
  const entries = form.value.targetType === 'PUBLICATION' ? publications.value : projects.value
  return entries
    .filter((entry) => entry.status === 'PUBLISHED')
    .map((entry) => ({
      value: entry.id,
      label: form.value.targetType === 'PUBLICATION'
        ? `${entry.publicationKey} · FA: ${entry.fa?.title || 'Missing translation'} · EN: ${entry.en?.title || 'Missing translation'}`
        : `${entry.projectKey} · FA: ${entry.fa?.title || 'Missing translation'} · EN: ${entry.en?.title || 'Missing translation'}`
    }))
})

watch(form, () => {
  if (!replacingForm.value) changes.markDirty()
}, { deep: true, flush: 'sync' })

watch(() => form.value.targetType, () => {
  if (!replacingForm.value) form.value.targetId = null
})

onBeforeRouteLeave(async () => changes.confirmLeave())

async function loadAll(endpoint) {
  const first = await httpClient.get(endpoint, {
    params: { page: 0, size: 100, sort: 'updatedAt,desc' }
  })
  const pages = first.data.totalPages ?? 0
  if (pages <= 1) return first.data.items ?? []

  const remaining = await Promise.all(
    Array.from({ length: pages - 1 }, (_, index) => httpClient.get(endpoint, {
      params: { page: index + 1, size: 100, sort: 'updatedAt,desc' }
    }))
  )
  return [
    ...(first.data.items ?? []),
    ...remaining.flatMap((response) => response.data.items ?? [])
  ]
}

async function load(requestedPage = page.value) {
  state.value = 'loading'
  error.value = null
  try {
    const [featuredItems, publicationItems, projectItems] = await Promise.all([
      httpClient.get('/api/v1/admin/featured-items', {
        params: { page: requestedPage, size: 20, sort: 'updatedAt,desc' }
      }),
      loadAll('/api/v1/admin/publications'),
      loadAll('/api/v1/admin/portfolio/projects')
    ])
    items.value = featuredItems.data.items ?? []
    publications.value = publicationItems
    projects.value = projectItems
    page.value = featuredItems.data.page ?? requestedPage
    totalPages.value = featuredItems.data.totalPages ?? 0
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
    const response = await httpClient.get(`/api/v1/admin/featured-items/${item.id}`)
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
    slotKey: form.value.slotKey,
    targetType: form.value.targetType,
    targetId: form.value.targetId,
    sortOrder: Number(form.value.sortOrder),
    startsAt: null,
    endsAt: null,
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
      ? await httpClient.put(`/api/v1/admin/featured-items/${form.value.id}`, payload())
      : await httpClient.post('/api/v1/admin/featured-items', payload())
    replaceForm(response.data)
    showSuccess('Featured content saved.')
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
      `/api/v1/admin/featured-items/${form.value.id}/${action}`,
      null,
      { params: { version: form.value.version } }
    )
    replaceForm(response.data)
    showSuccess(`Featured content ${action}d.`)
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
        <h1 class="text-h5 q-my-none">Featured content</h1>
        <p class="text-body2 text-grey-8 q-mb-none">Feature published publications or portfolio projects on the public Home page.</p>
      </div>
      <div class="col-auto"><q-btn color="primary" label="Create featured content" @click="create" /></div>
    </div>

    <q-banner v-if="notification" :class="notification.type === 'success' ? 'bg-green-1 text-positive' : 'bg-red-1 text-negative'" class="q-mb-md" rounded role="status">
      {{ notification.message }}
    </q-banner>
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">
      {{ isVersionConflict(error) ? 'This featured item changed elsewhere. Reload it before saving.' : error.message }}
    </q-banner>

    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator class="q-mb-lg">
        <q-item v-for="item in items" :key="item.id" clickable @click="select(item)">
          <q-item-section><q-item-label>{{ item.targetType }}</q-item-label><q-item-label caption>Home order {{ item.sortOrder }}</q-item-label></q-item-section>
          <q-item-section side><q-badge :label="item.active ? 'Active' : 'Inactive'" :color="item.active ? 'positive' : 'grey-7'" /></q-item-section>
        </q-item>
      </q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>

    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="save">
      <h2 class="text-h6 q-my-none">{{ form.id ? 'Edit featured content' : 'Create featured content' }}</h2>
      <q-input v-model="form.slotKey" label="Public placement" readonly hint="Featured content is shown in the Home placement." :disable="saving" :error="Boolean(fieldErrors.slotKey)" :error-message="fieldErrors.slotKey" />
      <q-select v-model="form.targetType" :options="[{ label: 'Publication', value: 'PUBLICATION' }, { label: 'Portfolio project', value: 'PORTFOLIO_PROJECT' }]" option-label="label" option-value="value" emit-value map-options label="Target type" :disable="saving" :error="Boolean(fieldErrors.targetType)" :error-message="fieldErrors.targetType" />
      <q-select v-model="form.targetId" :options="targetOptions" option-label="label" option-value="value" emit-value map-options label="Published target" :disable="saving" :error="Boolean(fieldErrors.targetId)" :error-message="fieldErrors.targetId" :rules="[(value) => Boolean(value) || 'Select a published target.']" />
      <q-input v-model.number="form.sortOrder" type="number" min="0" label="Sort order" :disable="saving" :error="Boolean(fieldErrors.sortOrder)" :error-message="fieldErrors.sortOrder" />
      <div class="row q-gutter-sm">
        <q-btn type="submit" color="primary" :loading="saving" :disable="saving" label="Save featured content" />
        <AdminActivationActions v-if="form.id" :active="form.active" :saving="saving" :public-preview-path="publicPreviewPath" @activate="transition('activate')" @deactivate="transition('deactivate')" />
      </div>
    </q-form>
  </q-page>
</template>
