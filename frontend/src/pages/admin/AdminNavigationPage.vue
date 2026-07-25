<script setup>
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { onBeforeRouteLeave } from 'vue-router'

import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import { createUnsavedChangesGuard, isVersionConflict, mapValidationErrors } from 'src/composables/adminContentInteractions'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()
const items = ref([])
const loading = ref(true)
const saving = ref(false)
const error = ref(null)
const fieldErrors = ref({})
const selectedLocale = ref('fa')
const replacing = ref(false)
const pendingRemoval = ref(null)
const changes = createUnsavedChangesGuard(() => Promise.resolve(window.confirm(t('admin.unsaved.discard'))))
const removalOpen = computed({
  get: () => pendingRemoval.value !== null,
  set: (value) => { if (!value) pendingRemoval.value = null }
})

function replaceItems(value) {
  replacing.value = true
  items.value = value
  changes.markSaved()
  nextTick(() => { replacing.value = false })
}

function newItem() {
  return { id: null, version: null, key: '', targetPath: '', externalTarget: false, active: true, fa: { label: '' }, en: { label: '' } }
}

const translationCompletion = computed(() => Object.fromEntries(
  items.value.map((item, index) => [index, {
    fa: Boolean(item.fa?.label?.trim()),
    en: Boolean(item.en?.label?.trim())
  }])
))

function activeTranslation(item) {
  return item[selectedLocale.value]
}

function labelError(index) {
  return fieldErrors.value[`items[${index}].${selectedLocale.value}.label`]
    ?? fieldErrors.value[`items.${index}.${selectedLocale.value}.label`]
}

async function load() {
  loading.value = true
  error.value = null
  fieldErrors.value = {}
  try {
    const response = await httpClient.get('/api/v1/admin/navigation')
    replaceItems(response.data.items ?? [])
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { loading.value = false }
}

function move(index, offset) {
  const target = index + offset
  if (target < 0 || target >= items.value.length) return
  const [item] = items.value.splice(index, 1)
  items.value.splice(target, 0, item)
}

function requestRemove(index) { pendingRemoval.value = index }
function remove() {
  if (pendingRemoval.value === null) return
  items.value.splice(pendingRemoval.value, 1)
  pendingRemoval.value = null
}

function targetError(index) {
  return fieldErrors.value[`items[${index}].targetPath`]
    ?? fieldErrors.value[`items.${index}.targetPath`]
    ?? null
}

function targetIsValid(item) {
  const path = item?.targetPath?.trim() ?? ''
  return item?.externalTarget
    ? /^https:\/\//.test(path)
    : /^\/(fa|en)(?:\/|$)/.test(path) || /^\/\{lang\}(?:\/|$)/.test(path)
}

function validateTargets() {
  const invalid = {}
  items.value.forEach((item, index) => {
    if (!targetIsValid(item)) {
      invalid[`items[${index}].targetPath`] = t('admin.navigationDialog.invalidTarget')
    }
  })
  fieldErrors.value = invalid
  return Object.keys(invalid).length === 0
}

async function save() {
  if (!validateTargets()) {
    error.value = { message: t('admin.navigationDialog.invalidTarget') }
    return
  }
  saving.value = true
  error.value = null
  fieldErrors.value = {}
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.put('/api/v1/admin/navigation', { items: items.value })
    replaceItems(response.data.items ?? [])
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    fieldErrors.value = mapValidationErrors(error.value)
  }
  finally { saving.value = false }
}

onMounted(() => { void load() })
watch(items, () => { if (!replacing.value) changes.markDirty() }, { deep: true, flush: 'sync' })
onBeforeRouteLeave(async () => changes.confirmLeave())
</script>

<template>
  <q-page class="q-pa-md q-pa-lg-md">
    <div class="row items-center justify-between q-col-gutter-md q-mb-lg">
      <div class="col">
        <h1 class="text-h5 q-my-none">{{ t('admin.navigation.title') }}</h1>
        <p class="text-body2 text-grey-8 q-mb-none">{{ t('admin.navigation.description') }}</p>
      </div>
      <div class="col-auto"><q-btn outline color="primary" icon="add" :label="t('admin.navigation.add')" :disable="loading || items.length >= 12" @click="items.push(newItem())" /></div>
    </div>
    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">
      {{ error.message }}
      <q-btn v-if="isVersionConflict(error)" flat color="negative" :label="t('admin.navigation.reload')" @click="load" />
    </q-banner>
    <q-inner-loading :showing="loading" />
    <q-card v-for="(item, index) in items" :key="item.id ?? `new-${index}`" flat bordered class="q-mb-md">
      <q-card-section class="row q-col-gutter-sm items-center">
        <div class="col"><q-input v-model="item.key" :label="t('admin.navigation.key')" :disable="saving" /></div>
        <div class="col-auto"><q-toggle v-model="item.active" :label="t('admin.navigation.visible')" :disable="saving" /></div>
        <div class="col-auto q-gutter-xs">
          <q-btn flat round icon="keyboard_arrow_up" :aria-label="t('admin.navigation.moveUp', { index: index + 1 })" :disable="saving || index === 0" @click="move(index, -1)" />
          <q-btn flat round icon="keyboard_arrow_down" :aria-label="t('admin.navigation.moveDown', { index: index + 1 })" :disable="saving || index === items.length - 1" @click="move(index, 1)" />
          <q-btn flat round color="negative" icon="delete" :aria-label="t('admin.navigation.remove', { index: index + 1 })" :disable="saving" @click="requestRemove(index)" />
        </div>
      </q-card-section>
      <q-card-section class="q-pt-none q-gutter-md">
        <q-toggle v-model="item.externalTarget" :label="t('admin.navigation.external')" :disable="saving" />
        <q-input v-model="item.targetPath" :label="item.externalTarget ? t('admin.navigation.externalTarget') : t('admin.navigation.internalTarget')" :hint="item.externalTarget ? t('admin.navigation.externalHint') : t('admin.navigation.internalHint')" :error="Boolean(targetError(index))" :error-message="targetError(index)" :disable="saving" />
        <AdminLocaleTabs v-model="selectedLocale" :translations="translationCompletion[index]" />
        <q-input
          v-if="activeTranslation(item)"
          v-model="activeTranslation(item).label"
          :label="selectedLocale === 'fa' ? t('admin.navigation.faLabel') : t('admin.navigation.enLabel')"
          :error="Boolean(labelError(index))"
          :error-message="labelError(index)"
          :disable="saving"
        />
      </q-card-section>
    </q-card>
    <p v-if="!loading && items.length === 0" class="text-grey-8">{{ t('admin.navigation.empty') }}</p>
    <q-btn color="primary" :label="t('admin.navigation.save')" :loading="saving" :disable="loading" @click="save" />
    <q-dialog v-model="removalOpen" persistent>
      <q-card>
        <q-card-section class="text-h6">{{ t('admin.navigationDialog.removeTitle') }}</q-card-section>
        <q-card-section>{{ t('admin.navigationDialog.removeDescription') }}</q-card-section>
        <q-card-actions align="right">
          <q-btn flat :label="t('admin.unsaved.cancel')" @click="pendingRemoval = null" />
          <q-btn color="negative" :label="t('admin.navigationDialog.removeConfirm')" @click="remove" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>
