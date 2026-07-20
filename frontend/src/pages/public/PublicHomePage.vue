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

const api = inject(PUBLIC_API_KEY, null)
const route = useRoute()
const { t } = useI18n()
const locale = computed(() => route.meta.locale)
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
usePublicSeoMeta({ data, state })
const showsContent = computed(() => (
  data.value !== null && state.value !== 'empty'
))
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

onMounted(() => {
  if (!hasInitialState) {
    void load()
  }
})
</script>

<template>
  <section
    class="tm-editorial-page tm-editorial-page--introduction tm-container"
    aria-labelledby="home-title"
  >
    <header class="tm-editorial-page__content">
      <h1 id="home-title" class="tm-page-title">{{ t('shell.siteName') }}</h1>
    </header>

    <PageState
      v-if="state && state !== 'translation-unavailable'"
      :state="state"
      @retry="retry"
    />

    <TranslationUnavailable
      v-else-if="state === 'translation-unavailable'"
      :alternate-path="alternatePath"
      :target-locale="locale"
    />

    <div
      v-if="showsContent"
      class="tm-editorial-page__content"
    >
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

      <section v-if="featuredItems.length" class="q-mt-xl" :aria-label="t('public.home.featured')">
        <h2 class="text-h5">{{ t('public.home.featured') }}</h2>
        <q-list bordered separator>
          <template v-for="item in featuredItems" :key="`${item.targetType}:${item.slug}`">
            <q-item v-if="featuredPath(item)" :to="featuredPath(item)">
              <q-item-section><q-item-label>{{ item.title }}</q-item-label></q-item-section>
            </q-item>
          </template>
        </q-list>
      </section>

      <nav v-if="socialLinks.length" class="q-mt-xl" :aria-label="t('public.home.socialLinks')">
        <h2 class="text-h6">{{ t('public.home.socialLinks') }}</h2>
        <div class="row q-gutter-sm">
          <q-btn v-for="link in socialLinks" :key="`${link.platformCode}:${link.url}`" outline :href="link.url" target="_blank" rel="noopener noreferrer" :label="link.platformCode" />
        </div>
      </nav>
    </div>
  </section>
</template>
