<script setup>
import { computed, inject, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import MarkdownContent from 'src/components/content/MarkdownContent.vue'
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
const ssrKey = computed(() => `public:${locale.value}:post:${slug.value}`)

function isEmptyPost(value) {
  return typeof value?.title !== 'string' || value.title.trim().length === 0
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
  load: (currentApi) => currentApi.getPost(locale.value, slug.value),
  isEmpty: isEmptyPost,
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
  <section class="tm-detail-page tm-container" aria-labelledby="blog-post-title">
    <header class="tm-detail-page__header">
      <h1 id="blog-post-title" class="tm-page-title">
        {{ data?.title ?? t('shell.navigation.blog') }}
      </h1>
      <p v-if="data?.excerpt" class="tm-page-copy">{{ data.excerpt }}</p>
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
      <dl v-if="data?.publishedAt || data?.lastModified" class="tm-detail-page__metadata">
        <div v-if="data?.publishedAt">
          <dt>{{ t('public.detail.published') }}</dt>
          <dd><time :datetime="data.publishedAt"><bdi>{{ data.publishedAt }}</bdi></time></dd>
        </div>
        <div v-if="data?.lastModified">
          <dt>{{ t('public.detail.updated') }}</dt>
          <dd><time :datetime="data.lastModified"><bdi>{{ data.lastModified }}</bdi></time></dd>
        </div>
      </dl>

      <MarkdownContent v-if="data?.bodyMarkdown" :markdown="data.bodyMarkdown">
        <template #error>
          <p class="tm-page-copy" role="alert">
            {{ t('public.richContent.renderingFailure') }}
          </p>
        </template>
      </MarkdownContent>
    </article>
  </section>
</template>
