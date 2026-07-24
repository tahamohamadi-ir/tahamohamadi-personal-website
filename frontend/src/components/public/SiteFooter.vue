<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  locale: { type: String, required: true },
  direction: { type: String, required: true }
})

const { t } = useI18n()

const navigationItems = [
  { key: 'work', path: '/portfolio' },
  { key: 'research', path: '/research' },
  { key: 'writing', path: '/blog' },
  { key: 'about', path: '/about' },
  { key: 'resume', path: '/resume' },
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
      <div class="site-footer__identity">
        <strong>{{ t('shell.siteName') }}</strong>
        <p>{{ t('shell.footer.statement') }}</p>
      </div>

      <nav
        class="site-footer__navigation"
        :aria-label="t('shell.footer.navigationLabel')"
      >
        <router-link
          v-for="item in localizedNavigationItems"
          :key="item.key"
          :to="item.path"
          class="site-footer__link tm-interactive"
        >
          {{ t(`shell.navigation.${item.key}`) }}
        </router-link>
      </nav>

      <div class="site-footer__meta">
        <p>{{ t('shell.footer.availability') }}</p>
        <p>{{ t('shell.footer.rights') }}</p>
      </div>
    </div>
  </footer>
</template>

<style scoped lang="scss">
.site-footer {
  border-block-start: 1px solid var(--tm-shell-boundary);
  background: var(--tm-text-primary);
  color: var(--tm-canvas);
}

.site-footer__content {
  display: grid;
  gap: var(--tm-space-7);
  padding-block: clamp(var(--tm-space-8), 7vw, var(--tm-space-12));
}

.site-footer__identity {
  max-inline-size: 34rem;
}

.site-footer__identity strong {
  font-size: 1.25rem;
}

.site-footer__identity p,
.site-footer__meta p {
  margin: var(--tm-space-3) 0 0;
  color: var(--tm-canvas);
  line-height: 1.65;
  opacity: 0.78;
}

.site-footer__navigation {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border-block-start: 1px solid var(--tm-border-subtle);
}

.site-footer__link {
  display: inline-flex;
  align-items: center;
  min-block-size: var(--tm-control-min-size);
  padding-block: var(--tm-space-3);
  border-block-end: 1px solid var(--tm-border-subtle);
  color: var(--tm-canvas);
  font-weight: 700;
  text-decoration: none;
}

.site-footer__link:hover {
  color: var(--tm-interactive-hover);
}

.site-footer__link:active {
  color: var(--tm-interactive-active);
}

.site-footer__meta {
  display: grid;
  gap: var(--tm-space-2);
  padding-block-start: var(--tm-space-4);
  border-block-start: 1px solid var(--tm-border-subtle);
}

.site-footer__meta p {
  margin: 0;
  font-size: 0.875rem;
}

@media (min-width: 800px) {
  .site-footer__content {
    grid-template-columns: minmax(0, 1fr) minmax(24rem, 0.9fr);
  }

  .site-footer__navigation {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .site-footer__meta {
    grid-column: 1 / -1;
    grid-template-columns: 1fr 1fr;
  }

  .site-footer__meta p:last-child {
    text-align: end;
  }
}
</style>
