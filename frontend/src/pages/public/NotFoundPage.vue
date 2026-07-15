<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

const route = useRoute()
const { t } = useI18n()
const homePath = computed(() => `/${route.meta.locale ?? 'en'}`)
</script>

<template>
  <section class="tm-editorial-page tm-container" aria-labelledby="not-found-title">
    <div class="tm-editorial-page__content">
      <h1 id="not-found-title" class="tm-page-title">{{ t('public.notFound') }}</h1>
      <p class="tm-page-copy">{{ t('public.notFoundDescription') }}</p>
      <div class="tm-editorial-page__actions">
        <router-link class="tm-text-link tm-interactive" :to="homePath">
          {{ t('public.returnHome') }}
        </router-link>
      </div>
    </div>
  </section>
</template>
