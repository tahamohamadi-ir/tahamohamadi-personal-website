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
  <q-page class="q-pa-md q-pa-lg-md">
    <div class="q-mb-lg">
      <h1 class="text-h5 q-my-none">{{ t('admin.siteSettings.title') }}</h1>
      <p class="text-body2 text-grey-8 q-mb-none">{{ t('admin.siteSettings.description') }}</p>
    </div>
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">
      {{ error.message }}
      <q-btn v-if="isVersionConflict(error)" flat color="negative" :label="t('admin.siteSettings.reload')" @click="load" />
    </q-banner>
    <q-inner-loading :showing="loading" />
    <q-form v-if="form" class="q-gutter-md" @submit.prevent="save">
      <AdminLocaleTabs v-model="selectedLocale" :translations="translationCompletion" />
      <q-input v-if="activeTranslation" v-model="activeTranslation.brandName" :label="t('admin.siteSettings.siteName')" :error="Boolean(fieldErrors[`${selectedLocale}.brandName`])" :error-message="fieldErrors[`${selectedLocale}.brandName`]" :disable="saving" />
      <q-input v-if="activeTranslation" v-model="activeTranslation.tagline" type="textarea" :label="t('admin.siteSettings.tagline')" :error="Boolean(fieldErrors[`${selectedLocale}.tagline`])" :error-message="fieldErrors[`${selectedLocale}.tagline`]" :disable="saving" />
      <q-input v-if="activeTranslation" v-model="activeTranslation.footerStatement" type="textarea" :label="t('admin.siteSettings.footerStatement')" :error="Boolean(fieldErrors[`${selectedLocale}.footerStatement`])" :error-message="fieldErrors[`${selectedLocale}.footerStatement`]" :disable="saving" />
      <q-input v-if="activeTranslation" v-model="activeTranslation.footerAvailability" type="textarea" :label="t('admin.siteSettings.footerAvailability')" :error="Boolean(fieldErrors[`${selectedLocale}.footerAvailability`])" :error-message="fieldErrors[`${selectedLocale}.footerAvailability`]" :disable="saving" />
      <q-input v-if="activeTranslation" v-model="activeTranslation.footerRights" type="textarea" :label="t('admin.siteSettings.footerRights')" :error="Boolean(fieldErrors[`${selectedLocale}.footerRights`])" :error-message="fieldErrors[`${selectedLocale}.footerRights`]" :disable="saving" />
      <AdminMediaSelector v-model="form.logoMediaId" :label="t('admin.siteSettings.logo')" :disable="saving" />
      <AdminMediaSelector v-model="form.ogMediaId" :label="t('admin.siteSettings.ogMedia')" :disable="saving" />
      <q-select v-model="form.themePreset" :options="themeOptions" emit-value map-options :label="t('admin.siteSettings.theme')" :disable="saving" />
      <q-select v-model="form.layoutDensity" :options="densityOptions" emit-value map-options :label="t('admin.siteSettings.density')" :disable="saving" />
      <div class="row q-gutter-sm">
        <q-btn type="submit" color="primary" :label="t('admin.siteSettings.save')" :loading="saving" />
        <a :href="`/${selectedLocale}`" target="_blank" rel="noopener noreferrer" class="text-primary self-center">{{ t('admin.actions.preview') }}</a>
      </div>
    </q-form>
  </q-page>
</template>
