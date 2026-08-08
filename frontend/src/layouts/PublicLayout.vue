<script setup>
import { computed, inject, nextTick, onMounted, provide, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useMeta } from 'quasar'
import SiteFooter from 'src/components/public/SiteFooter.vue'
import SiteHeader from 'src/components/public/SiteHeader.vue'
import SkipLink from 'src/components/public/SkipLink.vue'
import { useAsyncPage } from 'src/composables/useAsyncPage'
import { PUBLIC_API_KEY, PUBLIC_SITE_IDENTITY_KEY } from 'src/services/apiContext'

const route = useRoute()
const { locale } = useI18n()
const api = inject(PUBLIC_API_KEY)

const language = computed(() => route.meta.locale || 'en')
const direction = computed(() => route.meta.direction || 'ltr')
const chromeKey = computed(() => `public:${language.value}:site-chrome`)
const { data: siteChrome, load: loadSiteChrome, hasInitialState } = useAsyncPage({
  api,
  load: (currentApi) => currentApi.getSiteChrome(language.value),
  isEmpty: () => false,
  ssrKey: () => chromeKey.value
})
const siteIdentity = computed(() => siteChrome.value?.identity ?? null)
provide(PUBLIC_SITE_IDENTITY_KEY, siteIdentity)

useMeta(() => {
  const mediaId = siteChrome.value?.identity?.ogMediaId
  if (!mediaId) return {}
  const image = `/api/v1/public/media/${mediaId}`
  return {
    meta: {
      ogImage: { property: 'og:image', content: image },
      twitterImage: { name: 'twitter:image', content: image }
    }
  }
})

watch(
  language,
  (value) => {
    locale.value = value
  },
  { immediate: true }
)

onMounted(() => {
  if (!hasInitialState) void loadSiteChrome()
})

watch(
  direction,
  (value) => {
    if (typeof document !== 'undefined') {
      document.documentElement.dir = value
      document.documentElement.lang = language.value
    }
  },
  { immediate: true }
)

watch(
  () => route.fullPath,
  () => {
    if (typeof document === 'undefined') {
      return
    }

    void nextTick(() => {
      document.getElementById('main-content')?.focus()
    })
  }
)
</script>

<template>
  <q-layout
    class="public-shell"
    :class="{ 'public-shell--standard-density': siteChrome?.identity?.layoutDensity === 'STANDARD' }"
    :data-theme-preset="siteChrome?.identity?.themePreset ?? 'EDITORIAL_NAVY'"
    view="hHh lpR fFf"
  >
    <SkipLink />
    <SiteHeader
      :locale="language"
      :direction="direction"
      :site="siteIdentity"
      :navigation="siteChrome && (siteChrome.identity || siteChrome.navigation?.length) ? siteChrome.navigation : null"
    />
    <q-page-container>
      <main
        id="main-content"
        class="public-main"
        :dir="direction"
        :lang="language"
        tabindex="-1"
      >
        <router-view />
      </main>
    </q-page-container>
    <SiteFooter
      :locale="language"
      :direction="direction"
      :site="siteIdentity"
      :navigation="siteChrome && (siteChrome.identity || siteChrome.navigation?.length) ? siteChrome.navigation : null"
    />
  </q-layout>
</template>
