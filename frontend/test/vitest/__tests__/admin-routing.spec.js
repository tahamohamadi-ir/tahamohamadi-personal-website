import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { describe, expect, it } from 'vitest'

import routes from 'src/router/routes'

function adminRoute(path) {
  const root = routes.find((route) => route.path === '/admin')

  return root?.children?.find((child) => child.path === path)
}

describe('admin routing contract', () => {
  it('keeps login public and marks CMS routes as admin-only', () => {
    const login = routes.find((route) => route.path === '/admin/login')
    const home = adminRoute('')

    expect(login).toMatchObject({
      name: 'admin-login',
      meta: { requiresGuest: true, noindex: true }
    })
    expect(home).toMatchObject({
      name: 'admin-home',
      meta: { requiresAdmin: true, noindex: true }
    })
  })

  it('uses the existing CSRF session endpoints without browser token storage', () => {
    const sourcePath = resolve(process.cwd(), 'src/services/adminAuthApi.js')
    const source = existsSync(sourcePath)
      ? readFileSync(sourcePath, 'utf8')
      : ''

    expect(source).toContain("'/api/v1/auth/login'")
    expect(source).toContain("'/api/v1/auth/me'")
    expect(source).toContain("'/api/v1/auth/logout'")
    expect(source).toContain('primeCsrfToken')
    expect(source).not.toMatch(/localStorage|sessionStorage|Authorization/)
  })

  it('enforces guest and administrator route metadata on the client', () => {
    const source = readFileSync(
      resolve(process.cwd(), 'src/router/index.js'),
      'utf8'
    )

    expect(source).toContain('router.beforeEach')
    expect(source).toContain('useAdminAuthStore')
    expect(source).toContain('createAdminNavigationGuard')
    expect(source).toContain('process.env.SERVER')
  })

  it('provides a responsive login form and an authenticated shell', () => {
    const loginSource = readFileSync(
      resolve(process.cwd(), 'src/pages/admin/AdminLoginPage.vue'),
      'utf8'
    )
    const layoutSource = readFileSync(
      resolve(process.cwd(), 'src/layouts/AdminLayout.vue'),
      'utf8'
    )

    expect(loginSource).toContain('q-form')
    expect(loginSource).toContain('type="email"')
    expect(loginSource).toContain('type="password"')
    expect(loginSource).toContain('useAdminAuthStore')
    expect(layoutSource).toContain('q-drawer')
    expect(layoutSource).toContain('logout')
    expect(layoutSource).toContain('noindex')
  })

  it('assigns the managed pages route to a dedicated CMS page', () => {
    const pages = adminRoute('pages')

    expect(pages).toMatchObject({
      name: 'admin-pages',
      meta: { requiresAdmin: true, noindex: true }
    })
  })

  it('provides a noindex not-found destination inside the protected admin shell', () => {
    const notFound = adminRoute(':pathMatch(.*)*')

    expect(notFound).toMatchObject({
      name: 'admin-not-found',
      meta: { requiresAdmin: true, noindex: true }
    })
  })
})
