<script setup>
import { computed, reactive } from 'vue'
import { useMeta } from 'quasar'
import { useRoute, useRouter } from 'vue-router'

import { useAdminAuthStore } from 'src/stores/adminAuth'

useMeta({
  meta: {
    robots: { name: 'robots', content: 'noindex, nofollow' }
  }
})

const router = useRouter()
const route = useRoute()
const auth = useAdminAuthStore()
const credentials = reactive({ email: '', password: '' })
const submitting = computed(() => auth.status === 'loading')

function safeRedirect() {
  const redirect = route.query.redirect

  return typeof redirect === 'string' && redirect.startsWith('/admin/')
    ? redirect
    : '/admin'
}

async function submit() {
  const user = await auth.login({
    email: credentials.email.trim(),
    password: credentials.password
  })

  if (user) {
    await router.replace(safeRedirect())
  }
}
</script>

<template>
  <q-layout view="hHh lpR fFf">
    <q-page-container>
      <q-page class="row items-center justify-center q-pa-md">
        <q-card class="admin-login-card full-width" flat bordered>
          <q-card-section>
            <div class="text-overline text-primary">TahaMohamadi.ir</div>
            <h1 class="text-h5 q-my-sm">Admin sign in</h1>
            <p class="text-body2 text-grey-8 q-mb-none">
              Use your administrator account to manage presentation content.
            </p>
          </q-card-section>

          <q-card-section>
            <q-banner
              v-if="auth.error"
              class="bg-red-1 text-negative q-mb-md"
              rounded
              role="alert"
            >
              {{ auth.error.message }}
            </q-banner>

            <q-form class="q-gutter-md" @submit="submit">
              <q-input
                v-model="credentials.email"
                type="email"
                label="Email address"
                autocomplete="username"
                outlined
                :disable="submitting"
                :rules="[(value) => Boolean(value?.trim()) || 'Enter your email address.']"
              />
              <q-input
                v-model="credentials.password"
                type="password"
                label="Password"
                autocomplete="current-password"
                outlined
                :disable="submitting"
                :rules="[(value) => Boolean(value) || 'Enter your password.']"
              />
              <q-btn
                type="submit"
                color="primary"
                label="Sign in"
                class="full-width"
                :loading="submitting"
                :disable="submitting"
              />
            </q-form>
          </q-card-section>
        </q-card>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<style scoped>
.admin-login-card {
  max-width: 30rem;
}
</style>
