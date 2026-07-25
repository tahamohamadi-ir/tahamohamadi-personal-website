<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import { isVersionConflict, mapValidationErrors } from 'src/composables/adminContentInteractions'
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
const deactivateConfirmationOpen = ref(false)
const selectedLocale = ref('en')
const form = ref(createForm())

function translation(value = {}) {
  return {
    name: value.name ?? '', slug: value.slug ?? '', seoTitle: value.seoTitle ?? '',
    seoDescription: value.seoDescription ?? ''
  }
}

function createForm(value = {}) {
  return {
    id: value.id ?? null, tagKey: value.tagKey ?? '', active: value.active ?? true,
    version: value.version ?? null, fa: translation(value.fa), en: translation(value.en)
  }
}

const activeTranslation = computed(() => form.value[selectedLocale.value])
const translations = computed(() => ({
  fa: Boolean(form.value.fa.name && form.value.fa.slug),
  en: Boolean(form.value.en.name && form.value.en.slug)
}))
const fieldErrors = computed(() => mapValidationErrors(error.value))

async function load(requestedPage = page.value) {
  state.value = 'loading'
  error.value = null
  try {
    const response = await httpClient.get('/api/v1/admin/blog/tags', {
      params: { page: requestedPage, size: 20 }
    })
    items.value = response.data.items ?? []
    page.value = response.data.page ?? requestedPage
    totalPages.value = response.data.totalPages ?? 0
    state.value = items.value.length ? 'ready' : 'empty'
  }
  catch (cause) { error.value = normalizeApiError(cause); state.value = 'error' }
}

async function select(item) {
  error.value = null
  try {
    form.value = createForm((await httpClient.get(`/api/v1/admin/blog/tags/${item.id}`)).data)
    selectedLocale.value = 'en'
  }
  catch (cause) { error.value = normalizeApiError(cause) }
}

function create() { form.value = createForm(); selectedLocale.value = 'en'; error.value = null }
function payload() {
  return {
    tagKey: form.value.tagKey, fa: form.value.fa, en: form.value.en,
    ...(form.value.id ? { version: form.value.version } : {})
  }
}

async function save() {
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = form.value.id
      ? await httpClient.put(`/api/v1/admin/blog/tags/${form.value.id}`, payload())
      : await httpClient.post('/api/v1/admin/blog/tags', payload())
    form.value = createForm(response.data)
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

async function deactivate() {
  if (!form.value.id) return
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    await httpClient.delete(`/api/v1/admin/blog/tags/${form.value.id}`, {
      params: { version: form.value.version }
    })
    create()
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

function confirmDeactivate() {
  deactivateConfirmationOpen.value = false
  void deactivate()
}

onMounted(() => { void load() })
</script>

<template>
  <q-page class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1 class="text-h4 q-my-none">{{ t('admin.blogTaxonomy.tags.title') }}</h1>
        <p class="admin-page__description">{{ t('admin.blogTaxonomy.tags.description') }}</p>
      </div>
      <q-btn color="primary" no-caps icon="add" :label="t('admin.blogTaxonomy.tags.create')" @click="create" />
    </header>
    <q-banner v-if="error" class="bg-red-1 text-negative" role="alert">
      {{ isVersionConflict(error) ? t('admin.blogTaxonomy.tags.conflict') : error.message }}
    </q-banner>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator>
        <q-item v-for="item in items" :key="item.id" clickable @click="select(item)">
          <q-item-section>
            <q-item-label>{{ item.tagKey }}</q-item-label>
            <q-item-label caption>{{ item.en?.name || t('admin.localeTabs.missing') }} · {{ item.fa?.name || t('admin.localeTabs.missing') }}</q-item-label>
          </q-item-section>
          <q-item-section side>
            <q-badge :label="item.active ? t('admin.blogTaxonomy.active') : t('admin.blogTaxonomy.inactive')" :color="item.active ? 'positive' : 'grey-7'" />
          </q-item-section>
        </q-item>
      </q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>
    <q-form class="admin-panel q-pa-md q-gutter-md" @submit.prevent="save">
      <div class="admin-page__header">
        <h2 class="text-h5 q-my-none">{{ form.id ? t('admin.blogTaxonomy.tags.edit') : t('admin.blogTaxonomy.tags.new') }}</h2>
        <span class="text-caption">{{ t('admin.blogTaxonomy.version', { version: form.version ?? t('admin.blogTaxonomy.newVersion') }) }}</span>
      </div>
      <q-input v-model="form.tagKey" :label="t('admin.blogTaxonomy.tags.key')" :disable="saving" :error="Boolean(fieldErrors.tagKey)" :error-message="fieldErrors.tagKey" />
      <AdminLocaleTabs v-model="selectedLocale" :translations="translations" />
      <q-input v-model="activeTranslation.name" :label="t('admin.blogTaxonomy.name')" :disable="saving" />
      <q-input v-model="activeTranslation.slug" :label="t('admin.blogTaxonomy.slug')" :disable="saving" />
      <q-expansion-item :label="t('admin.blogTaxonomy.seoMetadata')">
        <div class="q-pa-sm q-gutter-md">
          <q-input v-model="activeTranslation.seoTitle" :label="t('admin.blogTaxonomy.seoTitle')" :disable="saving" />
          <q-input v-model="activeTranslation.seoDescription" type="textarea" :label="t('admin.blogTaxonomy.seoDescription')" :disable="saving" />
        </div>
      </q-expansion-item>
      <div class="admin-form-actions">
        <q-btn type="submit" color="primary" no-caps :loading="saving" :label="t('admin.blogTaxonomy.tags.save')" />
        <q-btn v-if="form.id && form.active" outline color="negative" no-caps :loading="saving" :label="t('admin.actions.deactivate')" @click="deactivateConfirmationOpen = true" />
      </div>
    </q-form>
    <q-dialog v-model="deactivateConfirmationOpen" persistent>
      <q-card>
        <q-card-section class="text-h6">{{ t('admin.actions.deactivateConfirmTitle') }}</q-card-section>
        <q-card-section>{{ t('admin.actions.deactivateConfirmDescription') }}</q-card-section>
        <q-card-actions align="right">
          <q-btn flat :label="t('admin.actions.cancel')" @click="deactivateConfirmationOpen = false" />
          <q-btn color="negative" :label="t('admin.actions.deactivate')" :loading="saving" @click="confirmDeactivate" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>
