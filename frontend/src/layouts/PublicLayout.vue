<script setup>
import { computed, nextTick, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import SiteFooter from 'src/components/public/SiteFooter.vue'
import SiteHeader from 'src/components/public/SiteHeader.vue'
import SkipLink from 'src/components/public/SkipLink.vue'

const route = useRoute()
const { locale } = useI18n()

const language = computed(() => route.meta.locale || 'en')
const direction = computed(() => route.meta.direction || 'ltr')

watch(
  language,
  (value) => {
    locale.value = value
  },
  { immediate: true }
)

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
    view="hHh lpR fFf"
  >
    <SkipLink />
    <SiteHeader
      :locale="language"
      :direction="direction"
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
    />
  </q-layout>
</template>
