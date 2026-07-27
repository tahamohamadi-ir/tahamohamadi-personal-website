<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { onBeforeRouteLeave } from 'vue-router'

import AdminLifecycleActions from 'src/components/admin/AdminLifecycleActions.vue'
import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminMarkdownPreview from 'src/components/admin/AdminMarkdownPreview.vue'
import AdminPageBlockComposer from 'src/components/admin/AdminPageBlockComposer.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import { createUnsavedChangesGuard, isVersionConflict, mapValidationErrors } from 'src/composables/adminContentInteractions'
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
const selectedLocale = ref('fa')
const form = ref(createForm())
const replacing = ref(false)
const fieldErrors = ref({})
const revisions = ref([])
const revisionsLoading = ref(false)
const revisionDetail = ref(null)
const revisionDialog = ref(false)
const restoreCandidate = ref(null)
const restoreConfirmationOpen = ref(false)
const changes = createUnsavedChangesGuard(() => Promise.resolve(window.confirm(t('admin.unsaved.discard'))))

function translation() {
  return {
    title: '',
    slug: '',
    summary: '',
    bodyMarkdown: '',
    seoTitle: '',
    seoDescription: '',
    canonicalPath: ''
  }
}

function createForm(value = {}) {
  return {
    id: value.id ?? null,
    pageKey: value.pageKey ?? '',
    status: value.status ?? 'DRAFT',
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

  const locale = selectedLocale.value
  const pageKey = form.value.pageKey?.trim().toLowerCase()
  const staticPaths = { home: `/${locale}`, about: `/${locale}/about`, research: `/${locale}/research` }
  if (staticPaths[pageKey]) return staticPaths[pageKey]
  const slug = activeTranslation.value.slug?.trim()
  return slug ? `/${locale}/pages/${slug}` : null
})

function replaceForm(value) {
  replacing.value = true
  form.value = createForm(value)
  fieldErrors.value = {}
  changes.markSaved()
  queueMicrotask(() => { replacing.value = false })
}

