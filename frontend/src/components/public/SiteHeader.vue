<script setup>
import { computed, nextTick, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import LanguageSwitch from './LanguageSwitch.vue'

const props = defineProps({
  locale: { type: String, required: true },
  direction: { type: String, required: true }
})

const { t } = useI18n()
const mobileNavigationOpen = ref(false)
const mobileTrigger = ref(null)

const navigationItems = [
  { key: 'home', labelKey: 'shell.navigation.home', path: '' },
  { key: 'about', labelKey: 'shell.navigation.about', path: '/about' },
  { key: 'research', labelKey: 'shell.navigation.research', path: '/research' },
  { key: 'skills', labelKey: 'shell.navigation.skills', path: '/skills' },
  { key: 'resume', labelKey: 'shell.navigation.resume', path: '/resume' },
  { key: 'blog', labelKey: 'shell.navigation.blog', path: '/blog' },
  { key: 'portfolio', labelKey: 'shell.navigation.portfolio', path: '/portfolio' },
  { key: 'publications', labelKey: 'shell.navigation.publications', path: '/publications' },
  { key: 'contact', labelKey: 'shell.navigation.contact', path: '/contact' }
]

const localizedNavigationItems = computed(() => navigationItems.map((item) => ({
  ...item,
  path: `/${props.locale}${item.path}`
})))

const drawerSide = computed(() => (props.direction === 'rtl' ? 'right' : 'left'))

function openNavigation () {
  mobileNavigationOpen.value = true
}

function closeNavigation () {
  mobileNavigationOpen.value = false
}

function restoreMobileTriggerFocus () {
  void nextTick(() => mobileTrigger.value?.focus?.())
}
</script>

<template>
  <header class="site-header" :dir="direction">
    <div class="tm-container site-header__content">
      <router-link :to="`/${locale}`" class="site-header__brand tm-interactive">
        {{ t('shell.siteName') }}
      </router-link>

      <nav class="site-header__desktop-nav" :aria-label="t('shell.primaryNavigation')">
        <router-link
          v-for="item in localizedNavigationItems"
          :key="item.key"
          :to="item.path"
          class="site-header__nav-link tm-interactive"
        >
          {{ t(item.labelKey) }}
        </router-link>
      </nav>

      <div class="site-header__desktop-language">
        <LanguageSwitch :locale="locale" />
      </div>

      <q-btn
        ref="mobileTrigger"
        class="site-header__mobile-trigger"
        flat
        no-caps
        :label="t('shell.openNavigation')"
        :aria-label="t('shell.openNavigation')"
        @click="openNavigation"
      />
    </div>

    <q-drawer
      v-model="mobileNavigationOpen"
      class="site-header__drawer"
      :side="drawerSide"
      overlay
      behavior="mobile"
      bordered
      @hide="restoreMobileTriggerFocus"
    >
      <nav class="site-header__mobile-nav" :aria-label="t('shell.primaryNavigation')">
        <div class="site-header__mobile-nav-header">
          <strong>{{ t('shell.siteName') }}</strong>
          <q-btn
            class="site-header__close-trigger"
            flat
            no-caps
            :label="t('shell.closeNavigation')"
            :aria-label="t('shell.closeNavigation')"
            @click="closeNavigation"
          />
        </div>

        <div class="site-header__mobile-links">
          <router-link
            v-for="item in localizedNavigationItems"
            :key="`mobile-${item.key}`"
            :to="item.path"
            class="site-header__mobile-link tm-interactive"
            @click="closeNavigation"
          >
            {{ t(item.labelKey) }}
          </router-link>
        </div>

        <div class="site-header__mobile-language">
          <LanguageSwitch :locale="locale" />
        </div>
      </nav>
    </q-drawer>
  </header>
</template>

<style scoped lang="scss">
.site-header {
  border-block-end: 1px solid var(--tm-shell-boundary);
  background: var(--tm-surface);
}

.site-header__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-block-size: var(--tm-header-height);
  gap: var(--tm-space-4);
}

.site-header__brand,
.site-header__nav-link,
.site-header__mobile-link {
  color: var(--tm-text-primary);
  font-weight: 600;
  text-decoration: none;
}

.site-header__brand {
  display: inline-flex;
  align-items: center;
  flex: 0 0 auto;
  min-block-size: var(--tm-control-min-size);
  font-size: 1.0625rem;
  font-weight: 700;
  letter-spacing: -0.01em;
  white-space: nowrap;
}

.site-header__desktop-nav,
.site-header__desktop-language {
  display: none;
}

.site-header__mobile-trigger,
.site-header__close-trigger {
  min-inline-size: var(--tm-control-min-size);
  min-block-size: var(--tm-control-min-size);
  border: 1px solid var(--tm-shell-boundary);
  border-radius: var(--tm-radius-control);
  color: var(--tm-text-primary);
}

.site-header__drawer {
  background: var(--tm-surface);
}

.site-header__mobile-nav {
  display: grid;
  min-block-size: 100%;
  grid-template-rows: auto 1fr auto;
  padding: var(--tm-space-4);
}

.site-header__mobile-nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--tm-space-4);
  padding-block-end: var(--tm-space-4);
  border-block-end: 1px solid var(--tm-shell-boundary);
}

.site-header__mobile-links {
  display: grid;
  align-content: start;
  padding-block: var(--tm-space-4);
}

.site-header__mobile-link {
  display: flex;
  align-items: center;
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-2);
  border-inline-start: 3px solid transparent;
  border-block-end: 1px solid var(--tm-shell-boundary);
}

.site-header__mobile-language {
  padding-block-start: var(--tm-space-4);
}

.site-header__brand:hover,
.site-header__nav-link:hover,
.site-header__mobile-link:hover {
  color: var(--tm-interactive-hover);
}

.site-header__mobile-trigger:hover,
.site-header__close-trigger:hover {
  border-color: var(--tm-interactive-hover);
  background: var(--tm-interactive-surface-hover);
  color: var(--tm-interactive-hover);
}

.site-header__mobile-trigger:active,
.site-header__close-trigger:active {
  background: var(--tm-interactive-surface-hover);
  color: var(--tm-interactive-active);
}

.site-header__nav-link.router-link-exact-active {
  background: var(--tm-interactive-surface-hover);
  color: var(--tm-action-primary);
  font-weight: 700;
  text-decoration: underline;
  text-decoration-color: var(--tm-navigation-current-indicator);
  text-decoration-thickness: 2px;
  text-underline-offset: var(--tm-space-2);
}

.site-header__mobile-link.router-link-exact-active {
  border-inline-start-color: var(--tm-navigation-current-indicator);
  background: var(--tm-interactive-surface-hover);
  color: var(--tm-action-primary);
  font-weight: 700;
}

@media (min-width: 1024px) {
  .site-header__desktop-nav {
    display: flex;
    align-items: center;
    flex: 1 1 auto;
    justify-content: center;
    gap: var(--tm-space-4);
    min-inline-size: 0;
    white-space: nowrap;
  }

  .site-header__nav-link {
    display: inline-flex;
    align-items: center;
    min-block-size: var(--tm-control-min-size);
    font-size: 0.9375rem;
  }

  .site-header__desktop-language {
    display: block;
    flex: 0 0 auto;
  }

  .site-header__mobile-trigger {
    display: none;
  }
}
</style>
