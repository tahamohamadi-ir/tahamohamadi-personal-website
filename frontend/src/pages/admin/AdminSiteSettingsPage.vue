<script setup>
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { onBeforeRouteLeave } from 'vue-router'

import AdminMediaSelector from 'src/components/admin/AdminMediaSelector.vue'
import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import { createUnsavedChangesGuard, isVersionConflict, mapValidationErrors } from 'src/composables/adminContentInteractions'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()
const form = ref(null)
const loading = ref(true)
const saving = ref(false)
const error = ref(null)
const fieldErrors = ref({})
const selectedLocale = ref('fa')
const replacing = ref(false)
const changes = createUnsavedChangesGuard(() => Promise.resolve(window.confirm(t('admin.unsaved.discard'))))
const activeTranslation = computed(() => form.value?.translations?.[selectedLocale.value] ?? null)
const translationCompletion = computed(() => ({
  fa: Boolean(form.value?.translations?.fa?.brandName),
  en: Boolean(form.value?.translations?.en?.brandName)
}))
const themeOptions = computed(() => ['EDITORIAL_NAVY'].map((value) => ({
  value,
  label: t(`admin.siteSettings.themeOptions.${value}`)
})))
const densityOptions = computed(() => ['COMFORTABLE', 'STANDARD'].map((value) => ({
  value,
  label: t(`admin.siteSettings.densityOptions.${value}`)
})))

function replaceForm(value) {
  replacing.value = true
  form.value = value
  changes.markSaved()
  nextTick(() => { replacing.value = false })
}

async function load() {
  loading.value = true
  error.value = null
  fieldErrors.value = {}
  try {
    const response = await httpClient.get('/api/v1/admin/site-settings')
    replaceForm(response.data)
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { loading.value = false }
}

async function save() {
  if (!form.value) return
  saving.value = true
  error.value = null
  fieldErrors.value = {}
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.put('/api/v1/admin/site-settings', form.value)
    replaceForm(response.data)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    fieldErrors.value = mapValidationErrors(error.value)
  }
  finally { saving.value = false }
}

onMounted(() => { void load() })
watch(form, () => { if (!replacing.value) changes.markDirty() }, { deep: true, flush: 'sync' })
onBeforeRouteLeave(async () => changes.confirmLeave())
</script>

