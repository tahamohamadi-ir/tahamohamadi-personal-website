<script setup>
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

import AdminLifecycleActions from 'src/components/admin/AdminLifecycleActions.vue'
import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminMarkdownPreview from 'src/components/admin/AdminMarkdownPreview.vue'
import AdminMediaSelector from 'src/components/admin/AdminMediaSelector.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import {
  createUnsavedChangesGuard,
  isVersionConflict,
  mapValidationErrors
} from 'src/composables/adminContentInteractions'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const items = ref([])
const skills = ref([])
const page = ref(0)
const totalPages = ref(0)
const state = ref('loading')
const error = ref(null)
const saving = ref(false)
const selectedLocale = ref('fa')
const replacingForm = ref(false)
const changes = createUnsavedChangesGuard(() => Promise.resolve(
  window.confirm('Discard unsaved portfolio changes?')
))
const form = ref(createForm())

function translation() {
  return {
    title: '', slug: '', summary: '', bodyMarkdown: '', seoTitle: '', seoDescription: ''
  }
}

function createForm(value = {}) {
  return {
    id: value.id ?? null,
    projectKey: value.projectKey ?? '',
    coverMediaId: value.coverMediaId ?? null,
    status: value.status ?? 'DRAFT',
    startedOn: value.startedOn ?? '',
    endedOn: value.endedOn ?? '',
    projectUrl: value.projectUrl ?? '',
    repositoryUrl: value.repositoryUrl ?? '',
    sortOrder: value.sortOrder ?? 0,
    skills: value.skills ?? [],
    version: value.version ?? null,
    fa: { ...translation(), ...(value.fa ?? {}) },
    en: { ...translation(), ...(value.en ?? {}) }
  }
}

function replaceForm(value = {}) {
  replacingForm.value = true
  form.value = createForm(value)
  changes.markSaved()
  nextTick(() => { replacingForm.value = false })
}

const activeTranslation = computed(() => form.value[selectedLocale.value])
const translations = computed(() => ({
  fa: Boolean(form.value.fa?.title && form.value.fa?.slug),
  en: Boolean(form.value.en?.title && form.value.en?.slug)
}))
const fieldErrors = computed(() => mapValidationErrors(error.value))
const skillOptions = computed(() => skills.value
  .filter((skill) => skill.active)
  .map((skill) => ({
    label: `${skill.skillKey} · ${skill[selectedLocale.value]?.name ?? ''}`,
    value: skill.id
  })))
const selectedSkillIds = computed({
  get: () => form.value.skills.map((reference) => reference.skillId),
  set: (values) => {
    form.value.skills = values.map((skillId, sortOrder) => ({ skillId, sortOrder }))
  }
})
const publicPreviewPath = computed(() => {
  if (form.value.status !== 'PUBLISHED') return null
  const slug = activeTranslation.value.slug?.trim()
  return slug ? `/${selectedLocale.value}/portfolio/${slug}` : null
})

watch(form, () => {
  if (!replacingForm.value) changes.markDirty()
}, { deep: true, flush: 'sync' })

onBeforeRouteLeave(async () => changes.confirmLeave())

async function load(requestedPage = page.value) {
  state.value = 'loading'
  error.value = null
  try {
    const [projects, availableSkills] = await Promise.all([
      httpClient.get('/api/v1/admin/portfolio/projects', {
        params: { page: requestedPage, size: 20, sort: 'updatedAt,desc' }
      }),
      httpClient.get('/api/v1/admin/skills', {
        params: { page: 0, size: 100, sort: 'sortOrder,asc' }
      })
    ])
    items.value = projects.data.items ?? []
    skills.value = availableSkills.data.items ?? []
    page.value = projects.data.page ?? requestedPage
    totalPages.value = projects.data.totalPages ?? 0
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
    const response = await httpClient.get(`/api/v1/admin/portfolio/projects/${item.id}`)
    replaceForm(response.data)
    selectedLocale.value = 'fa'
  }
  catch (cause) { error.value = normalizeApiError(cause) }
}

async function create() {
  if (!(await changes.confirmLeave())) return
  replaceForm()
  selectedLocale.value = 'fa'
  error.value = null
}

