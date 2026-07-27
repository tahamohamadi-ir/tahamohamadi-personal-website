<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { onBeforeRouteLeave } from 'vue-router'

import AdminEditorShell from 'src/components/admin/AdminEditorShell.vue'
import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminMediaSelector from 'src/components/admin/AdminMediaSelector.vue'

import { useUnsavedChangesGuard } from 'src/composables/useUnsavedChangesGuard'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'

import {
  formatFieldErrors,
  isVersionConflict,
  normalizeApiError
} from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()

const loading = ref(true)
const saving = ref(false)
const error = ref(null)
const selectedLocale = ref('fa')

const form = ref(null)
const fieldErrors = ref({})

const changes = useUnsavedChangesGuard(form)

const themeOptions = computed(() => [
  { label: t('admin.siteSettings.themePresets.editorialDefault'), value: 'EDITORIAL_DEFAULT' },
  { label: t('admin.siteSettings.themePresets.highContrastMonochrome'), value: 'HIGH_CONTRAST_MONOCHROME' },
  { label: t('admin.siteSettings.themePresets.warmPaperAcademic'), value: 'WARM_PAPER_ACADEMIC' }
])

const densityOptions = computed(() => [
  { label: t('admin.siteSettings.densityPresets.comfortable'), value: 'COMFORTABLE' },
  { label: t('admin.siteSettings.densityPresets.compact'), value: 'COMPACT' }
])

const translationCompletion = computed(() => ({
  fa: Boolean(form.value?.translations?.fa?.brandName?.trim() && form.value?.translations?.fa?.tagline?.trim()),
  en: Boolean(form.value?.translations?.en?.brandName?.trim() && form.value?.translations?.en?.tagline?.trim())
}))

const activeTranslation = computed(() => {
  return form.value?.translations?.[selectedLocale.value]
})

async function load() {
  loading.value = true
  error.value = null
  fieldErrors.value = {}
  try {
    const response = await httpClient.get('/api/v1/admin/site-settings')
    form.value = response.data
    changes.trackInitialState(form.value)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
  }
  finally {
    loading.value = false
  }
}

async function save() {
  if (!form.value) return
  saving.value = true
  error.value = null
  fieldErrors.value = {}
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.put('/api/v1/admin/site-settings', form.value)
    form.value = response.data
    changes.trackInitialState(form.value)
  }
  catch (cause) {
    const normalized = normalizeApiError(cause)
    error.value = normalized
    fieldErrors.value = formatFieldErrors(normalized)
  }
  finally {
    saving.value = false
  }
}

onMounted(load)
onBeforeRouteLeave(async () => changes.confirmLeave())
</script>

