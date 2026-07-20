import { describe, expect, it } from 'vitest'

import { createAdminNavigationGuard } from 'src/router/adminNavigationGuard'

function route(meta, fullPath = '/admin') {
  return { matched: [{ meta }], fullPath }
}

describe('admin navigation guard', () => {
  it('redirects an anonymous visitor to login and retains the requested admin path', async () => {
    const auth = { isAdmin: false, restoreSession: async () => {} }
    const guard = createAdminNavigationGuard(() => auth, false)

    await expect(guard(route({ requiresAdmin: true }, '/admin/pages'))).resolves.toEqual({
      name: 'admin-login',
      query: { redirect: '/admin/pages' }
    })
  })

  it('redirects an authenticated administrator away from login', async () => {
    const auth = { isAdmin: true, restoreSession: async () => {} }
    const guard = createAdminNavigationGuard(() => auth, false)

    await expect(guard(route({ requiresGuest: true }, '/admin/login'))).resolves.toEqual({
      name: 'admin-home'
    })
  })

  it('restores the safe administrator DTO with the request-scoped client during SSR', async () => {
    const requestClient = { get: () => {} }
    let restoredClient = null
    const auth = {
      isAdmin: true,
      restoreSession: async (httpClient) => {
        restoredClient = httpClient
      }
    }
    const guard = createAdminNavigationGuard(
      () => auth,
      true,
      () => requestClient
    )

    await expect(guard(route({ requiresAdmin: true }))).resolves.toBe(true)
    expect(restoredClient).toBe(requestClient)
  })
})
