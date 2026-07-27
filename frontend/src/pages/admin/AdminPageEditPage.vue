<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { onBeforeRouteLeave } from 'vue-router'

import AdminEditorShell from 'src/components/admin/AdminEditorShell.vue'
import AdminLifecycleActions from 'src/components/admin/AdminLifecycleActions.vue'
import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminPageBlockComposer from 'src/components/admin/AdminPageBlockComposer.vue'

import { createUnsavedChangesGuard, isVersionConflict, mapValidationErrors } from 'src/composables/adminContentInteractions'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const pageId = computed(() => route.params.id)
const loading = ref(true)
const saving = ref(false)
const error = ref(null)
const selectedLocale = ref('fa')
const fieldErrors = ref({})
const form = ref(null)

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

const activeTranslation = computed(() => form.value ? form.value[selectedLocale.value] : null)

const translations = computed(() => ({
  fa: Boolean(form.value?.fa?.title && form.value?.fa?.slug),
  en: Boolean(form.value?.en?.title && form.value?.en?.slug)
}))

const publicPreviewPath = computed(() => {
  if (!form.value || form.value.status !== 'PUBLISHED') return null

  const locale = selectedLocale.value
  const pageKey = form.value.pageKey?.trim().toLowerCase()
  const staticPaths = { home: `/${locale}`, about: `/${locale}/about`, research: `/${locale}/research` }
  if (staticPaths[pageKey]) return staticPaths[pageKey]
  const slug = activeTranslation.value?.slug?.trim()
  return slug ? `/${locale}/pages/${slug}` : null
})

async function load() {
  loading.value = true
  error.value = null
  try {
    const response = await httpClient.get(`/api/v1/admin/pages/${pageId.value}`)
    form.value = createForm(response.data)
    changes.markSaved()
    void loadRevisions()
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    loading.value = false
  }
}

async function loadRevisions() {
  if (!pageId.value) return
  revisionsLoading.value = true
  try {
    const response = await httpClient.get(`/api/v1/admin/pages/${pageId.value}/revisions`)
    revisions.value = response.data ?? []
  }
  catch {
    revisions.value = []
  }
  finally {
    revisionsLoading.value = false
  }
}

async function viewRevision(revisionId) {
  try {
    const response = await httpClient.get(`/api/v1/admin/pages/${pageId.value}/revisions/${revisionId}`)
    revisionDetail.value = response.data
    revisionDialog.value = true
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
}

function promptRestore(revision) {
  restoreCandidate.value = revision
  restoreConfirmationOpen.value = true
}

async function confirmRestore() {
  if (!restoreCandidate.value) return
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.post(`/api/v1/admin/pages/${pageId.value}/revisions/${restoreCandidate.value.id}/restore`)
    form.value = createForm(response.data)
    changes.markSaved()
    restoreConfirmationOpen.value = false
    revisionDialog.value = false
    await loadRevisions()
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
}

async function save() {
  if (!form.value) return
  const confirmed = await changes.confirmSave()
  if (!confirmed) return

  saving.value = true
  error.value = null
  fieldErrors.value = {}

  try {
    await primeCsrfToken(httpClient)
    const payload = {
      pageKey: form.value.pageKey,
      fa: form.value.fa,
      en: form.value.en
    }
    const response = await httpClient.put(`/api/v1/admin/pages/${pageId.value}`, payload)
    form.value = createForm(response.data)
    changes.markSaved()
    await loadRevisions()
  }
  catch (cause) {
    const normalized = normalizeApiError(cause)
    error.value = normalized
    fieldErrors.value = mapValidationErrors(normalized)
  }
  finally {
    saving.value = false
  }
}

async function updateStatus(newStatus) {
  if (!form.value) return
  saving.value = true
  error.value = null
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.post(`/api/v1/admin/pages/${pageId.value}/${newStatus.toLowerCase()}`)
    form.value = createForm(response.data)
    changes.markSaved()
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    saving.value = false
  }
}

onMounted(load)
onBeforeRouteLeave(async () => changes.confirmLeave())
</script>

