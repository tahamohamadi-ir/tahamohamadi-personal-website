<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { usePublicSeoMeta } from 'src/composables/usePublicSeoMeta'

const route = useRoute()
const { t } = useI18n()
const homePath = computed(() => `/${route.meta.locale ?? 'en'}`)
usePublicSeoMeta({
  data: computed(() => null),
  state: computed(() => 'translation-unavailable')
})
</script>

<template>
  <section class="tm-editorial-page tm-container" aria-labelledby="translation-unavailable-title">
    <div class="tm-editorial-page__content">
      <h1 id="translation-unavailable-title" class="tm-page-title">
        {{ t('public.translationUnavailable') }}
      </h1>
      <p class="tm-page-copy">{{ t('public.translationUnavailableDescription') }}</p>
      <div class="tm-editorial-page__actions">
        <router-link class="tm-text-link tm-interactive" :to="homePath">
          {{ t('public.returnHome') }}
        </router-link>
      </div>
    </div>
  </section>
</template>