function payload() {
  return {
    projectKey: form.value.projectKey,
    coverMediaId: form.value.coverMediaId,
    startedOn: form.value.startedOn || null,
    endedOn: form.value.endedOn || null,
    projectUrl: form.value.projectUrl || null,
    repositoryUrl: form.value.repositoryUrl || null,
    sortOrder: Number(form.value.sortOrder),
    fa: form.value.fa,
    en: form.value.en,
    skills: form.value.skills,
    version: form.value.version
  }
}

async function save() {
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = form.value.id
      ? await httpClient.put(`/api/v1/admin/portfolio/projects/${form.value.id}`, payload())
      : await httpClient.post('/api/v1/admin/portfolio/projects', payload())
    replaceForm(response.data)
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

async function transition(action) {
  if (!form.value.id) return
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.post(
      `/api/v1/admin/portfolio/projects/${form.value.id}/${action}`,
      null,
      { params: { version: form.value.version } }
    )
    replaceForm(response.data)
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

onMounted(() => { void load() })
</script>

<template>
  <q-page class="q-pa-md q-pa-lg-md">
    <div class="row items-center justify-between q-col-gutter-md q-mb-lg">
      <div class="col"><h1 class="text-h5 q-my-none">Portfolio projects</h1><p class="text-body2 text-grey-8 q-mb-none">Translations stay independent and project media is a supported cover asset.</p></div>
      <div class="col-auto"><q-btn color="primary" label="Create project" @click="create" /></div>
    </div>
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">
      {{ isVersionConflict(error) ? 'This project changed elsewhere. Reload it before saving.' : error.message }}
    </q-banner>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator class="q-mb-lg"><q-item v-for="item in items" :key="item.id" clickable @click="select(item)"><q-item-section><q-item-label>{{ item.projectKey }}</q-item-label><q-item-label caption>{{ item.fa?.title || 'Missing translation' }} · {{ item.en?.title || 'Missing translation' }}</q-item-label></q-item-section><q-item-section side><q-badge :label="item.status" :color="item.status === 'PUBLISHED' ? 'positive' : 'grey-7'" /></q-item-section></q-item></q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>
    <q-form class="q-mt-xl q-gutter-md" @submit.prevent="save">
      <h2 class="text-h6 q-my-none">{{ form.id ? 'Edit project' : 'Create project' }}</h2>
      <q-input v-model="form.projectKey" label="Project key" :disable="saving" :error="Boolean(fieldErrors.projectKey)" :error-message="fieldErrors.projectKey" />
      <q-input v-model="form.startedOn" type="date" label="Start date" :disable="saving" />
      <q-input v-model="form.endedOn" type="date" label="End date" :disable="saving" />
      <q-input v-model="form.projectUrl" type="url" label="Project URL" :disable="saving" />
      <q-input v-model="form.repositoryUrl" type="url" label="Repository URL" :disable="saving" />
      <q-input v-model.number="form.sortOrder" type="number" min="0" label="Sort order" :disable="saving" />
      <AdminMediaSelector v-model="form.coverMediaId" label="Cover media" :disable="saving" />
      <q-select v-model="selectedSkillIds" :options="skillOptions" option-label="label" option-value="value" emit-value map-options multiple use-chips label="Associated skills" :disable="saving" />
      <AdminLocaleTabs v-model="selectedLocale" :translations="translations" />
      <q-input v-model="activeTranslation.title" label="Title" :disable="saving" :error="Boolean(fieldErrors[`${selectedLocale}.title`])" :error-message="fieldErrors[`${selectedLocale}.title`]" />
      <q-input v-model="activeTranslation.slug" label="Slug" :disable="saving" :error="Boolean(fieldErrors[`${selectedLocale}.slug`])" :error-message="fieldErrors[`${selectedLocale}.slug`]" />
      <q-input v-model="activeTranslation.summary" type="textarea" label="Summary" :disable="saving" />
      <AdminMarkdownPreview v-model="activeTranslation.bodyMarkdown" />
      <q-input v-model="activeTranslation.seoTitle" label="SEO title" :disable="saving" />
      <q-input v-model="activeTranslation.seoDescription" type="textarea" label="SEO description" :disable="saving" />
      <div class="row q-gutter-sm"><q-btn type="submit" color="primary" :loading="saving" label="Save project" /><AdminLifecycleActions v-if="form.id" :status="form.status" :saving="saving" :public-preview-path="publicPreviewPath" @publish="transition('publish')" @archive="transition('archive')" /></div>
    </q-form>
  </q-page>
</template>
