export function createAdminNavigationGuard(
  getAuthStore,
  isServer,
  getServerHttpClient = null
) {
  return async (to) => {
    const requiresAdmin = to.matched.some(
      (record) => record.meta.requiresAdmin
    )
    const requiresGuest = to.matched.some(
      (record) => record.meta.requiresGuest
    )

    if (!requiresAdmin && !requiresGuest) {
      return true
    }

    if (isServer && typeof getServerHttpClient !== 'function') {
      return true
    }

    const auth = getAuthStore()
    await auth.restoreSession(
      isServer ? getServerHttpClient() : undefined
    )

    if (requiresAdmin && !auth.isAdmin) {
      return {
        name: 'admin-login',
        query: { redirect: to.fullPath }
      }
    }

    if (requiresGuest && auth.isAdmin) {
      return { name: 'admin-home' }
    }

    return true
  }
}
