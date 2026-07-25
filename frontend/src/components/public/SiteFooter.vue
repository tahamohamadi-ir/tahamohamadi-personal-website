<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  locale: { type: String, required: true },
  direction: { type: String, required: true },
  site: { type: Object, default: null },
  navigation: { type: Array, default: null }
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

const localizedNavigationItems = computed(() => Array.isArray(props.navigation)
  ? props.navigation.map((item) => ({ key: item.key, label: item.label, path: item.externalTarget ? item.targetPath : item.targetPath.replace('{lang}', props.locale), externalTarget: item.externalTarget === true }))
  : navigationItems.map((item) => ({ ...item, path: `/${props.locale}${item.path}`, externalTarget: false })))
const brandName = computed(() => props.site?.brandName || t('shell.siteName'))
const footerStatement = computed(() => props.site?.footerStatement || t('shell.footer.statement'))
const footerAvailability = computed(() => props.site?.footerAvailability || t('shell.footer.availability'))
const footerRights = computed(() => props.site?.footerRights || t('shell.footer.rights'))
</script>

<template>
  <footer class="site-footer" :dir="direction">
    <div class="tm-container site-footer__content">
      <div class="site-footer__identity">
        <strong>{{ brandName }}</strong>
        <p>{{ footerStatement }}</p>
      </div>

      <nav
        class="site-footer__navigation"
        :aria-label="t('shell.footer.navigationLabel')"
      >
        <template v-for="item in localizedNavigationItems" :key="item.key">
          <a v-if="item.externalTarget" :href="item.path" class="site-footer__link tm-interactive" rel="noopener noreferrer" target="_blank">{{ item.label ?? t(`shell.navigation.${item.key}`) }}</a>
          <router-link v-else :to="item.path" class="site-footer__link tm-interactive">{{ item.label ?? t(`shell.navigation.${item.key}`) }}</router-link>
        </template>
      </nav>

      <div class="site-footer__meta">
        <p>{{ footerAvailability }}</p>
        <p>{{ footerRights }}</p>
      </div>
    </div>
  </footer>
</template>

<style scoped lang="scss">
.site-footer {
  border-block-start: 1px solid var(--tm-shell-boundary);
  background: var(--tm-footer-surface);
  color: var(--tm-footer-text);
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
  color: var(--tm-footer-text);
  line-height: 1.65;
  opacity: 0.78;
}

.site-footer__navigation {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border-block-start: 1px solid var(--tm-footer-rule);
}

.site-footer__link {
  display: inline-flex;
  align-items: center;
  min-block-size: var(--tm-control-min-size);
  padding-block: var(--tm-space-3);
  border-block-end: 1px solid var(--tm-footer-rule);
  color: var(--tm-footer-text);
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
  border-block-start: 1px solid var(--tm-footer-rule);
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
