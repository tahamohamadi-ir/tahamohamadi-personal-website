<script setup>
import { computed, inject, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

import MarkdownContent from 'src/components/content/MarkdownContent.vue'
import HomeClosing from 'src/components/public/home/HomeClosing.vue'
import HomeFeatured from 'src/components/public/home/HomeFeatured.vue'
import HomeHero from 'src/components/public/home/HomeHero.vue'
import HomePractice from 'src/components/public/home/HomePractice.vue'
import HomeResearchWriting from 'src/components/public/home/HomeResearchWriting.vue'
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

const api = inject(PUBLIC_API_KEY, null)
const route = useRoute()
const { t, locale: activeLocale } = useI18n()
const locale = computed(() => (
  route.meta.locale ||
  props.initialData?.locale ||
  activeLocale.value ||
  'en'
))
const ssrKey = computed(() => `public:${locale.value}:home:home`)

function isEmptyHome(value) {
  const page = value?.page

  return !page || ![
    page.summary,
    page.bodyMarkdown
  ].some((field) => typeof field === 'string' && field.trim().length > 0)
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
const featuredItems = computed(() => data.value?.featured?.items ?? [])
const socialLinks = computed(() => data.value?.socialLinks?.items ?? [])

const showsCmsContent = computed(() => [
  page.value?.summary,
  page.value?.bodyMarkdown
].some((field) => typeof field === 'string' && field.trim().length > 0))

const alternatePath = computed(() => error.value?.alternatePaths?.[0] ?? null)

function retry() {
  return state.value === 'stale' ? refresh() : load()
}

function featuredPath(item) {
  if (item?.targetType === 'PORTFOLIO_PROJECT') {
    return `/${locale.value}/portfolio/${item.slug}`
  }

  if (item?.targetType === 'PUBLICATION') {
    return `/${locale.value}/publications/${item.slug}`
  }

  return null
}

const featuredEntries = computed(() => featuredItems.value
  .map((item) => ({
    ...item,
    path: featuredPath(item)
  }))
  .filter((item) => item.path))

usePublicSeoMeta({ data, state })

onMounted(() => {
  if (!hasInitialState) {
    void load()
  }
})
</script>

<template>
  <div class="public-home">
    <HomeHero :locale="locale">
      <template #identity>
        <h1 id="home-title" class="home-hero__identity">
          {{ t('shell.siteName') }}
        </h1>
      </template>
    </HomeHero>

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

    <HomePractice />

    <section
      v-if="showsCmsContent"
      class="public-home__profile"
      aria-labelledby="profile-title"
    >
      <div class="tm-container public-home__profile-grid">
        <header>
          <p>{{ t('public.home.profileLabel') }}</p>
          <h2 id="profile-title">
            {{ t('public.home.profileTitle') }}
          </h2>
        </header>

        <div>
          <p
            v-if="page?.summary"
            class="tm-page-copy"
          >
            {{ page.summary }}
          </p>

          <MarkdownContent
            v-if="page?.bodyMarkdown"
            :markdown="page.bodyMarkdown"
          >
            <template #error>
              <p class="tm-page-copy" role="alert">
                {{ t('public.richContent.renderingFailure') }}
              </p>
            </template>
          </MarkdownContent>
        </div>
      </div>
    </section>

    <HomeFeatured :entries="featuredEntries" />

    <HomeResearchWriting :locale="locale" />

    <HomeClosing
      :locale="locale"
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

.public-home__profile {
  padding-block: clamp(var(--tm-space-8), 7vw, var(--tm-space-14));
  border-block-end: 1px solid var(--tm-border-subtle);
}

.public-home__profile-grid {
  display: grid;
  gap: var(--tm-space-7);
}

.public-home__profile header p {
  margin: 0 0 var(--tm-space-3);
  color: var(--tm-action-primary);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.public-home__profile h2 {
  margin: 0;
  font-size: clamp(2rem, 4vw, 3.25rem);
  letter-spacing: -0.04em;
}

.public-home__profile .tm-page-copy {
  max-inline-size: var(--tm-prose-max-width);
  margin-block-start: 0;
  color: var(--tm-text-secondary);
  font-size: 1.125rem;
  line-height: 1.75;
}

@media (min-width: 900px) {
  .public-home__profile-grid {
    grid-template-columns: minmax(12rem, 0.4fr) minmax(0, 1fr);
    align-items: start;
  }
}
</style>
