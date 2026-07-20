import { defineStore } from 'pinia'

import { createAdminAuthApi } from 'src/services/adminAuthApi'
import { normalizeApiError, createHttpClient } from 'src/services/httpClient'

const ADMIN_ROLES = new Set(['ADMIN', 'SUPER_ADMIN'])

function hasAdminRole(user) {
  return Array.isArray(user?.roles) && user.roles.some(
    (role) => ADMIN_ROLES.has(role)
  )
}

export const useAdminAuthStore = defineStore('admin-auth', {
  state: () => ({
    user: null,
    status: 'idle',
    error: null,
    restored: false
  }),

  getters: {
    isAuthenticated: (state) => state.user !== null,
    isAdmin: (state) => hasAdminRole(state.user)
  },

  actions: {
    clearSession() {
      this.user = null
    },

    async restoreSession(httpClient = createHttpClient()) {
      if (this.restored) {
        return this.user
      }

      this.status = 'loading'
      this.error = null

      try {
        const user = await createAdminAuthApi(httpClient).currentUser()

        this.user = hasAdminRole(user) ? user : null
        this.status = this.user === null ? 'forbidden' : 'ready'
        this.error = this.user === null
          ? { status: 403, code: 'FORBIDDEN', message: 'Admin access is required.' }
          : null

        return this.user
      }
      catch (error) {
        const normalized = normalizeApiError(error)

        this.clearSession()
        this.status = normalized.status === 401 ? 'anonymous' : 'error'
        this.error = normalized

        return null
      }
      finally {
        this.restored = true
      }
    },

    async login(credentials, httpClient = createHttpClient()) {
      this.status = 'loading'
      this.error = null

      try {
        const user = await createAdminAuthApi(httpClient).login(credentials)

        this.user = hasAdminRole(user) ? user : null
        this.restored = true
        this.status = this.user === null ? 'forbidden' : 'ready'
        this.error = this.user === null
          ? { status: 403, code: 'FORBIDDEN', message: 'Admin access is required.' }
          : null

        return this.user
      }
      catch (error) {
        this.clearSession()
        this.restored = true
        this.status = 'anonymous'
        this.error = normalizeApiError(error)

        return null
      }
    },

    async logout(httpClient = createHttpClient()) {
      this.status = 'loading'
      this.error = null

      try {
        await createAdminAuthApi(httpClient).logout()
      }
      catch (error) {
        this.error = normalizeApiError(error)
      }
      finally {
        this.clearSession()
        this.restored = true
        this.status = 'anonymous'
      }
    }
  }
})
