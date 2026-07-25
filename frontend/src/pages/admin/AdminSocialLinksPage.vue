<script setup>
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { useI18n } from 'vue-i18n'

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
const { t } = useI18n()
const items = ref([])
const page = ref(0)
const totalPages = ref(0)
const state = ref('loading')
const error = ref(null)
const saving = ref(false)
const replacingForm = ref(false)
const form = ref(createForm())
const changes = createUnsavedChangesGuard(() => Promise.resolve(
  window.confirm(t('admin.socialLinks.discard'))
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
  return /^https?:\/\/.+/.test(value?.trim() ?? '') || t('admin.socialLinks.invalidUrl')
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
    showSuccess(t('admin.socialLinks.saved'))
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
    showSuccess(t(`admin.socialLinks.${action}d`))
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
        <h1 class="text-h5 q-my-none">{{ t('admin.socialLinks.title') }}</h1>
        <p class="text-body2 text-grey-8 q-mb-none">{{ t('admin.socialLinks.description') }}</p>
      </div>
      <div class="col-auto"><q-btn color="primary" :label="t('admin.socialLinks.create')" @click="create" /></div>
    </div>

    <q-banner v-if="notification" :class="notification.type === 'success' ? 'bg-green-1 text-positive' : 'bg-red-1 text-negative'" class="q-mb-md" rounded role="status">
      {{ notification.message }}
    </q-banner>
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">
      {{ isVersionConflict(error) ? t('admin.socialLinks.conflict') : error.message }}
    </q-banner>

    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator class="q-mb-lg">
        <q-item v-for="item in items" :key="item.id" clickable @click="select(item)">
          <q-item-section><q-item-label>{{ item.platformCode }}</q-item-label><q-item-label caption>{{ item.url }}</q-item-label></q-item-section>
          <q-item-section side><q-badge :label="item.active ? t('admin.socialLinks.active') : t('admin.socialLinks.inactive')" :color="item.active ? 'positive' : 'grey-7'" /></q-item-section>
        </q-item>
      </q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>

    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="save">
      <h2 class="text-h6 q-my-none">{{ form.id ? t('admin.socialLinks.edit') : t('admin.socialLinks.create') }}</h2>
      <q-input v-model="form.platformCode" :label="t('admin.socialLinks.platformCode')" :disable="saving" :error="Boolean(fieldErrors.platformCode)" :error-message="fieldErrors.platformCode" :rules="[(value) => Boolean(value?.trim()) || t('admin.socialLinks.platformCodeRequired')]" />
      <q-input v-model="form.url" type="url" :label="t('admin.socialLinks.publicUrl')" :hint="t('admin.socialLinks.urlHint')" :disable="saving" :error="Boolean(fieldErrors.url)" :error-message="fieldErrors.url" :rules="[validHttpUrl]" />
      <q-input v-model.number="form.sortOrder" type="number" min="0" :label="t('admin.socialLinks.sortOrder')" :disable="saving" :error="Boolean(fieldErrors.sortOrder)" :error-message="fieldErrors.sortOrder" />
      <div class="row q-gutter-sm">
        <q-btn type="submit" color="primary" :loading="saving" :disable="saving" :label="t('admin.socialLinks.save')" />
        <AdminActivationActions v-if="form.id" :active="form.active" :saving="saving" :public-preview-path="publicPreviewPath" @activate="transition('activate')" @deactivate="transition('deactivate')" />
      </div>
    </q-form>
  </q-page>
</template>
