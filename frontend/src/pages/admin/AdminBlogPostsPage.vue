<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import AdminLifecycleActions from 'src/components/admin/AdminLifecycleActions.vue'
import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import ArticleBlockEditor from 'src/components/admin/ArticleBlockEditor.vue'
import AdminMarkdownPreview from 'src/components/admin/AdminMarkdownPreview.vue'
import AdminMediaSelector from 'src/components/admin/AdminMediaSelector.vue'
import AdminPaginatedTable from 'src/components/admin/AdminPaginatedTable.vue'
import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import {
  isVersionConflict,
  mapValidationErrors
} from 'src/composables/adminContentInteractions'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()
const items = ref([])
const categories = ref([])
const tags = ref([])
const state = ref('loading')
const page = ref(0)
const totalPages = ref(0)
const error = ref(null)
const saving = ref(false)
const selectedLocale = ref('en')
const articleEditorMode = ref(true)
const form = ref(createForm())
const revisions = ref([])
const revisionsLoading = ref(false)
const revisionDetail = ref(null)
const revisionDialog = ref(false)

function translation(value = {}) {
  return {
    title: value.title ?? '', slug: value.slug ?? '', excerpt: value.excerpt ?? '',
    bodyMarkdown: value.bodyMarkdown ?? '', articleDocument: value.articleDocument ?? null, seoTitle: value.seoTitle ?? '',
    seoDescription: value.seoDescription ?? ''
  }
}

function createForm(value = {}) {
  return {
    id: value.id ?? null,
    categoryId: value.categoryId ?? null,
    status: value.status ?? 'DRAFT',
    scheduledFor: value.scheduledFor ?? '',
    version: value.version ?? null,
    tagIds: value.tagIds ?? [],
    media: value.media ?? [],
    fa: translation(value.fa), en: translation(value.en)
  }
}

const activeTranslation = computed(() => form.value[selectedLocale.value])
const translations = computed(() => ({
  fa: Boolean(form.value.fa.title && form.value.fa.slug),
  en: Boolean(form.value.en.title && form.value.en.slug)
}))
const fieldErrors = computed(() => mapValidationErrors(error.value))
const categoryOptions = computed(() => categories.value.filter((item) => item.active).map((item) => ({
  label: item[selectedLocale.value]?.name ?? item.categoryKey,
  value: item.id
})))
const tagOptions = computed(() => tags.value.filter((item) => item.active).map((item) => ({
  label: item[selectedLocale.value]?.name ?? item.tagKey,
  value: item.id
})))
const mediaIds = computed({
  get: () => form.value.media.map((reference) => reference.mediaAssetId),
  set: (values) => {
    form.value.media = values.map((mediaAssetId, sortOrder) => ({
      mediaAssetId, usage: 'INLINE', sortOrder
    }))
  }
})
const publicPreviewPath = computed(() => {
  if (form.value.status !== 'PUBLISHED') return null
  const slug = activeTranslation.value.slug.trim()
  return slug ? `/${selectedLocale.value}/blog/${slug}` : null
})

