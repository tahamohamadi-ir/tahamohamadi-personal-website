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
const ssrKey = computed(() => `public:${locale.value}:project:${slug.value}`)

function isEmptyProject(value) {
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
  load: (currentApi) => currentApi.getProject(locale.value, slug.value),
  isEmpty: isEmptyProject,
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
  <section class="tm-detail-page tm-container" aria-labelledby="portfolio-project-title">
    <header class="tm-detail-page__header portfolio-project-detail__header">
      <h1 id="portfolio-project-title" class="tm-page-title">
        {{ data?.title ?? t('shell.navigation.portfolio') }}
      </h1>
      <p v-if="data?.summary" class="tm-page-copy">{{ data.summary }}</p>
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

    <article v-if="showsContent" class="tm-detail-page__content portfolio-project-detail__content">
      <dl v-if="data?.roleText || data?.clientLabel || data?.teamDescription || data?.outcomeText" class="tm-detail-page__metadata portfolio-project-detail__facts">
        <div v-if="data?.roleText"><dt>{{ t('public.caseStudy.role') }}</dt><dd>{{ data.roleText }}</dd></div>
        <div v-if="data?.clientLabel"><dt>{{ t('public.caseStudy.client') }}</dt><dd>{{ data.clientLabel }}</dd></div>
        <div v-if="data?.teamDescription"><dt>{{ t('public.caseStudy.team') }}</dt><dd>{{ data.teamDescription }}</dd></div>
        <div v-if="data?.outcomeText"><dt>{{ t('public.caseStudy.outcome') }}</dt><dd>{{ data.outcomeText }}</dd></div>
      </dl>
      <dl v-if="data?.lastModified" class="tm-detail-page__metadata portfolio-project-detail__updated">
        <div>
          <dt>{{ t('public.detail.updated') }}</dt>
          <dd><time :datetime="data.lastModified"><bdi>{{ data.lastModified }}</bdi></time></dd>
        </div>
      </dl>

      <section v-if="data?.gallery?.length" class="tm-detail-page__gallery" :aria-label="t('public.caseStudy.gallery')">
        <img v-for="item in data.gallery" :key="item.mediaAssetId" :src="item.url" alt="" loading="lazy" decoding="async">
      </section>

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

<style scoped>
.portfolio-project-detail__header {
  gap: var(--tm-space-5);
  padding-block-end: var(--tm-space-6);
  border-block-end: 1px solid var(--tm-editorial-rule);
}

.portfolio-project-detail__content {
  gap: var(--tm-space-6);
}

.portfolio-project-detail__facts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 14rem), 1fr));
  gap: 1px;
  overflow: hidden;
  border: 1px solid var(--tm-editorial-rule);
  border-radius: var(--tm-radius-card);
  background: var(--tm-editorial-rule);
}

.portfolio-project-detail__facts > div,
.portfolio-project-detail__updated > div {
  display: grid;
  gap: var(--tm-space-1);
  padding: var(--tm-space-4);
  background: var(--tm-editorial-card-surface);
}

.portfolio-project-detail__facts dt,
.portfolio-project-detail__updated dt {
  color: var(--tm-text-secondary);
  font-size: .875rem;
  font-weight: 650;
}

.portfolio-project-detail__facts dd,
.portfolio-project-detail__updated dd {
  color: var(--tm-text-primary);
}

.portfolio-project-detail__updated {
  border-inline-start: 2px solid var(--tm-navigation-current-indicator);
}

.tm-detail-page__gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 15rem), 1fr));
  gap: var(--tm-space-3);
  margin: 0;
}

.tm-detail-page__gallery img {
  display: block;
  inline-size: 100%;
  aspect-ratio: 4 / 3;
  border: 1px solid var(--tm-editorial-rule);
  border-radius: var(--tm-radius-card);
  background: var(--tm-editorial-muted-surface);
  object-fit: cover;
}
</style>
