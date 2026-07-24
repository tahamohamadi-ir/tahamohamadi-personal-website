<script setup>
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref
} from 'vue'
import { useI18n } from 'vue-i18n'

import TmButton from 'src/components/shared/TmButton.vue'

import LanguageSwitch from './LanguageSwitch.vue'

const props = defineProps({
  locale: { type: String, required: true },
  direction: { type: String, required: true }
})

const { t } = useI18n()
const mobileNavigationOpen = ref(false)
const mobileTrigger = ref(null)

const navigationItems = [
  {
    key: 'work',
    labelKey: 'shell.navigation.work',
    path: '/portfolio'
  },
  {
    key: 'research',
    labelKey: 'shell.navigation.research',
    path: '/research'
  },
  {
    key: 'writing',
    labelKey: 'shell.navigation.writing',
    path: '/blog'
  },
  {
    key: 'about',
    labelKey: 'shell.navigation.about',
    path: '/about'
  },
  {
    key: 'resume',
    labelKey: 'shell.navigation.resume',
    path: '/resume'
  },
  {
    key: 'contact',
    labelKey: 'shell.navigation.contact',
    path: '/contact'
  }
]

const localizedNavigationItems = computed(() => navigationItems.map((item) => ({
  ...item,
  path: `/${props.locale}${item.path}`
})))

const drawerSide = computed(() => (
  props.direction === 'rtl' ? 'right' : 'left'
))

function openNavigation() {
  mobileNavigationOpen.value = true
}

function closeNavigation() {
  mobileNavigationOpen.value = false
}

function restoreMobileTriggerFocus() {
  void nextTick(() => mobileTrigger.value?.$el?.focus?.())
}

function handleEscape(event) {
  if (event.key !== 'Escape' || !mobileNavigationOpen.value) {
    return
  }

  closeNavigation()
}

onMounted(() => {
  document.addEventListener('keydown', handleEscape)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleEscape)
})
</script>

<template>
  <header class="site-header" :dir="direction">
    <div class="tm-container site-header__content">
      <router-link
        :to="`/${locale}`"
        class="site-header__brand tm-interactive"
      >
        <strong>{{ t('shell.siteName') }}</strong>
        <span>{{ t('shell.siteDescriptor') }}</span>
      </router-link>

      <nav
        class="site-header__desktop-nav"
        :aria-label="t('shell.primaryNavigation')"
      >
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

      <TmButton
        ref="mobileTrigger"
        class="site-header__mobile-trigger"
        variant="quiet"
        size="sm"
        :aria-label="t('shell.openNavigation')"
        @click="openNavigation"
      >
        {{ t('shell.menu') }}
      </TmButton>
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
      <nav
        class="site-header__mobile-nav"
        :aria-label="t('shell.primaryNavigation')"
      >
        <div class="site-header__mobile-nav-header">
          <div>
            <strong>{{ t('shell.siteName') }}</strong>
            <span>{{ t('shell.siteDescriptor') }}</span>
          </div>

          <TmButton
            class="site-header__close-trigger"
            variant="quiet"
            size="sm"
            :aria-label="t('shell.closeNavigation')"
            @click="closeNavigation"
          >
            {{ t('shell.close') }}
          </TmButton>
        </div>

        <div class="site-header__mobile-links">
          <router-link
            v-for="(item, index) in localizedNavigationItems"
            :key="`mobile-${item.key}`"
            :to="item.path"
            class="site-header__mobile-link tm-interactive"
            @click="closeNavigation"
          >
            <span aria-hidden="true">
              {{ String(index + 1).padStart(2, '0') }}
            </span>
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
  position: sticky;
  z-index: 20;
  inset-block-start: 0;
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
  text-decoration: none;
}

.site-header__brand {
  display: grid;
  flex: 0 0 auto;
  gap: 0.125rem;
  min-block-size: var(--tm-control-min-size);
  align-content: center;
}

.site-header__brand strong {
  font-size: 1rem;
  letter-spacing: -0.015em;
  line-height: 1.2;
}

.site-header__brand span {
  display: none;
  color: var(--tm-text-secondary);
  font-size: 0.6875rem;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.site-header__desktop-nav,
.site-header__desktop-language {
  display: none;
}

.site-header__drawer {
  background: var(--tm-surface);
}

.site-header__mobile-nav {
  display: grid;
  min-block-size: 100%;
  grid-template-rows: auto 1fr auto;
  padding: var(--tm-space-5);
}

.site-header__mobile-nav-header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: var(--tm-space-4);
  padding-block-end: var(--tm-space-5);
  border-block-end: 1px solid var(--tm-shell-boundary);
}

.site-header__mobile-nav-header > div {
  display: grid;
  gap: var(--tm-space-1);
}

.site-header__mobile-nav-header span {
  color: var(--tm-text-secondary);
  font-size: 0.75rem;
}

.site-header__mobile-links {
  display: grid;
  align-content: start;
  padding-block: var(--tm-space-5);
}

.site-header__mobile-link {
  display: grid;
  grid-template-columns: 2.5rem minmax(0, 1fr);
  align-items: center;
  min-block-size: 4rem;
  padding-inline: var(--tm-space-2);
  border-inline-start: 3px solid transparent;
  border-block-end: 1px solid var(--tm-shell-boundary);
  font-size: 1.125rem;
  font-weight: 700;
}

.site-header__mobile-link > span {
  color: var(--tm-text-secondary);
  font-size: 0.6875rem;
}

.site-header__mobile-language {
  padding-block-start: var(--tm-space-5);
}

.site-header__brand:hover,
.site-header__nav-link:hover,
.site-header__mobile-link:hover {
  color: var(--tm-interactive-hover);
}

.site-header__brand:active,
.site-header__nav-link:active,
.site-header__mobile-link:active {
  color: var(--tm-interactive-active);
}

.site-header__nav-link.router-link-exact-active {
  color: var(--tm-action-primary);
  font-weight: 800;
  text-decoration: underline;
  text-decoration-color: var(--tm-navigation-current-indicator);
  text-decoration-thickness: 2px;
  text-underline-offset: var(--tm-space-2);
}

.site-header__mobile-link.router-link-exact-active {
  border-inline-start-color: var(--tm-navigation-current-indicator);
  background: var(--tm-interactive-surface-hover);
  color: var(--tm-action-primary);
}

@media (min-width: 720px) {
  .site-header__brand span {
    display: block;
  }
}

@media (min-width: 1180px) {
  .site-header__desktop-nav {
    display: flex;
    align-items: center;
    flex: 1 1 auto;
    justify-content: center;
    gap: clamp(var(--tm-space-3), 2vw, var(--tm-space-6));
    min-inline-size: 0;
    white-space: nowrap;
  }

  .site-header__nav-link {
    display: inline-flex;
    align-items: center;
    min-block-size: var(--tm-control-min-size);
    font-size: 0.875rem;
    font-weight: 700;
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
