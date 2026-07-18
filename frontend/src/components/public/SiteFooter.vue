<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  locale: { type: String, required: true },
  direction: { type: String, required: true }
})

const { t } = useI18n()
const navigationItems = [
  { key: 'about', path: '/about' }, { key: 'research', path: '/research' },
  { key: 'resume', path: '/resume' }, { key: 'blog', path: '/blog' },
  { key: 'portfolio', path: '/portfolio' }, { key: 'publications', path: '/publications' },
  { key: 'contact', path: '/contact' }
]

const localizedNavigationItems = computed(() => navigationItems.map((item) => ({
  ...item,
  path: `/${props.locale}${item.path}`
})))
</script>

<template>
  <footer class="site-footer" :dir="direction">
    <div class="tm-container site-footer__content">
      <nav class="site-footer__navigation" :aria-label="t('shell.footer.navigationLabel')">
        <router-link
          v-for="item in localizedNavigationItems"
          :key="item.key"
          :to="item.path"
          class="site-footer__link tm-interactive"
        >
          {{ t(`shell.navigation.${item.key}`) }}
        </router-link>
      </nav>
      <p class="site-footer__rights">{{ t('shell.footer.rights') }}</p>
    </div>
  </footer>
</template>

<style scoped lang="scss">
.site-footer {
  border-block-start: 1px solid var(--tm-shell-boundary);
  background: var(--tm-surface);
}

.site-footer__content {
  display: grid;
  gap: var(--tm-space-4);
  padding-block: var(--tm-space-6);
}

.site-footer__navigation {
  display: flex;
  flex-wrap: wrap;
  gap: var(--tm-space-1) var(--tm-space-4);
}

.site-footer__link {
  display: inline-flex;
  align-items: center;
  min-block-size: var(--tm-control-min-size);
  color: var(--tm-text-secondary);
  text-decoration: none;
}

.site-footer__link:hover {
  color: var(--tm-interactive-hover);
}

.site-footer__link:active {
  color: var(--tm-interactive-active);
}

.site-footer__rights {
  margin: 0;
  color: var(--tm-text-secondary);
  font-size: 0.9375rem;
}
</style>
