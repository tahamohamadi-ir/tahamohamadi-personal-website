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

describe('admin managed pages', () => {
  it('assigns pages to a protected dedicated screen', () => {
    expect(adminRoute('pages')).toMatchObject({
      name: 'admin-pages',
      meta: { requiresAdmin: true, noindex: true }
    })
  })

  it('uses only the supported endpoints and fields', () => {
    const page = source('src/pages/admin/AdminPagesPage.vue')

    expect(page).toContain("'/api/v1/admin/pages'")
    expect(page).toContain('pageKey')
    expect(page).toContain('bodyMarkdown')
    expect(page).toContain('seoTitle')
    expect(page).toContain('seoDescription')
    expect(page).toContain('canonicalPath')
    expect(page).toContain('version')
    expect(page).toContain('AdminLocaleTabs')
    expect(page).toContain('AdminMarkdownPreview')
    expect(page).toContain('AdminLifecycleActions')
    expect(page).toContain('selectedLocale.value')
    expect(page).toContain('publicPreviewPath')
  })

})

describe('admin resume', () => {
  it('assigns resume to a protected dedicated screen', () => {
    expect(adminRoute('resume')).toMatchObject({
      name: 'admin-resume',
      meta: { requiresAdmin: true, noindex: true }
    })
  })

  it('uses only supported entry and document endpoints and fields', () => {
    const resume = source('src/pages/admin/AdminResumePage.vue')

    expect(resume).toContain("'/api/v1/admin/resume/entries'")
    expect(resume).toContain("'/api/v1/admin/resume/documents'")
    expect(resume).toContain('entryType')
    expect(resume).toContain('startedOn')
    expect(resume).toContain('endedOn')
    expect(resume).toContain('sortOrder')
    expect(resume).toContain('mediaAssetId')
    expect(resume).toContain('version')
    expect(resume).toContain('AdminLocaleTabs')
    expect(resume).toContain('AdminLifecycleActions')
  })

})

describe('admin publications', () => {
  it('assigns publications to a protected dedicated screen', () => {
    expect(adminRoute('publications')).toMatchObject({
      name: 'admin-publications',
      meta: { requiresAdmin: true, noindex: true }
    })
  })

  it('uses only supported endpoints and bibliographic fields', () => {
    const publications = source('src/pages/admin/AdminPublicationsPage.vue')

    expect(publications).toContain("'/api/v1/admin/publications'")
    expect(publications).toContain('publicationKey')
    expect(publications).toContain('publicationStage')
    expect(publications).toContain('doi')
    expect(publications).toContain('externalUrl')
    expect(publications).toContain('publishedOn')
    expect(publications).toContain('authorsDisplay')
    expect(publications).toContain('venueDisplay')
    expect(publications).toContain('version')
    expect(publications).toContain('AdminLocaleTabs')
    expect(publications).toContain('AdminLifecycleActions')
    expect(publications).toContain('selectedLocale.value')
    expect(publications).toContain('publicPreviewPath')
  })
})

describe('admin academic content route loading', () => {
  it('loads every dedicated screen through its lazy route', async () => {
    const pages = await adminRoute('pages').component()
    const resume = await adminRoute('resume').component()
    const publications = await adminRoute('publications').component()

    expect(pages.default).toBeDefined()
    expect(resume.default).toBeDefined()
    expect(publications.default).toBeDefined()
  })
})
