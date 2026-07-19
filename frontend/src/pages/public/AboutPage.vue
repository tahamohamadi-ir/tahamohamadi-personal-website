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
const ssrKey = computed(() => `public:${locale.value}:page:about`)

function isEmptyPage(value) {
  return ![
    value?.summary,
    value?.bodyMarkdown
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
  load: (currentApi) => currentApi.getPage(locale.value, 'about'),
  isEmpty: isEmptyPage,
  initialData: props.initialData,
  ssrKey: () => ssrKey.value
})

const showsContent = computed(() => (
  data.value !== null && state.value !== 'empty'
))
usePublicSeoMeta({ data, state })
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
      <h1 class="tm-page-title">{{ t('shell.navigation.about') }}</h1>
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
        v-if="data?.summary"
        class="tm-page-copy"
      >
        {{ data.summary }}
      </p>
      <MarkdownContent
        v-if="data?.bodyMarkdown"
        :markdown="data.bodyMarkdown"
      >
        <template #error>
          <p class="tm-page-copy" role="alert">
            {{ t('public.richContent.renderingFailure') }}
          </p>
        </template>
      </MarkdownContent>
    </div>
  </section>
</template>
