<script setup>
import { computed, inject, onMounted, ref } from 'vue'

import AdminLifecycleActions from 'src/components/admin/AdminLifecycleActions.vue'
import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminMediaSelector from 'src/components/admin/AdminMediaSelector.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const entryTypes = ['EDUCATION', 'EXPERIENCE', 'RESEARCH', 'AWARD', 'CERTIFICATION']
const entries = ref([])
const documents = ref([])
const page = ref(0)
const totalPages = ref(0)
const state = ref('loading')
const error = ref(null)
const saving = ref(false)
const selectedLocale = ref('fa')
const entryForm = ref(createEntry())
const documentForm = ref(createDocument())

function entryTranslation() {
  return { title: '', organization: '', location: '', summary: '' }
}

function createEntry(value = {}) {
  return {
    id: value.id ?? null,
    entryType: value.entryType ?? 'EXPERIENCE',
    status: value.status ?? 'DRAFT',
    startedOn: value.startedOn ?? '',
    endedOn: value.endedOn ?? '',
    current: value.current ?? false,
    sortOrder: value.sortOrder ?? 0,
    version: value.version ?? null,
    fa: { ...entryTranslation(), ...(value.fa ?? {}) },
    en: { ...entryTranslation(), ...(value.en ?? {}) }
  }
}

function createDocument(value = {}) {
  return {
    id: value.id ?? null,
    languageCode: value.languageCode ?? 'fa',
    mediaAssetId: value.mediaAssetId ?? '',
    status: value.status ?? 'DRAFT',
    version: value.version ?? null
  }
}

const activeTranslation = computed(() => entryForm.value[selectedLocale.value])
const translations = computed(() => ({
  fa: Boolean(entryForm.value.fa?.title && entryForm.value.fa?.organization),
  en: Boolean(entryForm.value.en?.title && entryForm.value.en?.organization)
}))
const entryPreviewPath = computed(() => (
  entryForm.value.status === 'PUBLISHED' ? `/${selectedLocale.value}/resume` : null
))
const documentPreviewPath = computed(() => (
  documentForm.value.status === 'PUBLISHED' ? `/${documentForm.value.languageCode}/resume` : null
))

async function load(requestedPage = page.value) {
  state.value = 'loading'
  error.value = null

  try {
    const [entryResponse, documentResponse] = await Promise.all([
      httpClient.get('/api/v1/admin/resume/entries', {
        params: { page: requestedPage, size: 20, sort: 'sortOrder,asc' }
      }),
      httpClient.get('/api/v1/admin/resume/documents', { params: { page: 0, size: 20 } })
    ])
    entries.value = entryResponse.data.items ?? []
    documents.value = documentResponse.data.items ?? []
    page.value = entryResponse.data.page ?? requestedPage
    totalPages.value = entryResponse.data.totalPages ?? 0
    state.value = entries.value.length === 0 && documents.value.length === 0 ? 'empty' : 'ready'
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    state.value = 'error'
  }
}