<template>
  <q-page class="admin-page admin-site-settings">
    <q-inner-loading :showing="loading" />
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">
      {{ error.message }}
      <q-btn v-if="isVersionConflict(error)" flat color="negative" :label="t('admin.siteSettings.reload')" @click="load" />
    </q-banner>

    <AdminEditorShell
      v-if="form"
      :title="t('admin.siteSettings.title')"
      :description="t('admin.siteSettings.description')"
      :is-dirty="changes.isDirty"
      :saving="saving"
      :preview-path="`/${selectedLocale}`"
      target="_blank"
      @save="save"
      @discard="changes.resetToInitialState"
    >
      <section class="admin-panel admin-site-settings__panel">
        <div class="admin-site-settings__panel-header">
          <div>
            <h2 class="text-h6 q-my-none">{{ t('admin.siteSettings.identity') }}</h2>
            <p>{{ t('admin.siteSettings.identityHelp') }}</p>
          </div>
          <AdminLocaleTabs v-model="selectedLocale" :translations="translationCompletion" />
        </div>
        <div v-if="activeTranslation" class="admin-site-settings__fields">
          <q-input
            v-model="activeTranslation.brandName"
            outlined
            :label="t('admin.siteSettings.siteName')"
            :error="Boolean(fieldErrors[`${selectedLocale}.brandName`])"
            :error-message="fieldErrors[`${selectedLocale}.brandName`]"
            :disable="saving"
          />
          <q-input
            v-model="activeTranslation.tagline"
            outlined
            type="textarea"
            :rows="3"
            :label="t('admin.siteSettings.tagline')"
            :error="Boolean(fieldErrors[`${selectedLocale}.tagline`])"
            :error-message="fieldErrors[`${selectedLocale}.tagline`]"
            :disable="saving"
          />
        </div>
      </section>

      <section class="admin-panel admin-site-settings__panel">
        <div class="admin-site-settings__panel-header">
          <div>
            <h2 class="text-h6 q-my-none">{{ t('admin.siteSettings.footer') }}</h2>
            <p>{{ t('admin.siteSettings.footerHelp') }}</p>
          </div>
        </div>
        <div v-if="activeTranslation" class="admin-site-settings__fields">
          <q-input
            v-model="activeTranslation.footerStatement"
            outlined
            type="textarea"
            :rows="3"
            :label="t('admin.siteSettings.footerStatement')"
            :error="Boolean(fieldErrors[`${selectedLocale}.footerStatement`])"
            :error-message="fieldErrors[`${selectedLocale}.footerStatement`]"
            :disable="saving"
          />
          <q-input
            v-model="activeTranslation.footerAvailability"
            outlined
            type="textarea"
            :rows="3"
            :label="t('admin.siteSettings.footerAvailability')"
            :error="Boolean(fieldErrors[`${selectedLocale}.footerAvailability`])"
            :error-message="fieldErrors[`${selectedLocale}.footerAvailability`]"
            :disable="saving"
          />
          <q-input
            v-model="activeTranslation.footerRights"
            outlined
            type="textarea"
            :rows="3"
            :label="t('admin.siteSettings.footerRights')"
            :error="Boolean(fieldErrors[`${selectedLocale}.footerRights`])"
            :error-message="fieldErrors[`${selectedLocale}.footerRights`]"
            :disable="saving"
          />
        </div>
      </section>

      <section class="admin-panel admin-site-settings__panel">
        <div class="admin-site-settings__panel-header">
          <div>
            <h2 class="text-h6 q-my-none">{{ t('admin.siteSettings.presentation') }}</h2>
            <p>{{ t('admin.siteSettings.presentationHelp') }}</p>
          </div>
        </div>
        <div class="admin-site-settings__fields">
          <AdminMediaSelector
            v-model="form.logoMediaId"
            :allowed-types="['image']"
            :label="t('admin.siteSettings.logo')"
            :disable="saving"
          />
          <AdminMediaSelector
            v-model="form.ogMediaId"
            :allowed-types="['image']"
            :label="t('admin.siteSettings.ogMedia')"
            :disable="saving"
          />
          <q-select
            v-model="form.themePreset"
            outlined
            :options="themeOptions"
            emit-value map-options
            :label="t('admin.siteSettings.theme')"
            :disable="saving"
          />
          <q-select
            v-model="form.layoutDensity"
            outlined
            :options="densityOptions"
            emit-value map-options
            :label="t('admin.siteSettings.density')"
            :disable="saving"
          />
        </div>
      </section>
    </AdminEditorShell>
  </q-page>
</template>

<style scoped>
.admin-site-settings__panel {
  display: grid;
  gap: var(--tm-space-5);
  padding: var(--tm-space-5);
}
.admin-site-settings__panel-header {
  align-items: start;
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-4);
  justify-content: space-between;
}
.admin-site-settings__panel-header p {
  color: var(--tm-text-secondary);
  margin: var(--tm-space-1) 0 0;
  max-inline-size: 64ch;
}
.admin-site-settings__fields {
  display: grid;
  gap: var(--tm-space-4);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}
.admin-site-settings__fields > :nth-child(2n + 1):last-child {
  grid-column: 1 / -1;
}
@media (max-width: 599px) {
  .admin-site-settings__fields {
    grid-template-columns: 1fr;
  }
  .admin-site-settings__fields > :nth-child(2n + 1):last-child {
    grid-column: auto;
  }
}
</style>
