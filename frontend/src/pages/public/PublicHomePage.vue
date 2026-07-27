<script setup>
import { computed, inject, onMounted } from 'vue'
import { useRoute } from 'vue-router'

import PageBlockRenderer from 'src/components/public/PageBlockRenderer.vue'
import PageState from 'src/components/public/PageState.vue'
import TranslationUnavailable from 'src/components/public/TranslationUnavailable.vue'
import HomeHero from 'src/components/public/HomeHero.vue'
import MarkdownContent from 'src/components/content/MarkdownContent.vue'
import { useAsyncPage } from 'src/composables/useAsyncPage'
import { usePublicSeoMeta } from 'src/composables/usePublicSeoMeta'
import { PUBLIC_API_KEY } from 'src/services/apiContext'

const props = defineProps({
  initialData: {
    type: Object,
    default: undefined
  }
})

const api = inject(PUBLIC_API_KEY, null)
const route = useRoute()
const activeLocale = computed(() => route.meta.locale || props.initialData?.locale || 'en')
const locale = computed(() => (
  activeLocale.value
))
const ssrKey = computed(() => `public:${locale.value}:home:home`)

function isEmptyHome(value) {
  const page = value?.page
  if (!page) return true

  const hasText = [
    page.summary,
    page.bodyMarkdown
  ].some((field) => typeof field === 'string' && field.trim().length > 0)
  return !hasText && !(Array.isArray(page.blocks) && page.blocks.length > 0)
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
  load: (currentApi) => currentApi.getHome(locale.value),
  isEmpty: isEmptyHome,
  initialData: props.initialData,
  ssrKey: () => ssrKey.value
})

const page = computed(() => data.value?.page ?? null)
const homeBlocks = computed(() => page.value?.blocks ?? [])
const collectionItems = computed(() => ({
  BLOG: data.value?.latestPosts ?? [],
  PORTFOLIO: data.value?.selectedProjects ?? [],
  PUBLICATIONS: data.value?.selectedPublications ?? []
}))
const skills = computed(() => data.value?.skills?.items ?? [])
const socialLinks = computed(() => data.value?.socialLinks?.items ?? [])
const hasHero = computed(() => homeBlocks.value.some((block) => (
  block?.enabled !== false
  && String(block?.type).toLowerCase() === 'hero'
  && typeof block?.title === 'string'
  && block.title.trim().length > 0
)))
const hasLegacyManagedContent = computed(() => (
  !homeBlocks.value.length
  && Boolean(page.value?.summary?.trim() || page.value?.bodyMarkdown?.trim())
))

const alternatePath = computed(() => error.value?.alternatePaths?.[0] ?? null)

function retry() {
  return state.value === 'stale' ? refresh() : load()
}

usePublicSeoMeta({ data, state })

onMounted(() => {
  if (!hasInitialState) {
    void load()
  }
})
</script>

<template>
  <div class="public-home">
    <div
      v-if="state && state !== 'translation-unavailable' && state !== 'empty'"
      class="tm-container public-home__status"
    >
      <PageState
        :state="state"
        @retry="retry"
      />
    </div>

    <div
      v-else-if="state === 'translation-unavailable'"
      class="tm-container public-home__status"
    >
      <TranslationUnavailable
        :alternate-path="alternatePath"
        :target-locale="locale"
      />
    </div>

    <HomeHero
      v-else-if="page && !hasHero && !hasLegacyManagedContent"
      :page="page"
      :locale="locale"
    />

    <div
      v-else-if="hasLegacyManagedContent"
      class="tm-container tm-rich-content"
    >
      <h1 class="tm-page-title">
        {{ page.title }}
      </h1>
      <p
        v-if="page.summary"
        class="public-home__summary"
      >
        {{ page.summary }}
      </p>
      <MarkdownContent
        v-if="page.bodyMarkdown"
        :markdown="page.bodyMarkdown"
      />
    </div>

    <PageBlockRenderer
      v-if="homeBlocks.length"
      :blocks="homeBlocks"
      :collection-items="collectionItems"
      :hero-heading-level="1"
      :locale="locale"
      :skills="skills"
      :social-links="socialLinks"
    />
  </div>
</template>

<style scoped>
.public-home {
  background: var(--tm-surface);
}

.public-home__status {
  padding-block: var(--tm-space-5);
}

.public-home__summary {
  color: var(--tm-text-secondary);
  font-size: clamp(1.125rem, 2vw, 1.375rem);
  line-height: 1.7;
  margin: 0;
  max-inline-size: 58ch;
}

</style>
