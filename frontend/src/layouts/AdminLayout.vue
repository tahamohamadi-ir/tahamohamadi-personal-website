<script setup>
import { ref, watch } from 'vue'
import { useMeta, useQuasar } from 'quasar'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { useAdminAuthStore } from 'src/stores/adminAuth'

useMeta({
  meta: {
    robots: { name: 'robots', content: 'noindex, nofollow' }
  }
})

const router = useRouter()
const $q = useQuasar()
const auth = useAdminAuthStore()
const { t } = useI18n()
const drawerOpen = ref(false)
const navigation = [
  { labelKey: 'admin.navigationItems.dashboard', icon: 'dashboard', to: '/admin' },
  { labelKey: 'admin.navigationItems.siteSettings', icon: 'tune', to: '/admin/site-settings' },
  { labelKey: 'admin.navigationItems.navigation', icon: 'menu_open', to: '/admin/navigation' },
  { labelKey: 'admin.navigationItems.pages', icon: 'article', to: '/admin/pages' },
  { labelKey: 'admin.navigationItems.blogPosts', icon: 'article', to: '/admin/blog/posts' },
  { labelKey: 'admin.navigationItems.blogCategories', icon: 'category', to: '/admin/blog/categories' },
  { labelKey: 'admin.navigationItems.blogTags', icon: 'sell', to: '/admin/blog/tags' },
  { labelKey: 'admin.navigationItems.resume', icon: 'work_history', to: '/admin/resume' },
  { labelKey: 'admin.navigationItems.publications', icon: 'menu_book', to: '/admin/publications' },
  { labelKey: 'admin.navigationItems.portfolio', icon: 'folder_open', to: '/admin/portfolio' },
  { labelKey: 'admin.navigationItems.skills', icon: 'psychology', to: '/admin/skills' },
  { labelKey: 'admin.navigationItems.media', icon: 'perm_media', to: '/admin/media' },
  { labelKey: 'admin.navigationItems.socialLinks', icon: 'share', to: '/admin/social-links' },
  { labelKey: 'admin.navigationItems.featured', icon: 'star', to: '/admin/featured' },
  { labelKey: 'admin.navigationItems.contactMessages', icon: 'mail', to: '/admin/contact-messages' }
]

async function logout() {
  await auth.logout()
  await router.replace({ name: 'admin-login' })
}

function closeMobileNavigationAfterSelection() {
  if ($q.screen.lt.md) drawerOpen.value = false
}

watch(
  () => $q.screen.lt.md,
  (isCompact) => {
    if (!isCompact) drawerOpen.value = true
  },
  { immediate: true }
)
</script>

<template>
  <q-layout view="hHh lpR fFf">
    <q-header bordered class="bg-white text-dark admin-toolbar">
      <q-toolbar class="admin-toolbar__inner">
        <q-btn
          flat
          dense
          round
          :icon="drawerOpen ? 'menu_open' : 'menu'"
          :aria-label="t('admin.chrome.toggleNavigation')"
          @click="drawerOpen = !drawerOpen"
        />
        <q-toolbar-title>
          <span class="text-weight-bold">{{ t('admin.chrome.productName') }}</span>
          <span class="text-caption q-ml-sm">{{ t('admin.chrome.name') }}</span>
        </q-toolbar-title>
        <div class="gt-xs text-body2 q-mr-sm">
          {{ auth.user?.displayName }}
        </div>
        <q-btn flat no-caps :label="t('admin.chrome.logout')" :loading="auth.status === 'loading'" @click="logout" />
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="drawerOpen"
      show-if-above
      bordered
      :width="272"
      class="admin-drawer"
    >
      <q-list data-testid="admin-navigation" padding :aria-label="t('admin.chrome.navigationLabel')">
        <q-item-label header>{{ t('admin.chrome.content') }}</q-item-label>
        <q-item
          v-for="item in navigation"
          :key="item.to"
          v-ripple
          clickable
          :to="item.to"
          active-class="admin-navigation__item--active"
          @click="closeMobileNavigationAfterSelection"
        >
          <q-item-section avatar>
            <q-icon :name="item.icon" />
          </q-item-section>
          <q-item-section>{{ t(item.labelKey) }}</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <main class="admin-main" lang="en" dir="ltr">
        <router-view />
      </main>
    </q-page-container>
  </q-layout>
</template>

<style scoped>
.admin-toolbar {
  min-block-size: var(--tm-admin-toolbar-height);
}

.admin-toolbar__inner {
  min-block-size: var(--tm-admin-toolbar-height);
}

.admin-toolbar :deep(.q-btn) {
  min-inline-size: var(--tm-control-min-size);
  min-block-size: var(--tm-control-min-size);
}

.admin-drawer {
  background: var(--tm-admin-surface);
}

:deep(.admin-navigation__item--active) {
  background: var(--tm-admin-nav-active);
  color: var(--tm-action-primary);
  font-weight: 700;
}
</style>