async function selectEntry(item) {
  try {
    const response = await httpClient.get(`/api/v1/admin/resume/entries/${item.id}`)
    entryForm.value = createEntry(response.data)
    selectedLocale.value = 'fa'
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
}

async function selectDocument(item) {
  try {
    const response = await httpClient.get(`/api/v1/admin/resume/documents/${item.id}`)
    documentForm.value = createDocument(response.data)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
}

function entryPayload() {
  return {
    entryType: entryForm.value.entryType,
    startedOn: entryForm.value.startedOn,
    endedOn: entryForm.value.endedOn || null,
    current: entryForm.value.current,
    sortOrder: Number(entryForm.value.sortOrder),
    fa: entryForm.value.fa,
    en: entryForm.value.en,
    version: entryForm.value.version
  }
}

function documentPayload() {
  return {
    languageCode: documentForm.value.languageCode,
    mediaAssetId: documentForm.value.mediaAssetId,
    version: documentForm.value.version
  }
}

async function saveEntry() {
  saving.value = true
  error.value = null

  try {
    await primeCsrfToken(httpClient)
    const response = entryForm.value.id
      ? await httpClient.put(`/api/v1/admin/resume/entries/${entryForm.value.id}`, entryPayload())
      : await httpClient.post('/api/v1/admin/resume/entries', entryPayload())
    entryForm.value = createEntry(response.data)
    await load(page.value)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    saving.value = false
  }
}

async function saveDocument() {
  saving.value = true
  error.value = null

  try {
    await primeCsrfToken(httpClient)
    const response = documentForm.value.id
      ? await httpClient.put(`/api/v1/admin/resume/documents/${documentForm.value.id}`, documentPayload())
      : await httpClient.post('/api/v1/admin/resume/documents', documentPayload())
    documentForm.value = createDocument(response.data)
    await load(page.value)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    saving.value = false
  }
}

async function transition(resource, action) {
  const current = resource === 'entries' ? entryForm.value : documentForm.value
  if (!current.id) {
    return
  }

  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.post(
      `/api/v1/admin/resume/${resource}/${current.id}/${action}`,
      null,
      { params: { version: current.version } }
    )
    if (resource === 'entries') {
      entryForm.value = createEntry(response.data)
    }
    else {
      documentForm.value = createDocument(response.data)
    }
    await load(page.value)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    saving.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <q-page class="q-pa-md q-pa-lg-md">
    <h1 class="text-h5 q-mt-none">Resume</h1>
    <p class="text-body2 text-grey-8">Manage entries and the supported published document for each locale.</p>
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">{{ error.message }}</q-banner>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator class="q-mb-md">
        <q-item v-for="item in entries" :key="item.id" clickable @click="selectEntry(item)">
          <q-item-section><q-item-label>{{ item.entryType }} · {{ item.fa?.title || 'Missing translation' }}</q-item-label></q-item-section>
          <q-item-section side><q-badge :label="item.status" :color="item.status === 'PUBLISHED' ? 'positive' : 'grey-7'" /></q-item-section>
        </q-item>
      </q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>

    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="saveEntry">
      <h2 class="text-h6 q-my-none">{{ entryForm.id ? 'Edit entry' : 'Create entry' }}</h2>
      <q-select v-model="entryForm.entryType" :options="entryTypes" label="Entry type" :disable="saving" />
      <q-input v-model="entryForm.startedOn" type="date" label="Start date" :disable="saving" />
      <q-input v-model="entryForm.endedOn" type="date" label="End date" :disable="saving || entryForm.current" />
      <q-checkbox v-model="entryForm.current" label="Current" :disable="saving" />
      <q-input v-model.number="entryForm.sortOrder" type="number" min="0" label="Sort order" :disable="saving" />
      <AdminLocaleTabs v-model="selectedLocale" :translations="translations" />
      <q-input v-model="activeTranslation.title" label="Title" :disable="saving" />
      <q-input v-model="activeTranslation.organization" label="Organization" :disable="saving" />
      <q-input v-model="activeTranslation.location" label="Location" :disable="saving" />
      <q-input v-model="activeTranslation.summary" type="textarea" label="Description" :disable="saving" />
      <div class="row q-gutter-sm">
        <q-btn type="submit" color="primary" :loading="saving" label="Save entry" />
        <AdminLifecycleActions v-if="entryForm.id" :status="entryForm.status" :saving="saving" :public-preview-path="entryPreviewPath" @publish="transition('entries', 'publish')" @archive="transition('entries', 'archive')" />
      </div>
    </q-form>

    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="saveDocument">
      <h2 class="text-h6 q-my-none">Resume document</h2>
      <q-list bordered separator>
        <q-item v-for="item in documents" :key="item.id" clickable @click="selectDocument(item)">
          <q-item-section><q-item-label>{{ item.languageCode }} · {{ item.mediaAssetId }}</q-item-label></q-item-section>
          <q-item-section side><q-badge :label="item.status" :color="item.status === 'PUBLISHED' ? 'positive' : 'grey-7'" /></q-item-section>
        </q-item>
      </q-list>
      <q-select v-model="documentForm.languageCode" :options="['fa', 'en']" label="Locale" :disable="saving" />
      <AdminMediaSelector v-model="documentForm.mediaAssetId" label="Resume document media" :disable="saving" />
      <div class="row q-gutter-sm">
        <q-btn type="submit" color="primary" :loading="saving" label="Save document" />
        <AdminLifecycleActions v-if="documentForm.id" :status="documentForm.status" :saving="saving" :public-preview-path="documentPreviewPath" @publish="transition('documents', 'publish')" @archive="transition('documents', 'archive')" />
      </div>
    </q-form>
  </q-page>
</template>