async function load(requestedPage = page.value) {
  state.value = 'loading'
  error.value = null
  try {
    const [posts, categoryResponse, tagResponse] = await Promise.all([
      httpClient.get('/api/v1/admin/blog/posts', { params: { page: requestedPage, size: 20 } }),
      httpClient.get('/api/v1/admin/blog/categories', { params: { page: 0, size: 100 } }),
      httpClient.get('/api/v1/admin/blog/tags', { params: { page: 0, size: 100 } })
    ])
    items.value = posts.data.items ?? []
    page.value = posts.data.page ?? requestedPage
    totalPages.value = posts.data.totalPages ?? 0
    categories.value = categoryResponse.data.items ?? []
    tags.value = tagResponse.data.items ?? []
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
    const response = await httpClient.get(`/api/v1/admin/blog/posts/${item.id}`)
    form.value = createForm(response.data)
    selectedLocale.value = 'en'
    await loadRevisions(form.value.id)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
}

function create() {
  form.value = createForm()
  selectedLocale.value = 'en'
  error.value = null
  revisions.value = []
}

function payload() {
  return {
    categoryId: form.value.categoryId,
    fa: form.value.fa,
    en: form.value.en,
    tagIds: form.value.tagIds,
    media: form.value.media,
    ...(form.value.id ? { version: form.value.version } : {})
  }
}

async function save() {
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = form.value.id
      ? await httpClient.put(`/api/v1/admin/blog/posts/${form.value.id}`, payload())
      : await httpClient.post('/api/v1/admin/blog/posts', payload())
    form.value = createForm(response.data)
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
    const response = await httpClient.post(`/api/v1/admin/blog/posts/${form.value.id}/${action}`, null, {
      params: { version: form.value.version }
    })
    form.value = createForm(response.data)
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

async function schedule() {
  if (!form.value.id || !form.value.scheduledFor) return
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const scheduledFor = new Date(form.value.scheduledFor).toISOString()
    const response = await httpClient.post(`/api/v1/admin/blog/posts/${form.value.id}/schedule`, null, {
      params: { version: form.value.version, scheduledFor }
    })
    form.value = createForm(response.data)
    await load(page.value)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

async function loadRevisions(id = form.value.id) {
  if (!id) { revisions.value = []; return }
  revisionsLoading.value = true
  try { revisions.value = (await httpClient.get(`/api/v1/admin/blog/posts/${id}/revisions`)).data ?? [] }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { revisionsLoading.value = false }
}

async function restoreRevision(revision) {
  if (!form.value.id || form.value.version == null) return
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.post(`/api/v1/admin/blog/posts/${form.value.id}/revisions/${revision.id}/restore-as-draft`, null, { params: { version: form.value.version } })
    form.value = createForm(response.data)
    selectedLocale.value = 'en'
    await load(page.value)
    await loadRevisions(form.value.id)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { saving.value = false }
}

async function viewRevision(revision) {
  if (!form.value.id) return
  revisionsLoading.value = true
  error.value = null
  try {
    revisionDetail.value = (await httpClient.get(`/api/v1/admin/blog/posts/${form.value.id}/revisions/${revision.id}`)).data
    revisionDialog.value = true
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { revisionsLoading.value = false }
}

onMounted(() => { void load() })
</script>

<template>
  <q-page class="admin-page">
    <header class="admin-page__header">
      <div><h1 class="text-h4 q-my-none">{{ t('admin.blogPosts.title') }}</h1><p class="admin-page__description">{{ t('admin.blogPosts.description') }}</p></div>
      <q-btn color="primary" no-caps icon="add" :label="t('admin.blogPosts.create')" @click="create" />
    </header>
    <q-banner v-if="error" class="bg-red-1 text-negative" role="alert">{{ isVersionConflict(error) ? t('admin.blogPosts.conflict') : error.message }}</q-banner>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />
    <template v-else>
      <q-list bordered separator>
        <q-item v-for="item in items" :key="item.id" clickable @click="select(item)">
          <q-item-section><q-item-label>{{ t('admin.blogPosts.item') }}</q-item-label><q-item-label caption>{{ item.status }} · {{ t('admin.blogPosts.version', { version: item.version }) }}</q-item-label></q-item-section>
          <q-item-section side><q-icon name="chevron_right" /></q-item-section>
        </q-item>
      </q-list>
      <AdminPaginatedTable :page="page" :total-pages="totalPages" @change-page="load" />
    </template>
    <q-form class="admin-panel q-pa-md q-gutter-md" @submit.prevent="save">
      <div class="admin-page__header"><h2 class="text-h5 q-my-none">{{ form.id ? t('admin.blogPosts.edit') : t('admin.blogPosts.new') }}</h2><span class="text-caption">{{ form.status }} · {{ form.version == null ? t('admin.blogPosts.newVersion') : t('admin.blogPosts.version', { version: form.version }) }}</span></div>
      <AdminLocaleTabs v-model="selectedLocale" :translations="translations" />
      <q-select v-model="form.categoryId" :options="categoryOptions" emit-value map-options :label="t('admin.blogPosts.category')" :disable="saving" :error="Boolean(fieldErrors.categoryId)" :error-message="fieldErrors.categoryId" />
      <q-select v-model="form.tagIds" :options="tagOptions" emit-value map-options multiple use-chips :label="t('admin.blogPosts.tags')" :disable="saving" />
      <AdminMediaSelector v-model="mediaIds" multiple :label="t('admin.blogPosts.media')" :disable="saving" />
      <q-input v-model="activeTranslation.title" :label="t('admin.blogPosts.titleField')" :disable="saving" :error="Boolean(fieldErrors[`${selectedLocale}.title`])" :error-message="fieldErrors[`${selectedLocale}.title`]" />
      <q-input v-model="activeTranslation.slug" :label="t('admin.blogPosts.slug')" :disable="saving" :error="Boolean(fieldErrors[`${selectedLocale}.slug`])" :error-message="fieldErrors[`${selectedLocale}.slug`]" />
      <q-input v-model="activeTranslation.excerpt" type="textarea" :label="t('admin.blogPosts.excerpt')" :disable="saving" />
      <q-btn-toggle v-model="articleEditorMode" unelevated toggle-color="primary" :options="[{ label: t('admin.articleEditor.blockMode'), value: true }, { label: t('admin.articleEditor.markdownMode'), value: false }]" :aria-label="t('admin.articleEditor.mode')" />
        <ArticleBlockEditor v-if="articleEditorMode" v-model="activeTranslation.bodyMarkdown" v-model:document="activeTranslation.articleDocument" :media-ids="mediaIds" :disable="saving" />
      <AdminMarkdownPreview v-else v-model="activeTranslation.bodyMarkdown" />
      <q-expansion-item :label="t('admin.blogPosts.seoMetadata')" header-class="text-weight-medium">
        <div class="q-gutter-md q-pa-sm"><q-input v-model="activeTranslation.seoTitle" :label="t('admin.blogPosts.seoTitle')" :disable="saving" /><q-input v-model="activeTranslation.seoDescription" type="textarea" :label="t('admin.blogPosts.seoDescription')" :disable="saving" /></div>
      </q-expansion-item>
      <q-expansion-item v-if="form.id" :label="t('admin.blogPosts.revisions')" header-class="text-weight-medium">
        <div class="q-pa-sm q-gutter-sm">
          <p class="text-caption q-mb-sm">{{ t('admin.blogPosts.revisionHelp') }}</p>
          <q-btn flat no-caps icon="refresh" :loading="revisionsLoading" :label="t('admin.blogPosts.refreshRevisions')" @click="loadRevisions()" />
          <q-list v-if="revisions.length" dense bordered separator>
            <q-item v-for="revision in revisions" :key="revision.id">
              <q-item-section clickable @click="viewRevision(revision)"><q-item-label>{{ t('admin.blogPosts.revision', { number: revision.revisionNumber }) }}</q-item-label><q-item-label caption>{{ revision.reason }}</q-item-label></q-item-section>
              <q-item-section side><q-btn outline no-caps icon="restore" :disable="saving" :label="t('admin.blogPosts.restoreAsDraft')" @click="restoreRevision(revision)" /></q-item-section>
            </q-item>
          </q-list>
          <p v-else-if="!revisionsLoading" class="text-caption q-mb-none">{{ t('admin.blogPosts.noRevisions') }}</p>
        </div>
      </q-expansion-item>
      <section v-if="form.id && (form.status === 'DRAFT' || form.status === 'IN_REVIEW')" class="admin-review q-gutter-sm" :aria-label="t('admin.blogPosts.review')">
        <p class="text-caption q-mb-none">{{ t('admin.blogPosts.reviewHelp') }}</p>
        <q-btn v-if="form.status === 'DRAFT'" outline no-caps icon="rate_review" :disable="saving" :label="t('admin.blogPosts.submitForReview')" @click="transition('submit-for-review')" />
        <q-btn v-else outline no-caps icon="undo" :disable="saving" :label="t('admin.blogPosts.returnToDraft')" @click="transition('return-to-draft')" />
      </section>
      <section v-if="form.id && (form.status === 'DRAFT' || form.status === 'IN_REVIEW' || form.status === 'SCHEDULED')" class="admin-schedule q-gutter-sm" :aria-label="t('admin.blogPosts.schedule')">
        <q-input v-if="form.status === 'DRAFT' || form.status === 'IN_REVIEW'" v-model="form.scheduledFor" type="datetime-local" :label="t('admin.blogPosts.scheduledFor')" :disable="saving" />
        <p v-else class="text-caption q-mb-none">{{ t('admin.blogPosts.scheduledFor') }}: <time :datetime="form.scheduledFor">{{ form.scheduledFor }}</time></p>
        <q-btn v-if="form.status === 'DRAFT' || form.status === 'IN_REVIEW'" outline no-caps icon="schedule" :disable="saving || !form.scheduledFor" :label="t('admin.blogPosts.schedule')" @click="schedule" />
        <q-btn v-else outline no-caps icon="event_busy" :disable="saving" :label="t('admin.blogPosts.cancelSchedule')" @click="transition('cancel-schedule')" />
      </section>
      <div class="admin-form-actions"><q-btn type="submit" color="primary" no-caps :loading="saving" :label="t('admin.blogPosts.save')" /><AdminLifecycleActions v-if="form.id" :status="form.status" :saving="saving" :public-preview-path="publicPreviewPath" @publish="transition('publish')" @archive="transition('archive')" /></div>
    </q-form>
    <q-dialog v-model="revisionDialog">
      <q-card class="admin-revision-dialog">
        <q-card-section class="row items-center q-pb-none"><h2 class="text-h6 q-my-none">{{ t('admin.blogPosts.revisionCompare') }}</h2><q-space /><q-btn flat round icon="close" :aria-label="t('admin.actions.cancel')" v-close-popup /></q-card-section>
        <q-card-section v-if="revisionDetail?.snapshot" class="q-gutter-md">
          <p class="text-caption q-mb-none">{{ t('admin.blogPosts.revision', { number: revisionDetail.revisionNumber }) }} · {{ revisionDetail.reason }}</p>
          <div v-for="locale in ['en', 'fa']" :key="locale" class="admin-revision-dialog__locale">
            <h3 class="text-subtitle2 q-my-none">{{ locale.toUpperCase() }}</h3>
            <dl><dt>{{ t('admin.blogPosts.titleField') }}</dt><dd>{{ revisionDetail.snapshot[locale]?.title }}</dd><dt>{{ t('admin.blogPosts.slug') }}</dt><dd>{{ revisionDetail.snapshot[locale]?.slug }}</dd><dt>{{ t('admin.blogPosts.compareCurrent') }}</dt><dd>{{ form[locale]?.title }} · {{ form[locale]?.slug }}</dd></dl>
          </div>
        </q-card-section>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<style scoped>
.admin-revision-dialog { inline-size: min(42rem, calc(100vw - 2 * var(--tm-space-4))); }
.admin-revision-dialog__locale { border-block-start: 1px solid var(--tm-admin-border); display: grid; gap: var(--tm-space-2); padding-block-start: var(--tm-space-3); }
.admin-revision-dialog dl { display: grid; gap: var(--tm-space-1); margin: 0; }
.admin-revision-dialog dt { font-weight: 600; }
.admin-revision-dialog dd { margin: 0; overflow-wrap: anywhere; }
.admin-schedule { border-block: 1px solid var(--tm-admin-border); display: grid; padding-block: var(--tm-space-4); }
</style>
