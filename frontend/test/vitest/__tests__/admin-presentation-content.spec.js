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
