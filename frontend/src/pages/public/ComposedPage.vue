<script setup>
import { computed, inject, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

import PageBlockRenderer from 'src/components/public/PageBlockRenderer.vue'
import PageState from 'src/components/public/PageState.vue'
import TranslationUnavailable from 'src/components/public/TranslationUnavailable.vue'
import { useComposedPageData } from 'src/composables/useComposedPageData'
import { usePublicSeoMeta } from 'src/composables/usePublicSeoMeta'
import { PUBLIC_API_KEY } from 'src/services/apiContext'

const props = defineProps({ initialData: { type: Object, default: undefined } })
const api = inject(PUBLIC_API_KEY)
const route = useRoute()
const locale = computed(() => route.meta.locale)
const slug = computed(() => route.params.slug)
const ssrKey = computed(() => `public:${locale.value}:page:${slug.value}`)

const { page, collectionItems, skills, socialLinks, state, error, load, refresh, hasInitialState } = useComposedPageData({
  api,
  locale,
  slug,
  initialData: props.initialData,
  ssrKey: () => ssrKey.value
})

usePublicSeoMeta({ data: page, state })
const alternatePath = computed(() => error.value?.alternatePaths?.[0] ?? null)
function retry() { return state.value === 'stale' ? refresh() : load() }

onMounted(() => { if (!hasInitialState) void load() })
watch(slug, () => { if (hasInitialState) void load() })
</script>

<template>
  <section class="tm-editorial-page tm-container">
    <PageState v-if="state && state !== 'translation-unavailable'" :state="state" @retry="retry" />
    <TranslationUnavailable v-else-if="state === 'translation-unavailable'" :alternate-path="alternatePath" :target-locale="locale" />
    <template v-else-if="page">
      <header class="tm-editorial-page__content">
        <h1 class="tm-page-title">{{ page.title }}</h1>
        <p v-if="page.summary && !page.blocks?.length" class="tm-page-copy">{{ page.summary }}</p>
      </header>
      <PageBlockRenderer v-if="page.blocks?.length" :blocks="page.blocks" :collection-items="collectionItems" :locale="locale" :skills="skills" :social-links="socialLinks" />
    </template>
  </section>
</template>
