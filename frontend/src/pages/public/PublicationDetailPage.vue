<script setup>
import { computed, inject, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import PageState from 'src/components/public/PageState.vue'
import TranslationUnavailable from 'src/components/public/TranslationUnavailable.vue'
import { useAsyncPage } from 'src/composables/useAsyncPage'
import { usePublicSeoMeta } from 'src/composables/usePublicSeoMeta'
import { PUBLIC_API_KEY } from 'src/services/apiContext'

const props = defineProps({
  initialData: {
    type: Object,
    default: undefined
  }
})

const api = inject(PUBLIC_API_KEY)
const route = useRoute()
const { t } = useI18n()
const locale = computed(() => route.meta.locale)
const slug = computed(() => String(route.params.slug ?? ''))
const ssrKey = computed(() => `public:${locale.value}:publication:${slug.value}`)

function isEmptyPublication(value) {
  return typeof value?.title !== 'string' || value.title.trim().length === 0
}

function safeExternalUrl(value) {
  if (typeof value !== 'string') {
    return null
  }

  try {
    const url = new URL(value)
    return ['https:', 'http:'].includes(url.protocol) ? url.href : null
  }
  catch {
    return null
  }
}

const {
  data,
  state,
  error,
  load,
  refresh,
  hasInitialState
} = useAsyncPage({
  api,
  load: (currentApi) => currentApi.getPublication(locale.value, slug.value),
  isEmpty: isEmptyPublication,
  initialData: props.initialData,
  ssrKey: () => ssrKey.value
})

const showsContent = computed(() => (
  data.value !== null && state.value !== 'empty'
))
const isNotFound = computed(() => (
  state.value === 'recoverable-failure' && error.value?.status === 404
))
const seoState = computed(() => isNotFound.value ? 'not-found' : state.value)
usePublicSeoMeta({ data, state: seoState })
const alternatePath = computed(() => error.value?.alternatePaths?.[0] ?? null)
const externalUrl = computed(() => safeExternalUrl(data.value?.externalUrl))

function retry() {
  return state.value === 'stale' ? refresh() : load()
}

onMounted(() => {
  if (!hasInitialState) {
    void load()
  }
})
</script>

<template>
  <section class="tm-detail-page tm-container" aria-labelledby="publication-title">
    <header class="tm-detail-page__header">
      <h1 id="publication-title" class="tm-page-title">
        {{ data?.title ?? t('shell.navigation.publications') }}
      </h1>
    </header>

    <PageState
      v-if="state && state !== 'translation-unavailable' && !isNotFound"
      :state="state"
      @retry="retry"
    />

    <TranslationUnavailable
      v-else-if="state === 'translation-unavailable'"
      :alternate-path="alternatePath"
      :target-locale="locale"
    />

    <p v-else-if="isNotFound" class="tm-page-copy" role="alert">
      {{ t('public.notFoundDescription') }}
    </p>

    <article v-if="showsContent" class="tm-detail-page__content">
      <dl class="tm-detail-page__metadata">
        <div v-if="data?.authorsDisplay">
          <dt>{{ t('public.detail.authors') }}</dt>
          <dd>{{ data.authorsDisplay }}</dd>
        </div>
        <div v-if="data?.venueDisplay">
          <dt>{{ t('public.detail.venue') }}</dt>
          <dd>{{ data.venueDisplay }}</dd>
        </div>
        <div v-if="data?.publishedOn">
          <dt>{{ t('public.detail.published') }}</dt>
          <dd><time :datetime="data.publishedOn"><bdi>{{ data.publishedOn }}</bdi></time></dd>
        </div>
        <div v-else-if="data?.year">
          <dt>{{ t('public.detail.year') }}</dt>
          <dd><bdi>{{ data.year }}</bdi></dd>
        </div>
        <div v-if="data?.stage">
          <dt>{{ t('public.detail.stage') }}</dt>
          <dd><bdi>{{ data.stage }}</bdi></dd>
        </div>
        <div v-if="data?.doi">
          <dt>{{ t('public.detail.doi') }}</dt>
          <dd class="tm-detail-page__identifier"><bdi>{{ data.doi }}</bdi></dd>
        </div>
        <div v-if="data?.lastModified">
          <dt>{{ t('public.detail.updated') }}</dt>
          <dd><time :datetime="data.lastModified"><bdi>{{ data.lastModified }}</bdi></time></dd>
        </div>
      </dl>

      <p v-if="data?.abstractText" class="tm-page-copy">{{ data.abstractText }}</p>
      <a v-if="externalUrl" class="tm-text-link" :href="externalUrl">
        {{ t('public.detail.externalLink') }}
      </a>
    </article>
  </section>
</template>