<template>
  <q-page class="admin-page admin-site-settings">
    <header class="admin-page__header">
      <div>
        <h1 class="text-h4 q-my-none">{{ t('admin.siteSettings.title') }}</h1>
        <p class="admin-page__description">{{ t('admin.siteSettings.description') }}</p>
      </div>
      <a :href="`/${selectedLocale}`" target="_blank" rel="noopener noreferrer" class="tm-text-link">
        <q-icon name="open_in_new" size="18px" class="q-mr-xs" />{{ t('admin.actions.preview') }}
      </a>
    </header>
    <q-banner v-if="error" class="bg-red-1 text-negative" rounded role="alert">
      {{ error.message }}
      <q-btn v-if="isVersionConflict(error)" flat color="negative" :label="t('admin.siteSettings.reload')" @click="load" />
    </q-banner>
    <q-inner-loading :showing="loading" />
    <q-form v-if="form" class="admin-site-settings__form" @submit.prevent="save">
      <section class="admin-panel admin-site-settings__panel">
        <div class="admin-site-settings__panel-header">
          <div><h2 class="text-h6 q-my-none">{{ t('admin.siteSettings.identity') }}</h2><p>{{ t('admin.siteSettings.identityHelp') }}</p></div>
          <AdminLocaleTabs v-model="selectedLocale" :translations="translationCompletion" />
        </div>
        <div v-if="activeTranslation" class="admin-site-settings__fields">
          <q-input v-model="activeTranslation.brandName" outlined :label="t('admin.siteSettings.siteName')" :error="Boolean(fieldErrors[`${selectedLocale}.brandName`])" :error-message="fieldErrors[`${selectedLocale}.brandName`]" :disable="saving" />
          <q-input v-model="activeTranslation.tagline" outlined type="textarea" :rows="3" :label="t('admin.siteSettings.tagline')" :error="Boolean(fieldErrors[`${selectedLocale}.tagline`])" :error-message="fieldErrors[`${selectedLocale}.tagline`]" :disable="saving" />
        </div>
      </section>
      <section class="admin-panel admin-site-settings__panel">
        <div class="admin-site-settings__panel-header"><div><h2 class="text-h6 q-my-none">{{ t('admin.siteSettings.footer') }}</h2><p>{{ t('admin.siteSettings.footerHelp') }}</p></div></div>
        <div v-if="activeTranslation" class="admin-site-settings__fields">
          <q-input v-model="activeTranslation.footerStatement" outlined type="textarea" :rows="3" :label="t('admin.siteSettings.footerStatement')" :error="Boolean(fieldErrors[`${selectedLocale}.footerStatement`])" :error-message="fieldErrors[`${selectedLocale}.footerStatement`]" :disable="saving" />
          <q-input v-model="activeTranslation.footerAvailability" outlined type="textarea" :rows="3" :label="t('admin.siteSettings.footerAvailability')" :error="Boolean(fieldErrors[`${selectedLocale}.footerAvailability`])" :error-message="fieldErrors[`${selectedLocale}.footerAvailability`]" :disable="saving" />
          <q-input v-model="activeTranslation.footerRights" outlined type="textarea" :rows="3" :label="t('admin.siteSettings.footerRights')" :error="Boolean(fieldErrors[`${selectedLocale}.footerRights`])" :error-message="fieldErrors[`${selectedLocale}.footerRights`]" :disable="saving" />
        </div>
      </section>
      <section class="admin-panel admin-site-settings__panel">
        <div class="admin-site-settings__panel-header"><div><h2 class="text-h6 q-my-none">{{ t('admin.siteSettings.presentation') }}</h2><p>{{ t('admin.siteSettings.presentationHelp') }}</p></div></div>
        <div class="admin-site-settings__fields">
          <AdminMediaSelector v-model="form.logoMediaId" :allowed-types="['image']" :label="t('admin.siteSettings.logo')" :disable="saving" />
          <AdminMediaSelector v-model="form.ogMediaId" :allowed-types="['image']" :label="t('admin.siteSettings.ogMedia')" :disable="saving" />
          <q-select v-model="form.themePreset" outlined :options="themeOptions" emit-value map-options :label="t('admin.siteSettings.theme')" :disable="saving" />
          <q-select v-model="form.layoutDensity" outlined :options="densityOptions" emit-value map-options :label="t('admin.siteSettings.density')" :disable="saving" />
        </div>
      </section>
      <footer class="admin-site-settings__actions">
        <span class="text-caption">{{ changes.isDirty ? t('admin.siteSettings.unsaved') : t('admin.siteSettings.saved') }}</span>
        <q-btn type="submit" color="primary" no-caps icon="save" :label="t('admin.siteSettings.save')" :loading="saving" />
      </footer>
    </q-form>
  </q-page>
</template>

<style scoped>
.admin-site-settings__form { display: grid; gap: var(--tm-admin-panel-gap); max-inline-size: 1040px; }
.admin-site-settings__panel { display: grid; gap: var(--tm-space-5); padding: var(--tm-space-5); }
.admin-site-settings__panel-header { align-items: start; display: flex; flex-wrap: wrap; gap: var(--tm-space-4); justify-content: space-between; }
.admin-site-settings__panel-header p { color: var(--tm-text-secondary); margin: var(--tm-space-1) 0 0; max-inline-size: 64ch; }
.admin-site-settings__fields { display: grid; gap: var(--tm-space-4); grid-template-columns: repeat(2, minmax(0, 1fr)); }
.admin-site-settings__fields > :nth-child(2n + 1):last-child { grid-column: 1 / -1; }
.admin-site-settings__actions { align-items: center; background: var(--tm-admin-surface); border: 1px solid var(--tm-admin-border); border-radius: var(--tm-admin-panel-radius); display: flex; gap: var(--tm-space-3); justify-content: space-between; padding: var(--tm-space-3) var(--tm-space-4); position: sticky; inset-block-end: var(--tm-space-3); }
.admin-site-settings__actions span { color: var(--tm-text-secondary); }
@media (max-width: 599px) { .admin-site-settings__fields { grid-template-columns: 1fr; } .admin-site-settings__fields > :nth-child(2n + 1):last-child { grid-column: auto; } }
</style>
