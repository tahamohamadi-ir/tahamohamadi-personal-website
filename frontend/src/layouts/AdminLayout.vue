<script setup>
import { ref } from 'vue'
import { useMeta } from 'quasar'
import { useRouter } from 'vue-router'

import { useAdminAuthStore } from 'src/stores/adminAuth'

useMeta({
  meta: {
    robots: { name: 'robots', content: 'noindex, nofollow' }
  }
})

const router = useRouter()
const auth = useAdminAuthStore()
const drawerOpen = ref(false)
const navigation = [
  { label: 'Dashboard', icon: 'dashboard', to: '/admin' },
  { label: 'Pages', icon: 'article', to: '/admin/pages' },
  { label: 'Resume', icon: 'work_history', to: '/admin/resume' },
  { label: 'Publications', icon: 'menu_book', to: '/admin/publications' },
  { label: 'Portfolio', icon: 'folder_open', to: '/admin/portfolio' },
  { label: 'Skills', icon: 'psychology', to: '/admin/skills' },
  { label: 'Media', icon: 'perm_media', to: '/admin/media' },
  { label: 'Social links', icon: 'share', to: '/admin/social-links' },
  { label: 'Featured content', icon: 'star', to: '/admin/featured' }
]

async function logout() {
  await auth.logout()
  await router.replace({ name: 'admin-login' })
}
</script>

<template>
  <q-layout view="hHh lpR fFf">
    <q-header bordered class="bg-white text-dark">
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Open administration navigation"
          class="lt-md"
          @click="drawerOpen = !drawerOpen"
        />
        <q-toolbar-title>
          <span class="text-weight-bold">Administration</span>
        </q-toolbar-title>
        <div class="gt-xs text-body2 q-mr-sm">
          {{ auth.user?.displayName }}
        </div>
        <q-btn flat label="Log out" :loading="auth.status === 'loading'" @click="logout" />
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="drawerOpen"
      show-if-above
      bordered
      :width="260"
    >
      <q-list padding aria-label="Administration navigation">
        <q-item-label header>Content</q-item-label>
        <q-item
          v-for="item in navigation"
          :key="item.to"
          v-ripple
          clickable
          :to="item.to"
          active-class="bg-blue-1 text-primary"
          @click="drawerOpen = false"
        >
          <q-item-section avatar>
            <q-icon :name="item.icon" />
          </q-item-section>
          <q-item-section>{{ item.label }}</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <main lang="en" dir="ltr">
        <router-view />
      </main>
    </q-page-container>
  </q-layout>
</template>
