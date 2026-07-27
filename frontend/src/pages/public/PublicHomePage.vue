<script setup>
import { computed, inject, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

import BlogPostList from 'src/components/public/BlogPostList.vue'
import PageBlockRenderer from 'src/components/public/PageBlockRenderer.vue'
import PageState from 'src/components/public/PageState.vue'
import PortfolioProjectList from 'src/components/public/PortfolioProjectList.vue'
import TranslationUnavailable from 'src/components/public/TranslationUnavailable.vue'
import HomeHero from 'src/components/public/HomeHero.vue'
import MarkdownContent from 'src/components/content/MarkdownContent.vue'
import TmButton from 'src/components/shared/TmButton.vue'
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
const { t } = useI18n()
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
const latestPosts = computed(() => data.value?.latestPosts ?? [])
const selectedProjects = computed(() => data.value?.selectedProjects ?? [])
const selectedPublications = computed(() => data.value?.selectedPublications ?? [])
const workPath = computed(() => `/${locale.value}/portfolio`)
const writingPath = computed(() => `/${locale.value}/blog`)
const publicationsPath = computed(() => `/${locale.value}/publications`)
const contactPath = computed(() => `/${locale.value}/contact`)
const hasHero = computed(() => homeBlocks.value.some((block) => (
  block?.enabled !== false
  && String(block?.type).toLowerCase() === 'hero'
  && typeof block?.title === 'string'
  && block.title.trim().length > 0
)))
const hasLegacyManagedContent = computed(() => (
  !homeBlocks.value.length
  && !page.value?.title
  && Boolean(page.value?.bodyMarkdown?.trim())
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

    <section
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
    </section>

    <section
      v-else-if="page?.title && !hasHero"
      class="public-home__hero"
    >
      <div class="tm-container public-home__hero-content">
        <div class="public-home__hero-copy">
          <p class="public-home__eyebrow">
            TAHA MOHAMADI
          </p>
          <h1>{{ page.title }}</h1>
          <p
            v-if="page.summary"
            class="public-home__summary"
          >
            {{ page.summary }}
          </p>
          <MarkdownContent
            v-if="page.bodyMarkdown"
            class="tm-rich-content"
            :markdown="page.bodyMarkdown"
          />
          <div class="public-home__hero-actions">
            <TmButton
              variant="primary"
              :to="workPath"
            >
              {{ t('home.hero.exploreWork') }}
            </TmButton>
            <router-link
              :to="contactPath"
              class="public-home__contact-link tm-interactive"
            >
              {{ t('home.hero.contactMe') }}
            </router-link>
          </div>
        </div>
        <p
          class="public-home__hero-monogram"
          aria-hidden="true"
        >
          TM
        </p>
      </div>
    </section>

    <PageBlockRenderer
      v-if="homeBlocks.length"
      :blocks="homeBlocks"
      :collection-items="collectionItems"
      :hero-heading-level="1"
      :locale="locale"
      :skills="skills"
      :social-links="socialLinks"
    />

    <template v-else>
      <section
        v-if="selectedProjects.length"
        class="tm-container public-home__collection public-home__collection--work"
      >
        <div class="public-home__section-heading">
          <p class="public-home__eyebrow">
            {{ t('shell.navigation.work') }}
          </p>
          <h2>{{ t('home.collections.selectedWorkTitle') }}</h2>
        </div>
        <PortfolioProjectList
          :items="selectedProjects"
          :locale="locale"
        />
        <router-link
          :to="workPath"
          class="public-home__section-link tm-interactive"
        >
          {{ t('home.collections.viewAllWork') }}
        </router-link>
      </section>

      <section
        v-if="latestPosts.length"
        class="tm-container public-home__collection"
      >
        <div class="public-home__section-heading">
          <p class="public-home__eyebrow">
            {{ t('shell.navigation.writing') }}
          </p>
          <h2>{{ t('home.collections.latestWritingTitle') }}</h2>
        </div>
        <BlogPostList
          :items="latestPosts"
          :locale="locale"
        />
        <router-link
          :to="writingPath"
          class="public-home__section-link tm-interactive"
        >
          {{ t('home.collections.viewAllWriting') }}
        </router-link>
      </section>

      <section
        v-if="selectedPublications.length"
        class="tm-container public-home__collection"
      >
        <div class="public-home__section-heading">
          <p class="public-home__eyebrow">
            {{ t('shell.navigation.publications') }}
          </p>
          <h2>{{ t('home.collections.featuredPublicationsTitle') }}</h2>
        </div>
        <ul class="public-home__publication-list">
          <li
            v-for="item in selectedPublications"
            :key="item.slug"
            class="public-home__publication-item"
          >
            <article>
              <span class="public-home__publication-meta">{{ item.venue || item.stage }} {{ item.year }}</span>
              <h3>
                <router-link :to="`/${locale}/publications`">{{ item.title }}</router-link>
              </h3>
              <p v-if="item.abstractText">
                {{ item.abstractText }}
              </p>
            </article>
          </li>
        </ul>
        <router-link
          :to="publicationsPath"
          class="public-home__section-link tm-interactive"
        >
          {{ t('home.collections.viewAllPublications') }}
        </router-link>
      </section>
    </template>
  </div>
</template>

<style scoped>
.public-home {
  background: var(--tm-surface);
}

.public-home__title {
  padding-block: clamp(var(--tm-space-10), 10vw, var(--tm-space-18));
}

.public-home__title h1,
.public-home__legacy-content h1 {
  margin: 0;
  max-inline-size: 13ch;
  font-size: clamp(2.625rem, 6.1vw, 5.25rem);
  letter-spacing: -0.04em;
}

.public-home__legacy-content {
  display: grid;
  gap: var(--tm-space-5);
  max-inline-size: 72rem;
  padding-block: clamp(var(--tm-space-10), 10vw, var(--tm-space-18));
}

.public-home__hero {
  border-block-end: 1px solid var(--tm-editorial-rule);
  overflow: hidden;
}

.public-home__hero-content {
  display: grid;
  gap: var(--tm-space-6);
  min-block-size: min(44rem, 76dvh);
  padding-block: clamp(var(--tm-space-10), 12vw, var(--tm-space-18));
}

.public-home__hero-copy {
  display: grid;
  align-content: center;
  gap: var(--tm-space-5);
  max-inline-size: 49rem;
  animation: public-home-enter 280ms ease-out both;
}

.public-home__eyebrow {
  color: var(--tm-editorial-kicker);
  font-size: .75rem;
  font-weight: 800;
  letter-spacing: .12em;
  margin: 0;
  text-transform: uppercase;
}

.public-home__hero h1 {
  margin: 0;
  max-inline-size: 12ch;
  font-size: clamp(3.2rem, 9vw, 7.8rem);
  letter-spacing: -.065em;
  line-height: .9;
}

.public-home__hero-monogram {
  align-self: end;
  color: var(--tm-editorial-muted-surface);
  font-size: clamp(7rem, 22vw, 17rem);
  font-weight: 800;
  letter-spacing: -.11em;
  line-height: .72;
  margin: 0;
  user-select: none;
}

.public-home__hero-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--tm-space-4);
}

.public-home__contact-link,
.public-home__section-link {
  color: var(--tm-link);
  font-weight: 800;
  text-decoration: none;
}

.public-home__contact-link {
  display: inline-flex;
  align-items: center;
  min-block-size: var(--tm-control-min-size);
}

.public-home__story,
.public-home__collection {
  display: grid;
  gap: var(--tm-space-6);
  padding-block: clamp(var(--tm-space-9), 10vw, var(--tm-space-16));
}

.public-home__story {
  max-inline-size: 52rem;
}

.public-home__collection--work {
  border-block-start: 1px solid var(--tm-editorial-rule);
}

.public-home__section-heading {
  display: grid;
  gap: var(--tm-space-3);
  max-inline-size: 48rem;
}

.public-home__section-heading h2,
.public-home__publication-item h3 {
  margin: 0;
  letter-spacing: -.04em;
}

.public-home__section-heading h2 {
  font-size: clamp(2rem, 5vw, 4rem);
  line-height: 1;
}

.public-home__section-link {
  inline-size: fit-content;
  min-block-size: var(--tm-control-min-size);
}

.public-home__publication-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 17rem), 1fr));
  gap: var(--tm-space-4);
  list-style: none;
  margin: 0;
  padding: 0;
}

.public-home__publication-item {
  min-block-size: 13rem;
  padding: var(--tm-space-5);
  border: 1px solid var(--tm-editorial-card-border);
  border-radius: var(--tm-radius-card);
  background: var(--tm-editorial-card-surface);
}

.public-home__publication-item article {
  display: grid;
  gap: var(--tm-space-3);
}

.public-home__publication-item p {
  color: var(--tm-text-secondary);
  line-height: 1.65;
  margin: 0;
}

.public-home__publication-meta {
  font-size: .8125rem;
  font-weight: 800;
  letter-spacing: .06em;
  text-transform: uppercase;
}

@keyframes public-home-enter {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (prefers-reduced-motion: reduce) {
  .public-home__hero-copy { animation: none; }
}

@media (min-width: 900px) {
  .public-home__hero-content {
    grid-template-columns: minmax(0, 1fr) minmax(14rem, .55fr);
    align-items: end;
  }
}

.public-home__summary {
  color: var(--tm-text-secondary);
  font-size: clamp(1.125rem, 2vw, 1.375rem);
  line-height: 1.7;
  margin: 0;
  max-inline-size: 58ch;
}

</style>
