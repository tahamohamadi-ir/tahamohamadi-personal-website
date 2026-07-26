<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import AdminStatePanel from 'src/components/admin/AdminStatePanel.vue'
import { formatLocalizedDate } from 'src/utils/formatDate'
import { HTTP_CLIENT_KEY } from 'src/services/apiContext'
import { normalizeApiError } from 'src/services/httpClient'

const httpClient = inject(HTTP_CLIENT_KEY)
const { t, locale } = useI18n()
const state = ref('loading')
const error = ref(null)
const items = ref([])
const selected = ref(null)
const detail = ref(null)
const detailLoading = ref(false)
const filter = ref('NEEDS_ATTENTION')

const filterOptions = computed(() => [
  { label: t('admin.translationQueue.needsAttention'), value: 'NEEDS_ATTENTION' },
  { label: t('admin.translationQueue.missing'), value: 'MISSING' },
  { label: t('admin.translationQueue.outdated'), value: 'OUTDATED' },
  { label: t('admin.translationQueue.all'), value: 'ALL' }
])
const visibleItems = computed(() => items.value.filter((item) => {
  const states = [item.faTranslationStatus?.status, item.enTranslationStatus?.status]
  if (filter.value === 'ALL') return true
  if (filter.value === 'NEEDS_ATTENTION') return states.some((value) => value === 'MISSING' || value === 'INCOMPLETE' || value === 'OUTDATED')
  return states.includes(filter.value)
}))

function status(localeCode) {
  return selected.value?.[`${localeCode}TranslationStatus`]?.status ?? 'MISSING'
}

function checklist(localeCode) {
  const value = detail.value?.[localeCode] ?? {}
  return [
    ['title', Boolean(value.title?.trim())],
    ['slug', Boolean(value.slug?.trim())],
    ['body', Boolean(value.bodyMarkdown?.trim())],
    ['seo', Boolean(value.seoTitle?.trim() && value.seoDescription?.trim())]
  ]
}

async function load() {
  state.value = 'loading'
  error.value = null
  try {
    const response = await httpClient.get('/api/v1/admin/blog/posts', { params: { page: 0, size: 100 } })
    items.value = response.data.items ?? []
    state.value = items.value.length ? 'ready' : 'empty'
  }
  catch (cause) {
    error.value = normalizeApiError(cause)
    state.value = 'error'
  }
}

async function select(item) {
  selected.value = item
  detail.value = null
  detailLoading.value = true
  try {
    detail.value = (await httpClient.get(`/api/v1/admin/blog/posts/${item.id}`)).data
  }
  catch (cause) { error.value = normalizeApiError(cause) }
  finally { detailLoading.value = false }
}

onMounted(() => { void load() })
</script>

