import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { describe, expect, it } from 'vitest'

import routes from 'src/router/routes'

function adminRoute(path) {
  const root = routes.find((route) => route.path === '/admin')

  return root?.children?.find((child) => child.path === path)
}

function source(relativePath) {
  const path = resolve(process.cwd(), relativePath)

  expect(existsSync(path)).toBe(true)
  return readFileSync(path, 'utf8')
}

describe('admin portfolio presentation', () => {
  it('uses the protected route and supported project contract', () => {
    expect(adminRoute('portfolio')).toMatchObject({
      name: 'admin-portfolio',
      meta: { requiresAdmin: true, noindex: true }
    })

    const portfolio = source('src/pages/admin/AdminPortfolioPage.vue')
    expect(portfolio).toContain("'/api/v1/admin/portfolio/projects'")
    expect(portfolio).toContain('projectKey')
    expect(portfolio).toContain('coverMediaId')
    expect(portfolio).toContain('projectUrl')
    expect(portfolio).toContain('repositoryUrl')
    expect(portfolio).toContain('startedOn')
    expect(portfolio).toContain('sortOrder')
    expect(portfolio).toContain('AdminLocaleTabs')
    expect(portfolio).toContain('AdminMarkdownPreview')
    expect(portfolio).toContain('AdminLifecycleActions')
    expect(portfolio).toContain('isVersionConflict')
    expect(portfolio).toContain('/portfolio/')
  })
})

describe('admin skills presentation', () => {
  it('uses the protected route and supported category and skill contracts', () => {
    expect(adminRoute('skills')).toMatchObject({
      name: 'admin-skills',
      meta: { requiresAdmin: true, noindex: true }
    })

    const skills = source('src/pages/admin/AdminSkillsPage.vue')
    expect(skills).toContain("'/api/v1/admin/skills/categories'")
    expect(skills).toContain("'/api/v1/admin/skills'")
    expect(skills).toContain('categoryKey')
    expect(skills).toContain('categoryId')
    expect(skills).toContain('skillKey')
    expect(skills).toContain('sortOrder')
    expect(skills).toContain('AdminLocaleTabs')
    expect(skills).toContain('version')
    expect(skills).toContain('isVersionConflict')
  })
})

describe('admin media presentation', () => {
  it('uses the protected route and browser-safe media contract', () => {
    expect(adminRoute('media')).toMatchObject({
      name: 'admin-media',
      meta: { requiresAdmin: true, noindex: true }
    })

    const media = source('src/pages/admin/AdminMediaPage.vue')
    expect(media).toContain("'/api/v1/admin/media'")
    expect(media).toContain('FormData')
    expect(media).toContain('onUploadProgress')
    expect(media).toContain('image/png')
    expect(media).toContain('image/jpeg')
    expect(media).toContain('image/webp')
    expect(media).toContain('application/pdf')
    expect(media).toContain('duplicate')
  })
})

describe('admin presentation route loading', () => {
  it('loads every added dedicated screen through its lazy route', async () => {
    const portfolio = await adminRoute('portfolio').component()
    const skills = await adminRoute('skills').component()
    const media = await adminRoute('media').component()

    expect(portfolio.default).toBeDefined()
    expect(skills.default).toBeDefined()
    expect(media.default).toBeDefined()
  })
})

describe('admin social links presentation', () => {
  it('uses a protected dedicated route and only the supported social-link fields', () => {
    expect(adminRoute('social-links')).toMatchObject({
      name: 'admin-social-links',
      meta: { requiresAdmin: true, noindex: true }
    })

    const socialLinks = source('src/pages/admin/AdminSocialLinksPage.vue')
    expect(socialLinks).toContain("'/api/v1/admin/social-links'")
    expect(socialLinks).toContain('platformCode')
    expect(socialLinks).toContain('url')
    expect(socialLinks).toContain('sortOrder')
    expect(socialLinks).toContain('version')
    expect(socialLinks).not.toContain('AdminLocaleTabs')
  })

  it('keeps social-link saves safe and reloads persisted results', () => {
    const socialLinks = source('src/pages/admin/AdminSocialLinksPage.vue')

    expect(socialLinks).toContain('type="url"')
    expect(socialLinks).toContain('primeCsrfToken')
    expect(socialLinks).toContain('isVersionConflict')
    expect(socialLinks).toContain('createUnsavedChangesGuard')
    expect(socialLinks).toContain("transition('deactivate')")
    expect(socialLinks).toContain('await load(page.value)')
    expect(socialLinks).toContain(':loading="saving"')
  })
})

describe('admin featured content presentation', () => {
  it('uses a protected dedicated route and the supported featured-item contract', () => {
    expect(adminRoute('featured')).toMatchObject({
      name: 'admin-featured',
      meta: { requiresAdmin: true, noindex: true }
    })

    const featured = source('src/pages/admin/AdminFeaturedPage.vue')
    expect(featured).toContain("'/api/v1/admin/featured-items'")
    expect(featured).toContain('slotKey')
    expect(featured).toContain('targetType')
    expect(featured).toContain('targetId')
    expect(featured).toContain('sortOrder')
    expect(featured).toContain('version')
  })

  it('selects supported publication and portfolio targets, filters them, and protects saves', () => {
    const featured = source('src/pages/admin/AdminFeaturedPage.vue')

    expect(featured).toContain("'/api/v1/admin/publications'")
    expect(featured).toContain("'/api/v1/admin/portfolio/projects'")
    expect(featured).toContain('PUBLICATION')
    expect(featured).toContain('PORTFOLIO_PROJECT')
    expect(featured).toContain('targetOptions')
    expect(featured).toContain('primeCsrfToken')
    expect(featured).toContain('isVersionConflict')
    expect(featured).toContain('createUnsavedChangesGuard')
    expect(featured).toContain("transition('deactivate')")
    expect(featured).toContain('await load(page.value)')
  })
})

describe('public home presentation content', () => {
  it('renders the supported featured and social payloads without inventing a preview API', () => {
    const home = source('src/pages/public/PublicHomePage.vue')

    expect(home).toContain('data.value?.featured')
    expect(home).toContain('data.value?.socialLinks')
    expect(home).toContain('item.slug')
    expect(home).toContain('link.url')
  })
})
