export function createAdminNavigationGuard(getAuthStore, isServer) {
  return async (to) => {
    if (isServer) {
      return true
    }

    const requiresAdmin = to.matched.some(
      (record) => record.meta.requiresAdmin
    )
    const requiresGuest = to.matched.some(
      (record) => record.meta.requiresGuest
    )

    if (!requiresAdmin && !requiresGuest) {
      return true
    }

    const auth = getAuthStore()
    await auth.restoreSession()

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
