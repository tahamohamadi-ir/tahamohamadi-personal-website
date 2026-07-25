<script setup>
import { computed, inject, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import MarkdownContent from 'src/components/content/MarkdownContent.vue'
import PageBlockRenderer from 'src/components/public/PageBlockRenderer.vue'
import PageState from 'src/components/public/PageState.vue'
import TranslationUnavailable from 'src/components/public/TranslationUnavailable.vue'
import { useComposedPageData } from 'src/composables/useComposedPageData'
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
const ssrKey = computed(() => `public:${locale.value}:page:research`)
const slug = computed(() => 'research')

const {
  page,
  collectionItems,
  skills,
  socialLinks,
  state,
  error,
  load,
  refresh,
  hasInitialState
} = useComposedPageData({
  api,
  locale,
  slug,
  initialData: props.initialData,
  ssrKey: () => ssrKey.value
})

const showsContent = computed(() => (
  page.value !== null && state.value !== 'empty'
))
usePublicSeoMeta({ data: page, state })
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
  <section class="tm-editorial-page tm-editorial-page--introduction tm-container">
    <header class="tm-editorial-page__content">
      <h1 class="tm-page-title">{{ page?.title || t('shell.navigation.research') }}</h1>
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
      v-if="showsContent && !page?.blocks?.length"
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
    </div>

    <PageBlockRenderer
      v-else-if="showsContent && page?.blocks?.length"
      :blocks="page.blocks"
      :collection-items="collectionItems"
      :locale="locale"
      :skills="skills"
      :social-links="socialLinks"
    />
  </section>
</template>