<template>
  <q-page class="admin-page admin-page-edit">
    <q-inner-loading :showing="loading" />
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">
      {{ error.message }}
      <q-btn v-if="isVersionConflict(error)" flat color="negative" :label="t('admin.actions.reload')" @click="load" />
    </q-banner>

    <AdminEditorShell
      v-if="form"
      :title="activeTranslation?.title || form.pageKey || t('admin.pages.editPage')"
      :description="`Key: ${form.pageKey} • Version: ${form.version ?? 1}`"
      back-path="/admin/pages"
      :status="form.status"
      :is-dirty="changes.isDirty.value"
      :saving="saving"
      :preview-path="publicPreviewPath"
      @save="save"
      @discard="load"
    >
      <template #header-actions>
        <q-btn
          flat
          dense
          no-caps
          icon="history"
          :label="t('admin.pages.revisions.history')"
          @click="revisionDialog = true"
        />
        <AdminLifecycleActions
          :status="form.status"
          :saving="saving"
          @publish="updateStatus('PUBLISHED')"
          @archive="updateStatus('ARCHIVED')"
          @activate="updateStatus('DRAFT')"
        />
      </template>

      <!-- Main Metadata & Locale Selection Card -->
      <section class="admin-panel">
        <div class="admin-panel__header">
          <div>
            <h2 class="text-h6 q-my-none">{{ t('admin.pages.metadata') }}</h2>
            <p class="text-caption text-grey-7 q-mb-none">{{ t('admin.pages.metadataHelp') }}</p>
          </div>
          <AdminLocaleTabs v-model="selectedLocale" :translations="translations" />
        </div>

        <div class="q-gutter-md q-mt-sm">
          <q-input
            v-model="form.pageKey"
            outlined
            dense
            :label="t('admin.pages.pageKey')"
            :error="Boolean(fieldErrors.pageKey)"
            :error-message="fieldErrors.pageKey"
            :disable="saving"
            @update:model-value="changes.markDirty()"
          />

          <template v-if="activeTranslation">
            <div class="row q-col-gutter-md">
              <div class="col-12 col-md-6">
                <q-input
                  v-model="activeTranslation.title"
                  outlined
                  dense
                  :label="t('admin.pages.translationTitle')"
                  :error="Boolean(fieldErrors[`${selectedLocale}.title`])"
                  :error-message="fieldErrors[`${selectedLocale}.title`]"
                  :disable="saving"
                  @update:model-value="changes.markDirty()"
                />
              </div>
              <div class="col-12 col-md-6">
                <q-input
                  v-model="activeTranslation.slug"
                  outlined
                  dense
                  :label="t('admin.pages.slug')"
                  :error="Boolean(fieldErrors[`${selectedLocale}.slug`])"
                  :error-message="fieldErrors[`${selectedLocale}.slug`]"
                  :disable="saving"
                  @update:model-value="changes.markDirty()"
                />
              </div>
            </div>

            <q-input
              v-model="activeTranslation.summary"
              outlined
              dense
              type="textarea"
              :rows="2"
              :label="t('admin.pages.summary')"
              :error="Boolean(fieldErrors[`${selectedLocale}.summary`])"
              :error-message="fieldErrors[`${selectedLocale}.summary`]"
              :disable="saving"
              @update:model-value="changes.markDirty()"
            />

            <div class="row q-col-gutter-md">
              <div class="col-12 col-md-6">
                <q-input
                  v-model="activeTranslation.seoTitle"
                  outlined
                  dense
                  :label="t('admin.pages.seoTitle')"
                  :error="Boolean(fieldErrors[`${selectedLocale}.seoTitle`])"
                  :error-message="fieldErrors[`${selectedLocale}.seoTitle`]"
                  :disable="saving"
                  @update:model-value="changes.markDirty()"
                />
              </div>
              <div class="col-12 col-md-6">
                <q-input
                  v-model="activeTranslation.seoDescription"
                  outlined
                  dense
                  :label="t('admin.pages.seoDescription')"
                  :error="Boolean(fieldErrors[`${selectedLocale}.seoDescription`])"
                  :error-message="fieldErrors[`${selectedLocale}.seoDescription`]"
                  :disable="saving"
                  @update:model-value="changes.markDirty()"
                />
              </div>
            </div>
          </template>
        </div>
      </section>

      <!-- Visual Page Block Composer Section -->
      <section class="admin-panel">
        <h2 class="text-h6 q-my-none q-mb-md">{{ t('admin.pages.blockComposer') }}</h2>
        <AdminPageBlockComposer
          :page-id="form.id"
          :page-version="form.version"
          @composition-updated="load"
        />
      </section>
    </AdminEditorShell>

    <!-- Revision History Dialog -->
    <q-dialog v-model="revisionDialog" class="admin-revisions-dialog">
      <q-card style="min-width: 600px; max-width: 800px;">
        <q-card-section class="row items-center justify-between">
          <div class="text-h6">{{ t('admin.pages.revisions.title') }}</div>
          <q-btn v-close-popup flat round icon="close" />
        </q-card-section>
        <q-separator />
        <q-card-section>
          <q-list v-if="revisions.length" separator>
            <q-item v-for="rev in revisions" :key="rev.id" clickable @click="viewRevision(rev.id)">
              <q-item-section>
                <q-item-label class="text-weight-bold">Revision #{{ rev.revisionNumber }}</q-item-label>
                <q-item-label caption>{{ new Date(rev.createdAt).toLocaleString() }} • {{ rev.createdBy || 'Admin' }}</q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-btn flat dense color="primary" :label="t('admin.pages.revisions.restore')" @click.stop="promptRestore(rev)" />
              </q-item-section>
            </q-item>
          </q-list>
          <div v-else class="text-grey-7 text-center q-pa-md">
            {{ t('admin.pages.revisions.empty') }}
          </div>
        </q-card-section>
      </q-card>
    </q-dialog>

    <!-- Restore Confirmation Dialog -->
    <q-dialog v-model="restoreConfirmationOpen">
      <q-card>
        <q-card-section class="text-h6">{{ t('admin.pages.revisions.restoreTitle') }}</q-card-section>
        <q-card-section>{{ t('admin.pages.revisions.restoreDescription') }}</q-card-section>
        <q-card-actions align="right">
          <q-btn v-close-popup flat :label="t('admin.unsaved.cancel')" />
          <q-btn color="primary" unelevated :label="t('admin.pages.revisions.confirmRestore')" @click="confirmRestore" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<style scoped>
.admin-panel {
  background: var(--tm-admin-surface, #ffffff);
  border: 1px solid var(--tm-admin-border, #cbd5e1);
  border-radius: var(--tm-admin-panel-radius, 10px);
  padding: 20px;
}
.admin-panel__header {
  align-items: flex-start;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: space-between;
}
</style>