<template>
  <q-page class="admin-page admin-translation-queue">
    <header class="admin-page__header">
      <div>
        <h1 class="text-h4 q-my-none">{{ t('admin.translationQueue.title') }}</h1>
        <p class="admin-page__description">{{ t('admin.translationQueue.description') }}</p>
      </div>
      <q-select v-model="filter" outlined dense emit-value map-options :options="filterOptions" :label="t('admin.translationQueue.filter')" />
    </header>

    <q-banner v-if="error" class="bg-red-1 text-negative" role="alert">{{ error.message }}</q-banner>
    <AdminStatePanel v-if="state !== 'ready'" :state="state" @retry="load" />

    <section v-else class="admin-translation-queue__workspace">
      <q-list bordered separator aria-label="Translation queue">
        <q-item v-for="item in visibleItems" :key="item.id" clickable :active="selected?.id === item.id" active-class="admin-translation-queue__item--active" @click="select(item)">
          <q-item-section>
            <q-item-label>{{ item.sourceTitle || t('admin.translationQueue.untitled') }}</q-item-label>
            <q-item-label caption>{{ t('admin.translationQueue.sourceUpdated', { date: item.sourceUpdatedAt ? formatLocalizedDate(item.sourceUpdatedAt, locale) : t('admin.translationQueue.unavailable') }) }}</q-item-label>
          </q-item-section>
          <q-item-section side top class="admin-translation-queue__badges">
            <q-badge outline :color="item.faTranslationStatus?.status === 'COMPLETE' ? 'positive' : 'warning'">FA · {{ t(`admin.blogTranslationStatuses.${item.faTranslationStatus?.status}`) }}</q-badge>
            <q-badge outline :color="item.enTranslationStatus?.status === 'COMPLETE' ? 'positive' : 'warning'">EN · {{ t(`admin.blogTranslationStatuses.${item.enTranslationStatus?.status}`) }}</q-badge>
          </q-item-section>
        </q-item>
        <q-item v-if="visibleItems.length === 0"><q-item-section>{{ t('admin.translationQueue.empty') }}</q-item-section></q-item>
      </q-list>

      <section class="admin-panel admin-translation-queue__comparison" :aria-busy="detailLoading">
        <template v-if="selected">
          <header class="admin-translation-queue__comparison-header">
            <div><h2 class="text-h6 q-my-none">{{ selected.sourceTitle || t('admin.translationQueue.untitled') }}</h2><p class="text-caption q-mb-none">{{ t('admin.translationQueue.sourceLanguage', { locale: selected.sourceLanguage?.toUpperCase() }) }}</p></div>
            <q-btn outline no-caps icon="edit" :label="t('admin.translationQueue.openEditor')" :to="`/admin/blog/posts?post=${selected.id}`" />
          </header>
          <q-inner-loading :showing="detailLoading" />
          <div v-if="detail" class="admin-translation-queue__locales">
            <article v-for="localeCode in ['fa', 'en']" :key="localeCode" class="admin-translation-queue__locale">
              <div class="row items-center justify-between q-gutter-sm"><h3 class="text-subtitle1 q-my-none">{{ localeCode.toUpperCase() }}</h3><q-badge outline>{{ t(`admin.blogTranslationStatuses.${status(localeCode)}`) }}</q-badge></div>
              <dl>
                <template v-for="[key, complete] in checklist(localeCode)" :key="key"><dt>{{ t(`admin.translationQueue.checklist.${key}`) }}</dt><dd><q-icon :name="complete ? 'check_circle' : 'error_outline'" :color="complete ? 'positive' : 'warning'" /> {{ complete ? t('admin.translationQueue.complete') : t('admin.translationQueue.incomplete') }}</dd></template>
              </dl>
              <p class="admin-translation-queue__excerpt">{{ detail[localeCode]?.excerpt || detail[localeCode]?.bodyMarkdown }}</p>
            </article>
          </div>
        </template>
        <p v-else class="text-body2 q-my-none">{{ t('admin.translationQueue.selectItem') }}</p>
      </section>
    </section>
  </q-page>
</template>

<style scoped>
.admin-translation-queue__workspace { display: grid; grid-template-columns: minmax(18rem, .85fr) minmax(0, 1.5fr); gap: var(--tm-admin-panel-gap); align-items: start; }
.admin-translation-queue__badges { align-items: flex-end; gap: var(--tm-space-1); }
.admin-translation-queue__comparison { display: grid; gap: var(--tm-space-5); min-block-size: 18rem; padding: var(--tm-space-5); }
.admin-translation-queue__comparison-header { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: var(--tm-space-3); }
.admin-translation-queue__locales { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--tm-space-4); }
.admin-translation-queue__locale { display: grid; gap: var(--tm-space-3); padding: var(--tm-space-4); border: 1px solid var(--tm-admin-border); border-radius: var(--tm-admin-control-radius); }
.admin-translation-queue__locale dl { display: grid; grid-template-columns: 1fr auto; gap: var(--tm-space-2); margin: 0; }
.admin-translation-queue__locale dd { display: flex; align-items: center; gap: var(--tm-space-1); margin: 0; }
.admin-translation-queue__excerpt { max-block-size: 9rem; margin: 0; overflow: auto; color: var(--tm-text-secondary); white-space: pre-wrap; }
:deep(.admin-translation-queue__item--active) { background: var(--tm-admin-nav-active); }
@media (max-width: 900px) { .admin-translation-queue__workspace, .admin-translation-queue__locales { grid-template-columns: 1fr; } }
</style>
