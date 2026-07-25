<script setup>
import { computed, inject, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { onBeforeRouteLeave } from 'vue-router'

import AdminLocaleTabs from 'src/components/admin/AdminLocaleTabs.vue'
import AdminMarkdownPreview from 'src/components/admin/AdminMarkdownPreview.vue'
import AdminMediaSelector from 'src/components/admin/AdminMediaSelector.vue'
import PageBlockRenderer from 'src/components/public/PageBlockRenderer.vue'
import { createUnsavedChangesGuard, isVersionConflict, mapValidationErrors } from 'src/composables/adminContentInteractions'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'
import { normalizeApiError } from 'src/services/httpClient'

const props = defineProps({
  pageId: { type: String, default: null },
  pageVersion: { type: Number, default: null },
  disable: Boolean
})
const emit = defineEmits(['saved'])
const httpClient = inject(HTTP_CLIENT_KEY)
const { t } = useI18n()
const locale = ref('fa')
const blocks = ref([])
const version = ref(null)
const loading = ref(false)
const saving = ref(false)
const error = ref(null)
const fieldErrors = ref({})
const replacing = ref(false)
const pendingRemoval = ref(null)
const previewOpen = ref(false)
const operationStatus = ref('')
const changes = createUnsavedChangesGuard(() => Promise.resolve(window.confirm(t('admin.unsaved.discard'))))
const removalOpen = computed({
  get: () => pendingRemoval.value !== null,
  set: (value) => { if (!value) pendingRemoval.value = null }
})

const blockOptions = computed(() => [
  'HERO', 'RICH_TEXT', 'MEDIA', 'MEDIA_TEXT', 'CALL_TO_ACTION', 'COLLECTION', 'SKILLS', 'RESUME', 'SOCIAL_LINKS', 'CONTACT'
].map((value) => ({ value, label: t(`admin.composer.blockTypes.${value}`) })))
const collectionOptions = computed(() => [
  'BLOG', 'PORTFOLIO', 'PUBLICATIONS'
].map((value) => ({
  value,
  label: t(`admin.composer.collectionSources.${value}`)
})))

function supportsEyebrow(type) {
  return ['HERO', 'CALL_TO_ACTION', 'COLLECTION', 'SKILLS', 'RESUME', 'SOCIAL_LINKS', 'CONTACT'].includes(type)
}

function supportsLead(type) {
  return type !== 'RICH_TEXT'
}

function supportsMarkdown(type) {
  return ['RICH_TEXT', 'MEDIA', 'MEDIA_TEXT'].includes(type)
}

function supportsAction(type) {
  return ['HERO', 'CALL_TO_ACTION', 'COLLECTION', 'SKILLS', 'RESUME', 'SOCIAL_LINKS', 'CONTACT'].includes(type)
}

function supportsAlt(type) {
  return ['HERO', 'MEDIA', 'MEDIA_TEXT'].includes(type)
}

const activeTranslations = computed(() => blocks.value.map((block) => ({
  fa: complete(block, block.fa),
  en: complete(block, block.en)
})))

const previewBlocks = computed(() => blocks.value.map((block) => ({
  ...block,
  ...block[locale.value],
  settings: block.settings ?? {}
})))

function complete(block, value = {}) {
  const hasEditorialText = Boolean(value.title || value.lead || value.bodyMarkdown)
  if (block.type === 'MEDIA') return Boolean(value.alt)
  if (block.type === 'CALL_TO_ACTION') return Boolean(value.actionLabel && value.actionPath)
  if (block.type === 'MEDIA_TEXT') return Boolean(hasEditorialText && value.alt)
  return hasEditorialText
}

function translation(value = {}) {
  return {
    title: '', eyebrow: '', lead: '', bodyMarkdown: '', actionLabel: '', actionPath: '', alt: '', ...value
  }
}

function defaultBlock() {
  return {
    id: null,
    type: 'RICH_TEXT',
    enabled: true,
    settings: {},
    fa: translation(),
    en: translation()
  }
}

function normalize(block) {
  let settings = {}
  try { settings = block.settingsJson ? JSON.parse(block.settingsJson) : {} }
  catch { settings = {} }
  return { ...defaultBlock(), ...block, settings, fa: translation(block.fa), en: translation(block.en) }
}

function replaceBlocks(value) {
  replacing.value = true
  blocks.value = value.map(normalize)
  changes.markSaved()
  queueMicrotask(() => { replacing.value = false })
}

async function load() {
  if (!props.pageId) {
    blocks.value = []
    version.value = null
    return
  }
  loading.value = true
  error.value = null
  try {
    const response = await httpClient.get(`/api/v1/admin/pages/${props.pageId}/blocks`)
    version.value = response.data.version
    replaceBlocks(response.data.blocks ?? [])
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { loading.value = false }
}

function announce(message) {
  operationStatus.value = ''
  nextTick(() => { operationStatus.value = message })
}
function add() {
  blocks.value.push(defaultBlock())
  announce(t('admin.composer.added', { index: blocks.value.length }))
}
function requestRemove(index) { pendingRemoval.value = index }
function remove() {
  if (pendingRemoval.value === null) return
  const removedIndex = pendingRemoval.value
  blocks.value.splice(pendingRemoval.value, 1)
  pendingRemoval.value = null
  announce(t('admin.composer.removed', { index: removedIndex + 1 }))
}
function move(index, offset) {
  const target = index + offset
  if (target < 0 || target >= blocks.value.length) return
  const [block] = blocks.value.splice(index, 1)
  blocks.value.splice(target, 0, block)
  announce(t('admin.composer.moved', { from: index + 1, to: target + 1 }))
  nextTick(() => document.querySelector(`[data-composer-block-index="${target}"]`)?.focus())
}

function onBlockTypeChange(block) {
  const allowed = block.type === 'COLLECTION'
    ? new Set(['source', 'limit'])
    : ['HERO', 'MEDIA', 'MEDIA_TEXT'].includes(block.type)
      ? new Set(['mediaId'])
      : new Set()
  block.settings = Object.fromEntries(Object.entries(block.settings ?? {}).filter(([key]) => allowed.has(key)))
}

function fieldError(index, field) {
  return fieldErrors.value[`blocks[${index}].${locale.value}.${field}`]
    ?? fieldErrors.value[`blocks.${index}.${locale.value}.${field}`]
    ?? null
}

function actionPathIsValid(value) {
  return !value || /^\/(fa|en)(?:\/|$)/.test(value) || /^https:\/\//.test(value)
}

function validateActionPaths() {
  const invalid = {}
  blocks.value.forEach((block, index) => {
    for (const currentLocale of ['fa', 'en']) {
      if (!actionPathIsValid(block[currentLocale]?.actionPath)) {
        invalid[`blocks[${index}].${currentLocale}.actionPath`] = t('admin.composer.invalidActionPath')
      }
    }
  })
  fieldErrors.value = invalid
  return Object.keys(invalid).length === 0
}

function settingsPayload(settings) {
  const compact = Object.fromEntries(Object.entries(settings ?? {}).filter(([, value]) => value !== '' && value !== null && value !== undefined))
  return Object.keys(compact).length ? JSON.stringify(compact) : null
}

async function save() {
  if (!props.pageId) return
  if (!validateActionPaths()) {
    error.value = { message: t('admin.composer.invalidActionPath') }
    return
  }
  saving.value = true
  error.value = null
  fieldErrors.value = {}
  try {
    await primeCsrfToken(httpClient)
    const response = await httpClient.put(`/api/v1/admin/pages/${props.pageId}/blocks`, {
      version: version.value ?? props.pageVersion,
      blocks: blocks.value.map((block) => ({
        type: block.type,
        enabled: block.enabled,
        settingsJson: settingsPayload(block.settings),
        fa: block.fa,
        en: block.en
      }))
    })
    version.value = response.data.version
    replaceBlocks(response.data.blocks ?? [])
    emit('saved', response.data.version)
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    fieldErrors.value = mapValidationErrors(error.value)
  }
  finally { saving.value = false }
}

watch(() => props.pageId, () => { void load() }, { immediate: true })
watch(blocks, () => { if (!replacing.value) changes.markDirty() }, { deep: true, flush: 'sync' })
onBeforeRouteLeave(async () => changes.confirmLeave())
</script>

<template>
  <section v-if="pageId" class="q-mt-xl" aria-labelledby="page-composer-heading">
    <div class="row items-center justify-between q-col-gutter-md q-mb-md">
      <div class="col">
        <h2 id="page-composer-heading" class="text-h6 q-my-none">{{ t('admin.composer.title') }}</h2>
        <p class="text-body2 text-grey-8 q-mb-none">{{ t('admin.composer.description') }}</p>
      </div>
      <div class="col-auto q-gutter-sm">
        <q-btn outline color="primary" icon="visibility" :label="t('admin.composer.preview')" :disable="loading" @click="previewOpen = true" />
        <q-btn outline color="primary" icon="add" :label="t('admin.composer.add')" :disable="disable || loading" @click="add" />
      </div>
    </div>

    <p class="admin-composer__status" role="status" aria-live="polite">{{ operationStatus }}</p>

    <q-banner v-if="error" class="bg-red-1 text-negative q-mb-md" rounded role="alert">
      {{ error.message }}
      <q-btn v-if="isVersionConflict(error)" flat color="negative" :label="t('admin.composer.reload')" @click="load" />
    </q-banner>

    <q-inner-loading :showing="loading" />
    <p v-if="!loading && blocks.length === 0" class="text-grey-8">{{ t('admin.composer.empty') }}</p>
    <q-card v-for="(block, index) in blocks" :key="block.id ?? `new-${index}`" flat bordered tabindex="-1" class="admin-composer__block q-mb-md" :data-composer-block-index="index" :aria-labelledby="`composer-block-${index}`">
      <q-card-section class="admin-composer__block-header row items-center q-col-gutter-sm">
        <div class="col-12 col-md">
          <h3 :id="`composer-block-${index}`" class="text-subtitle1 q-my-none">
            {{ t('admin.composer.blockHeading', { type: t(`admin.composer.blockTypes.${block.type}`), index: index + 1 }) }}
          </h3>
        </div>
        <div class="col-12 col-md"><q-select v-model="block.type" :options="blockOptions" emit-value map-options :label="t('admin.composer.type')" :disable="disable || saving" @update:model-value="onBlockTypeChange(block)" /></div>
        <div class="col-auto"><q-toggle v-model="block.enabled" :label="t('admin.composer.visible')" :disable="disable || saving" /></div>
        <div class="col-auto q-gutter-xs">
          <q-btn class="admin-composer__move" flat round icon="keyboard_arrow_up" :disable="index === 0 || disable || saving" :aria-label="t('admin.composer.moveUp', { index: index + 1 })" @click="move(index, -1)" />
          <q-btn class="admin-composer__move" flat round icon="keyboard_arrow_down" :disable="index === blocks.length - 1 || disable || saving" :aria-label="t('admin.composer.moveDown', { index: index + 1 })" @click="move(index, 1)" />
          <q-btn flat round color="negative" icon="delete" :aria-label="t('admin.composer.remove', { index: index + 1 })" :disable="disable || saving" @click="requestRemove(index)" />
        </div>
      </q-card-section>
      <q-card-section class="q-pt-none">
        <AdminMediaSelector v-if="['HERO', 'MEDIA', 'MEDIA_TEXT'].includes(block.type)" v-model="block.settings.mediaId" :label="t('admin.composer.media')" :disable="disable || saving" />
        <template v-if="block.type === 'COLLECTION'">
          <q-select v-model="block.settings.source" :options="collectionOptions" emit-value map-options :label="t('admin.composer.collection')" :disable="disable || saving" />
          <q-input v-model.number="block.settings.limit" type="number" min="1" max="12" :label="t('admin.composer.limit')" :disable="disable || saving" />
        </template>
        <AdminLocaleTabs v-model="locale" :translations="activeTranslations[index]" />
        <template v-if="locale === 'fa'">
          <q-input v-if="supportsEyebrow(block.type)" v-model="block.fa.eyebrow" :label="t('admin.composer.eyebrowFa')" :disable="disable || saving" />
          <q-input v-model="block.fa.title" :label="t('admin.composer.titleFa')" :disable="disable || saving" />
          <q-input v-if="supportsLead(block.type)" v-model="block.fa.lead" type="textarea" :label="t('admin.composer.leadFa')" :disable="disable || saving" />
          <AdminMarkdownPreview v-if="supportsMarkdown(block.type)" v-model="block.fa.bodyMarkdown" />
          <q-input v-if="supportsAction(block.type)" v-model="block.fa.actionLabel" :label="t('admin.composer.actionLabelFa')" :disable="disable || saving" />
          <q-input v-if="supportsAction(block.type)" v-model="block.fa.actionPath" :label="t('admin.composer.actionPathFa')" :hint="t('admin.composer.actionHint')" :error="Boolean(fieldError(index, 'actionPath'))" :error-message="fieldError(index, 'actionPath')" :disable="disable || saving" />
          <q-input v-if="supportsAlt(block.type)" v-model="block.fa.alt" :label="t('admin.composer.altFa')" :disable="disable || saving" />
        </template>
        <template v-else>
          <q-input v-if="supportsEyebrow(block.type)" v-model="block.en.eyebrow" :label="t('admin.composer.eyebrowEn')" :disable="disable || saving" />
          <q-input v-model="block.en.title" :label="t('admin.composer.titleEn')" :disable="disable || saving" />
          <q-input v-if="supportsLead(block.type)" v-model="block.en.lead" type="textarea" :label="t('admin.composer.leadEn')" :disable="disable || saving" />
          <AdminMarkdownPreview v-if="supportsMarkdown(block.type)" v-model="block.en.bodyMarkdown" />
          <q-input v-if="supportsAction(block.type)" v-model="block.en.actionLabel" :label="t('admin.composer.actionLabelEn')" :disable="disable || saving" />
          <q-input v-if="supportsAction(block.type)" v-model="block.en.actionPath" :label="t('admin.composer.actionPathEn')" :hint="t('admin.composer.actionHint')" :error="Boolean(fieldError(index, 'actionPath'))" :error-message="fieldError(index, 'actionPath')" :disable="disable || saving" />
          <q-input v-if="supportsAlt(block.type)" v-model="block.en.alt" :label="t('admin.composer.altEn')" :disable="disable || saving" />
        </template>
      </q-card-section>
    </q-card>
    <q-btn color="primary" :label="t('admin.composer.save')" :loading="saving" :disable="disable || loading" @click="save" />
    <q-dialog v-model="previewOpen" maximized>
      <q-card class="admin-composer__preview" role="document">
        <q-card-section class="row items-center justify-between">
          <h3 class="text-h6 q-my-none">{{ t('admin.composer.previewTitle') }}</h3>
          <q-btn flat round icon="close" :aria-label="t('admin.composer.closePreview')" @click="previewOpen = false" />
        </q-card-section>
        <q-separator />
        <q-card-section class="q-pa-none">
          <PageBlockRenderer :blocks="previewBlocks" :locale="locale" :hero-heading-level="2" />
        </q-card-section>
      </q-card>
    </q-dialog>
    <q-dialog v-model="removalOpen" persistent>
      <q-card>
        <q-card-section class="text-h6">{{ t('admin.composer.removeTitle') }}</q-card-section>
        <q-card-section>{{ t('admin.composer.removeDescription') }}</q-card-section>
        <q-card-actions align="right">
          <q-btn flat :label="t('admin.composer.cancel')" @click="pendingRemoval = null" />
          <q-btn color="negative" :label="t('admin.composer.removeConfirm')" @click="remove" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </section>
</template>

<style scoped>
.admin-composer__block {
  overflow: hidden;
}

.admin-composer__status {
  position: absolute;
  inline-size: 1px;
  block-size: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  clip-path: inset(50%);
  white-space: nowrap;
}

.admin-composer__block-header {
  background: var(--tm-admin-surface-subtle);
  border-block-end: 1px solid var(--tm-admin-border);
}

.admin-composer__move:focus-visible {
  outline: 3px solid var(--tm-focus-ring);
  outline-offset: 2px;
}
</style>