async function load(requestedPage = page.value) {
  state.value = 'loading'
  error.value = null

  try {
    const response = await httpClient.get('/api/v1/admin/pages', {
      params: { page: requestedPage, size: 20 }
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
  error.value = null

  try {
    const response = await httpClient.get(`/api/v1/admin/pages/${item.id}`)
    replaceForm(response.data)
    selectedLocale.value = 'fa'
    await loadRevisions(response.data.id)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
}

function create() {
  replaceForm()
  selectedLocale.value = 'fa'
  error.value = null
  revisions.value = []
}

function payload() {
  return {
    pageKey: form.value.pageKey,
    fa: form.value.fa,
    en: form.value.en,
    version: form.value.version
  }
}

function compositionSaved(version) {
  const wasDirty = changes.isDirty.value
  replacing.value = true
  form.value.version = version
  if (!wasDirty) changes.markSaved()
  queueMicrotask(() => { replacing.value = false })
}

async function save() {
  saving.value = true
  error.value = null
  fieldErrors.value = {}

  try {
    await primeCsrfToken(httpClient)
    const response = form.value.id
      ? await httpClient.put(`/api/v1/admin/pages/${form.value.id}`, payload())
      : await httpClient.post('/api/v1/admin/pages', payload())

    replaceForm(response.data)
    await load(page.value)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    fieldErrors.value = mapValidationErrors(error.value)
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
      `/api/v1/admin/pages/${form.value.id}/${action}`,
      null,
      { params: { version: form.value.version } }
    )
    replaceForm(response.data)
    await load(page.value)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    saving.value = false
  }
}

async function loadRevisions(id = form.value.id) {
  if (!id) { revisions.value = []; return }
  revisionsLoading.value = true
  try {
    revisions.value = (await httpClient.get(`/api/v1/admin/pages/${id}/revisions`)).data ?? []
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { revisionsLoading.value = false }
}

async function viewRevision(revision) {
  if (!form.value.id) return
  revisionsLoading.value = true
  error.value = null
  try {
    revisionDetail.value = (await httpClient.get(`/api/v1/admin/pages/${form.value.id}/revisions/${revision.id}`)).data
    revisionDialog.value = true
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { revisionsLoading.value = false }
}

function requestRestore(revision) {
  restoreCandidate.value = revision
  restoreConfirmationOpen.value = true
}

async function restoreRevision() {
  const revision = restoreCandidate.value
  if (!revision || !form.value.id || form.value.version == null) return
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.post(`/api/v1/admin/pages/${form.value.id}/revisions/${revision.id}/restore-as-draft`, null, { params: { version: form.value.version } })
    restoreConfirmationOpen.value = false
    restoreCandidate.value = null
    replaceForm(response.data)
    selectedLocale.value = 'fa'
    await load(page.value)
    await loadRevisions(response.data.id)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

onMounted(() => {
  void load()
})

watch(form, () => {
  if (!replacing.value) changes.markDirty()
}, { deep: true, flush: 'sync' })
onBeforeRouteLeave(async () => changes.confirmLeave())
</script>

<template>
  <q-page class="admin-page admin-pages">
    <header class="admin-page__header">
      <div><h1 class="text-h4 q-my-none">{{ t('admin.pages.title') }}</h1><p class="admin-page__description">{{ t('admin.pages.description') }}</p></div>
      <q-btn color="primary" no-caps icon="add" :label="t('admin.pages.create')" @click="create" />
    </header>

    <q-banner v-if="error" class="bg-red-1 text-negative" rounded role="alert">
      {{ error.message }}
      <q-btn v-if="isVersionConflict(error) && form.id" flat color="negative" :label="t('admin.pages.reload')" @click="select(form)" />
    </q-banner>

    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator class="admin-pages__list">
        <q-item v-for="item in items" :key="item.id" clickable @click="select(item)">
          <q-item-section>
            <q-item-label>{{ item.pageKey }}</q-item-label>
            <q-item-label caption class="admin-pages__translation-summary">
              <span>{{ item.fa?.title || t('admin.pages.missingTranslation') }}</span>
              <span>{{ item.en?.title || t('admin.pages.missingTranslation') }}</span>
            </q-item-label>
          </q-item-section>
          <q-item-section side>
            <q-badge :label="item.status" :color="item.status === 'PUBLISHED' ? 'positive' : 'grey-7'" />
          </q-item-section>
        </q-item>
      </q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>

    <q-form class="admin-pages__form" @submit.prevent="save">
      <section class="admin-panel admin-pages__panel">
        <div class="admin-pages__panel-header"><h2 class="text-h6 q-my-none">{{ form.id ? t('admin.pages.edit') : t('admin.pages.create') }}</h2><AdminLocaleTabs v-model="selectedLocale" :translations="translations" /></div>
        <q-input v-model="form.pageKey" outlined :label="t('admin.pages.pageKey')" :error="Boolean(fieldErrors.pageKey)" :error-message="fieldErrors.pageKey" :disable="saving" />
      </section>
      <section v-if="form.id" class="admin-panel admin-pages__panel admin-pages__revisions" :aria-label="t('admin.pages.revisions')">
        <div class="admin-pages__panel-header">
          <div><h2 class="text-h6 q-my-none">{{ t('admin.pages.revisions') }}</h2><p class="text-caption q-mb-none">{{ t('admin.pages.revisionHelp') }}</p></div>
          <q-btn flat no-caps icon="refresh" :loading="revisionsLoading" :label="t('admin.pages.refreshRevisions')" @click="loadRevisions()" />
        </div>
        <q-list v-if="revisions.length" bordered separator>
          <q-item v-for="revision in revisions" :key="revision.id">
            <q-item-section clickable @click="viewRevision(revision)">
              <q-item-label>{{ t('admin.pages.revision', { number: revision.revisionNumber }) }}</q-item-label>
              <q-item-label caption><time :datetime="revision.createdAt">{{ revision.createdAt }}</time> — {{ revision.reason }}</q-item-label>
            </q-item-section>
            <q-item-section side><q-btn outline no-caps icon="restore" :disable="saving" :label="t('admin.pages.restoreAsDraft')" @click="requestRestore(revision)" /></q-item-section>
          </q-item>
        </q-list>
        <p v-else-if="!revisionsLoading" class="text-caption q-mb-none">{{ t('admin.pages.noRevisions') }}</p>
      </section>
      <section class="admin-panel admin-pages__panel">
        <div class="admin-pages__fields">
          <q-input v-model="activeTranslation.title" outlined :label="t('admin.pages.titleField')" :error="Boolean(fieldErrors[`${selectedLocale}.title`])" :error-message="fieldErrors[`${selectedLocale}.title`]" :disable="saving" />
          <q-input v-model="activeTranslation.slug" outlined :label="t('admin.pages.slug')" :error="Boolean(fieldErrors[`${selectedLocale}.slug`])" :error-message="fieldErrors[`${selectedLocale}.slug`]" :disable="saving" />
          <q-input v-model="activeTranslation.summary" outlined type="textarea" :rows="3" :label="t('admin.pages.summary')" :disable="saving" />
        </div>
        <AdminMarkdownPreview v-model="activeTranslation.bodyMarkdown" />
      </section>
      <section class="admin-panel admin-pages__panel">
        <div class="admin-pages__fields">
          <q-input v-model="activeTranslation.seoTitle" outlined :label="t('admin.pages.seoTitle')" :disable="saving" />
          <q-input v-model="activeTranslation.seoDescription" outlined type="textarea" :rows="3" :label="t('admin.pages.seoDescription')" :disable="saving" />
          <q-input v-model="activeTranslation.canonicalPath" outlined :label="t('admin.pages.canonicalPath')" :disable="saving" />
        </div>
      </section>
      <section class="admin-panel admin-pages__panel">
        <AdminPageBlockComposer
          :page-id="form.id"
          :page-version="form.version"
          :page-status="form.status"
          :disable="saving"
          @saved="compositionSaved"
        />
      </section>
      <footer class="admin-pages__actions">
        <span class="text-caption">{{ changes.isDirty ? t('admin.siteSettings.unsaved') : t('admin.siteSettings.saved') }}</span>
        <div class="admin-pages__actions-buttons"><q-btn type="submit" color="primary" no-caps icon="save" :loading="saving" :label="t('admin.pages.save')" />
        <AdminLifecycleActions
          v-if="form.id"
          :status="form.status"
          :saving="saving"
          :public-preview-path="publicPreviewPath"
          @publish="transition('publish')"
          @archive="transition('archive')"
        />
        </div>
      </footer>
    </q-form>
    <q-dialog v-model="revisionDialog">
      <q-card class="admin-page-revision-dialog">
        <q-card-section class="row items-center q-pb-none"><h2 class="text-h6 q-my-none">{{ t('admin.pages.revisionCompare') }}</h2><q-space /><q-btn flat round icon="close" :aria-label="t('admin.actions.cancel')" v-close-popup /></q-card-section>
        <q-card-section v-if="revisionDetail?.snapshot" class="q-gutter-md">
          <p class="text-caption q-mb-none">{{ t('admin.pages.revision', { number: revisionDetail.revisionNumber }) }} — {{ revisionDetail.reason }}</p>
          <div v-for="locale in ['fa', 'en']" :key="locale" class="admin-page-revision-dialog__locale">
            <h3 class="text-subtitle2 q-my-none">{{ locale.toUpperCase() }}</h3>
            <dl><dt>{{ t('admin.pages.titleField') }}</dt><dd>{{ revisionDetail.snapshot.page?.[locale]?.title }}</dd><dt>{{ t('admin.pages.slug') }}</dt><dd>{{ revisionDetail.snapshot.page?.[locale]?.slug }}</dd><dt>{{ t('admin.pages.compareCurrent') }}</dt><dd>{{ form[locale]?.title }} — {{ form[locale]?.slug }}</dd></dl>
          </div>
          <p class="text-caption q-mb-none">{{ t('admin.pages.snapshotComposition', { sections: revisionDetail.snapshot.sections?.length ?? 0, blocks: revisionDetail.snapshot.sections?.reduce((count, section) => count + (section.blocks?.length ?? 0), 0) ?? 0 }) }}</p>
        </q-card-section>
      </q-card>
    </q-dialog>
    <q-dialog v-model="restoreConfirmationOpen">
      <q-card class="admin-page-restore-dialog">
        <q-card-section><h2 class="text-h6 q-my-none">{{ t('admin.pages.restoreTitle') }}</h2></q-card-section>
        <q-card-section class="q-pt-none">{{ t('admin.pages.restoreDescription', { number: restoreCandidate?.revisionNumber }) }}</q-card-section>
        <q-card-actions align="right"><q-btn flat no-caps :label="t('admin.actions.cancel')" v-close-popup /><q-btn color="primary" no-caps :loading="saving" :label="t('admin.pages.restoreAsDraft')" @click="restoreRevision" /></q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<style scoped>
.admin-pages__translation-summary {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-2) var(--tm-space-4);
}
.admin-pages__list { margin-block-end: var(--tm-space-4); }
.admin-pages__form { display: grid; gap: var(--tm-admin-panel-gap); max-inline-size: 1040px; }
.admin-pages__panel { display: grid; gap: var(--tm-space-5); padding: var(--tm-space-5); }
.admin-pages__panel-header { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: var(--tm-space-3); }
.admin-pages__fields { display: grid; gap: var(--tm-space-4); grid-template-columns: repeat(2, minmax(0, 1fr)); }
.admin-pages__fields > :nth-child(2n + 1):last-child { grid-column: 1 / -1; }
.admin-pages__actions { position: sticky; inset-block-end: var(--tm-space-3); display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: var(--tm-space-3); padding: var(--tm-space-3) var(--tm-space-4); border: 1px solid var(--tm-admin-border); border-radius: var(--tm-admin-panel-radius); background: var(--tm-admin-surface); }
.admin-pages__actions span { color: var(--tm-text-secondary); }
.admin-pages__actions-buttons { display: flex; flex-wrap: wrap; gap: var(--tm-space-2); }
.admin-page-revision-dialog, .admin-page-restore-dialog { inline-size: min(42rem, calc(100vw - 2 * var(--tm-space-4))); }
.admin-page-revision-dialog__locale { border-block-start: 1px solid var(--tm-admin-border); display: grid; gap: var(--tm-space-2); padding-block-start: var(--tm-space-3); }
.admin-page-revision-dialog dl { display: grid; gap: var(--tm-space-1); margin: 0; }
.admin-page-revision-dialog dt { font-weight: 600; }
.admin-page-revision-dialog dd { margin: 0; overflow-wrap: anywhere; }
@media (max-width: 599px) { .admin-pages__fields { grid-template-columns: 1fr; } .admin-pages__fields > :nth-child(2n + 1):last-child { grid-column: auto; } }
</style>
