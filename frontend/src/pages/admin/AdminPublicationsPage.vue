<script setup>
import { computed, inject, onMounted, ref } from 'vue'

import AdminLifecycleActions from 'src/components/admin/AdminLifecycleActions.vue'
import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const publicationStages = ['PREPRINT', 'ACCEPTED', 'IN_PRESS', 'PUBLISHED']
const items = ref([])
const page = ref(0)
const totalPages = ref(0)
const state = ref('loading')
const error = ref(null)
const saving = ref(false)
const selectedLocale = ref('fa')
const form = ref(createForm())

function translation() {
  return {
    title: '',
    slug: '',
    abstractText: '',
    authorsDisplay: '',
    venueDisplay: '',
    seoTitle: '',
    seoDescription: ''
  }
}

function createForm(value = {}) {
  return {
    id: value.id ?? null,
    publicationKey: value.publicationKey ?? '',
    publicationStage: value.publicationStage ?? 'PUBLISHED',
    status: value.status ?? 'DRAFT',
    doi: value.doi ?? '',
    externalUrl: value.externalUrl ?? '',
    publishedOn: value.publishedOn ?? '',
    year: value.year ?? new Date().getFullYear(),
    coverMediaId: value.coverMediaId ?? '',
    sortOrder: value.sortOrder ?? 0,
    version: value.version ?? null,
    fa: { ...translation(), ...(value.fa ?? {}) },
    en: { ...translation(), ...(value.en ?? {}) }
  }
}

const activeTranslation = computed(() => form.value[selectedLocale.value])
const translations = computed(() => ({
  fa: Boolean(form.value.fa?.title && form.value.fa?.slug),
  en: Boolean(form.value.en?.title && form.value.en?.slug)
}))
const publicPreviewPath = computed(() => {
  if (form.value.status !== 'PUBLISHED') {
    return null
  }

  const slug = activeTranslation.value.slug?.trim()
  return slug ? `/${selectedLocale.value}/publications/${slug}` : null
})

async function load(requestedPage = page.value) {
  state.value = 'loading'
  error.value = null
  try {
    const response = await httpClient.get('/api/v1/admin/publications', {
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
  try {
    const response = await httpClient.get(`/api/v1/admin/publications/${item.id}`)
    form.value = createForm(response.data)
    selectedLocale.value = 'fa'
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
}

function create() {
  form.value = createForm()
  selectedLocale.value = 'fa'
  error.value = null
}

function payload() {
  return {
    publicationKey: form.value.publicationKey,
    publicationStage: form.value.publicationStage,
    doi: form.value.doi,
    externalUrl: form.value.externalUrl,
    publishedOn: form.value.publishedOn || null,
    year: Number(form.value.year),
    coverMediaId: form.value.coverMediaId || null,
    sortOrder: Number(form.value.sortOrder),
    fa: form.value.fa,
    en: form.value.en,
    version: form.value.version
  }
}

async function save() {
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = form.value.id
      ? await httpClient.put(`/api/v1/admin/publications/${form.value.id}`, payload())
      : await httpClient.post('/api/v1/admin/publications', payload())
    form.value = createForm(response.data)
    await load(page.value)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    saving.value = false
  }
}

async function transition(action) {
  if (!form.value.id) {
    return
  }

  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.post(
      `/api/v1/admin/publications/${form.value.id}/${action}`,
      null,
      { params: { version: form.value.version } }
    )
    form.value = createForm(response.data)
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
    <div class="row items-center justify-between q-col-gutter-md q-mb-lg">
      <div class="col">
        <h1 class="text-h5 q-my-none">Publications</h1>
        <p class="text-body2 text-grey-8 q-mb-none">Manage academic bibliographic records and translations.</p>
      </div>
      <div class="col-auto"><q-btn color="primary" label="Create publication" @click="create" /></div>
    </div>
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">{{ error.message }}</q-banner>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator class="q-mb-lg">
        <q-item v-for="item in items" :key="item.id" clickable @click="select(item)">
          <q-item-section>
            <q-item-label>{{ item.publicationKey }} · {{ item.fa?.title || 'Missing translation' }}</q-item-label>
            <q-item-label caption>{{ item.publicationStage }} · {{ item.year }}</q-item-label>
          </q-item-section>
          <q-item-section side><q-badge :label="item.status" :color="item.status === 'PUBLISHED' ? 'positive' : 'grey-7'" /></q-item-section>
        </q-item>
      </q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>

    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="save">
      <h2 class="text-h6 q-my-none">{{ form.id ? 'Edit publication' : 'Create publication' }}</h2>
      <q-input v-model="form.publicationKey" label="Publication key" :disable="saving" />
      <q-select v-model="form.publicationStage" :options="publicationStages" label="Publication stage" :disable="saving" />
      <q-input v-model="form.doi" label="DOI" :disable="saving" />
      <q-input v-model="form.externalUrl" type="url" label="External URL" :disable="saving" />
      <q-input v-model="form.publishedOn" type="date" label="Publication date" :disable="saving" />
      <q-input v-model.number="form.year" type="number" min="1000" max="9999" label="Year" :disable="saving" />
      <q-input v-model="form.coverMediaId" label="Cover media asset ID" :disable="saving" />
      <q-input v-model.number="form.sortOrder" type="number" min="0" label="Sort order" :disable="saving" />
      <AdminLocaleTabs v-model="selectedLocale" :translations="translations" />
      <q-input v-model="activeTranslation.title" label="Title" :disable="saving" />
      <q-input v-model="activeTranslation.slug" label="Slug" :disable="saving" />
      <q-input v-model="activeTranslation.authorsDisplay" type="textarea" label="Authors" :disable="saving" />
      <q-input v-model="activeTranslation.venueDisplay" type="textarea" label="Venue" :disable="saving" />
      <q-input v-model="activeTranslation.abstractText" type="textarea" label="Abstract" :disable="saving" />
      <q-input v-model="activeTranslation.seoTitle" label="SEO title" :disable="saving" />
      <q-input v-model="activeTranslation.seoDescription" type="textarea" label="SEO description" :disable="saving" />
      <div class="row q-gutter-sm">
        <q-btn type="submit" color="primary" :loading="saving" label="Save publication" />
        <AdminLifecycleActions v-if="form.id" :status="form.status" :saving="saving" :public-preview-path="publicPreviewPath" @publish="transition('publish')" @archive="transition('archive')" />
      </div>
    </q-form>
  </q-page>
</template>
