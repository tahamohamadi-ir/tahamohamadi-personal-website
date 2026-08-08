import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { describe, expect, it } from 'vitest'

import routes from 'src/router/routes'

function adminRoute(path) {
  return routes.find((route) => route.path === '/admin')?.children?.find(
    (route) => route.path === path
  )
}

function source(relativePath) {
  const path = resolve(process.cwd(), relativePath)
  expect(existsSync(path)).toBe(true)
  return readFileSync(path, 'utf8')
}

describe('admin source-backed dashboard', () => {
  it('uses only the supported analytics summary fields', () => {
    const dashboard = source('src/pages/admin/AdminHomePage.vue')

    expect(dashboard).toContain("'/api/v1/admin/analytics/summary'")
    expect(dashboard).toContain('pages')
    expect(dashboard).toContain('posts')
    expect(dashboard).toContain('media')
    expect(dashboard).toContain('newContactMessages')
    expect(dashboard).not.toContain('missingTranslations')
  })

  it('keeps site settings as a dense, localized operational form', () => {
    const settings = source('src/pages/admin/AdminSiteSettingsPage.vue')

    expect(settings).toContain('admin-site-settings__panel')
    expect(settings).toContain("t('admin.siteSettings.identity')")
    expect(settings).toContain("t('admin.siteSettings.footer')")
    expect(settings).toContain("t('admin.siteSettings.presentation')")
    expect(settings).toContain('outlined')
    expect(settings).toContain(':rows="3"')
    expect(settings).toContain('changes.isDirty')
  })

  it('keeps Pages as a panelled operational editor without changing its composer contract', () => {
    const page = source('src/pages/admin/AdminPagesPage.vue')

    expect(page).toContain('admin-pages__panel')
    expect(page).toContain('admin-pages__actions')
    expect(page).toContain('outlined')
    expect(page).toContain(':rows="3"')
    expect(page).toContain('AdminPageBlockComposer')
    expect(page).toContain('compositionSaved')
    expect(page).toContain('/api/v1/admin/pages/${id}/revisions')
    expect(page).toContain('restore-as-draft')
    expect(page).toContain('restoreConfirmationOpen')
    expect(page).toContain('admin.pages.restoreDescription')
    expect(page).toContain('snapshotComposition')
  })

  it('restores a Page Edit revision through the versioned restore-as-draft contract', () => {
    const page = source('src/pages/admin/AdminPageEditPage.vue')
    const saveSource = page.slice(page.indexOf('async function save'), page.indexOf('async function updateStatus'))
    const lifecycleSource = page.slice(page.indexOf('async function updateStatus'), page.indexOf('onMounted(load)'))

    expect(page).toContain('restore-as-draft')
    expect(page).toContain('{ params: { version: form.value.version } }')
    expect(page).toContain('isVersionConflict(error)')
    expect(page).toContain("router.replace({ name: 'admin-pages-edit', params: { id: response.data.id } })")
    expect(page).toContain(':loading="saving" :disable="saving"')
    expect(page).not.toMatch(/revisions\/\$\{restoreCandidate\.value\.id\}\/restore`/)
    expect(saveSource).toContain('version: form.value.version')
    expect(lifecycleSource).toContain('{ params: { version: form.value.version } }')
  })
})

describe('admin blog and contact workflows', () => {
  it('protects the new routes from public access and lazy-loads them', async () => {
    const paths = ['blog/posts', 'blog/categories', 'blog/tags', 'translation-queue', 'contact-messages']

    for (const path of paths) {
      expect(adminRoute(path)).toMatchObject({
        meta: { requiresAdmin: true, noindex: true }
      })
      const component = await adminRoute(path).component()
      expect(component.default).toBeDefined()
    }
  }, 60_000)

  it('uses only the supported blog post, taxonomy, and contact endpoints', () => {
    const posts = source('src/pages/admin/AdminBlogPostsPage.vue')
    const categories = source('src/pages/admin/AdminBlogCategoriesPage.vue')
    const tags = source('src/pages/admin/AdminBlogTagsPage.vue')
    const contacts = source('src/pages/admin/AdminContactMessagesPage.vue')

    expect(posts).toContain("'/api/v1/admin/blog/posts'")
    expect(posts).toContain("'/api/v1/admin/blog/categories'")
    expect(posts).toContain("'/api/v1/admin/blog/tags'")
    expect(posts).toContain('AdminMediaSelector')
    expect(posts).toContain('v-model="mediaIds" multiple')
    expect(posts).toContain("usage: 'INLINE'")
    expect(posts).toContain('AdminMarkdownPreview')
    expect(posts).toContain('ArticleBlockEditor')
    expect(posts).toContain('articleEditorMode')
    expect(posts).toContain('v-model:document="activeTranslation.articleDocument"')
      expect(posts).toContain('AdminLifecycleActions')
      expect(posts).toContain('/revisions')
      expect(posts).toContain('restore-as-draft')
      expect(posts).toContain('restoreRevision')
      expect(posts).toContain("transition('submit-for-review')")
      expect(posts).toContain("transition('return-to-draft')")
      expect(posts).toContain("form.status === 'IN_REVIEW'")
      expect(posts).toContain('useI18n')
    expect(posts).toContain("t('admin.blogPosts.title')")
    expect(posts).toContain("t('admin.blogPosts.item')")
    expect(posts).toContain("t('admin.blogPosts.edit')")
    expect(posts).toContain("t('admin.blogPosts.version'")
    expect(categories).toContain("'/api/v1/admin/blog/categories'")
    expect(categories).toContain('categoryKey')
    expect(categories).toContain('useI18n')
    expect(categories).toContain("t('admin.blogTaxonomy.categories.title')")
    expect(categories).toContain('deactivateConfirmationOpen')
    expect(tags).toContain("'/api/v1/admin/blog/tags'")
    expect(tags).toContain('tagKey')
    expect(tags).toContain('useI18n')
    expect(tags).toContain("t('admin.blogTaxonomy.tags.title')")
    expect(tags).toContain('deactivateConfirmationOpen')
    expect(contacts).toContain("'/api/v1/admin/contact-messages'")
    expect(contacts).toContain("transition('read')")
    expect(contacts).toContain("transition('archive')")
    expect(contacts).toContain(':dir="detailDirection"')
  })

  it('keeps the translation queue limited to source-backed Blog data', () => {
    const queue = source('src/pages/admin/AdminTranslationQueuePage.vue')

    expect(queue).toContain("'/api/v1/admin/blog/posts'")
    expect(queue).toContain('size: 100')
    expect(queue).toContain('faTranslationStatus')
    expect(queue).toContain('enTranslationStatus')
    expect(queue).toContain('sourceUpdatedAt')
    expect(queue).toContain('checklist')
    expect(queue).toContain('OUTDATED')
  })
})

describe('admin publication workflow', () => {
  it('keeps the supported publication API and its operational labels bilingual', () => {
    const publications = source('src/pages/admin/AdminPublicationsPage.vue')

    expect(publications).toContain("'/api/v1/admin/publications'")
    expect(publications).toContain('AdminLocaleTabs')
    expect(publications).toContain('AdminLifecycleActions')
    expect(publications).toContain('useI18n')
    expect(publications).toContain("t('admin.publications.title')")
    expect(publications).toContain("t('admin.publications.save')")
  })
})

describe('admin resume workflow', () => {
  it('keeps entries and localized documents inside the supported admin workflow', () => {
    const resume = source('src/pages/admin/AdminResumePage.vue')

    expect(resume).toContain("'/api/v1/admin/resume/entries'")
    expect(resume).toContain("'/api/v1/admin/resume/documents'")
    expect(resume).toContain('AdminMediaSelector')
    expect(resume).toContain('AdminLocaleTabs')
    expect(resume).toContain('useI18n')
    expect(resume).toContain("t('admin.resume.title')")
    expect(resume).toContain("t('admin.resume.saveDocument')")
  })
})

describe('admin social-link workflow', () => {
  it('keeps social-link validation, lifecycle actions, and operational copy localized', () => {
    const socialLinks = source('src/pages/admin/AdminSocialLinksPage.vue')

    expect(socialLinks).toContain("'/api/v1/admin/social-links'")
    expect(socialLinks).toContain('AdminActivationActions')
    expect(socialLinks).toContain('useI18n')
    expect(socialLinks).toContain("t('admin.socialLinks.title')")
    expect(socialLinks).toContain("t('admin.socialLinks.invalidUrl')")
    expect(socialLinks).toContain('createUnsavedChangesGuard')
  })
})

describe('admin portfolio workflow', () => {
  it('keeps bilingual project editing, media selection, and lifecycle actions together', () => {
    const portfolio = source('src/pages/admin/AdminPortfolioPage.vue')

    expect(portfolio).toContain("'/api/v1/admin/portfolio/projects'")
    expect(portfolio).toContain('AdminMediaSelector')
    expect(portfolio).toContain('AdminLocaleTabs')
    expect(portfolio).toContain('AdminLifecycleActions')
    expect(portfolio).toContain('useI18n')
    expect(portfolio).toContain("t('admin.portfolio.title')")
    expect(portfolio).toContain("t('admin.portfolio.save')")
  })
})

describe('admin skills workflow', () => {
  it('keeps bilingual skill editing and version-aware deactivation confirmation', () => {
    const skills = source('src/pages/admin/AdminSkillsPage.vue')

    expect(skills).toContain("'/api/v1/admin/skills/categories'")
    expect(skills).toContain("'/api/v1/admin/skills'")
    expect(skills).toContain('useI18n')
    expect(skills).toContain("t('admin.skills.title')")
    expect(skills).toContain('deactivationConfirmationOpen')
    expect(skills).toContain('params: { version: value.version }')
    expect(skills).toContain('AdminLocaleTabs')
  })
})
