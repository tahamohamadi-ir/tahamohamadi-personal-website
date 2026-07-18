import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { flushPromises, mount } from '@vue/test-utils'
import { Quasar } from 'quasar'
import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it, vi } from 'vitest'

import { i18n } from 'src/boot/i18n'
import { PUBLIC_API_KEY } from 'src/services/apiContext'

const workingDirectory = process.cwd()
const projectRoot = (
  workingDirectory.split(/[\\/]/).pop().toLowerCase() === 'frontend'
)
  ? resolve(workingDirectory, '..')
  : workingDirectory

const COLLECTIONS = [
  {
    name: 'blog',
    pagePath: 'frontend/src/pages/public/BlogPage.vue',
    listPath: 'frontend/src/components/public/BlogPostList.vue',
    routePath: 'blog',
    apiMethod: 'listPosts',
    response: {
      locale: 'en',
      availableLocales: ['en'],
      canonicalPath: '/en/posts',
      hreflang: [{ locale: 'en', path: '/en/posts' }],
      seo: { title: null, description: null },
      ogMedia: null,
      lastModified: null,
      items: [{
        locale: 'en',
        availableLocales: ['en'],
        canonicalPath: '/en/posts/first-post',
        hreflang: [{ locale: 'en', path: '/en/posts/first-post' }],
        seo: { title: null, description: null },
        ogMedia: {
          url: '/api/v1/public/media/post-cover',
          altText: 'Research notes on a desk'
        },
        lastModified: null,
        slug: 'first-post',
        title: 'First post',
        excerpt: 'Escaped <strong>article</strong> excerpt.',
        publishedAt: '2026-07-01T10:00:00Z'
      }, {
        locale: 'en',
        availableLocales: ['en'],
        canonicalPath: '/en/posts/second-post',
        hreflang: [{ locale: 'en', path: '/en/posts/second-post' }],
        seo: { title: null, description: null },
        ogMedia: null,
        lastModified: null,
        slug: 'second-post',
        title: 'Second post',
        excerpt: null,
        publishedAt: null
      }],
      page: 0,
      size: 20,
      totalElements: 22,
      totalPages: 2
    },
    itemKeys: [
      'availableLocales', 'canonicalPath', 'excerpt', 'hreflang',
      'lastModified', 'locale', 'ogMedia', 'publishedAt', 'seo', 'slug',
      'title'
    ]
  },
  {
    name: 'portfolio',
    pagePath: 'frontend/src/pages/public/PortfolioPage.vue',
    listPath: 'frontend/src/components/public/PortfolioProjectList.vue',
    routePath: 'portfolio',
    apiMethod: 'listPortfolio',
    response: {
      locale: 'en',
      availableLocales: ['en'],
      canonicalPath: '/en/portfolio',
      hreflang: [{ locale: 'en', path: '/en/portfolio' }],
      seo: { title: null, description: null },
      ogMedia: null,
      lastModified: null,
      items: [{
        locale: 'en',
        availableLocales: ['en'],
        canonicalPath: '/en/portfolio/first-project',
        hreflang: [{ locale: 'en', path: '/en/portfolio/first-project' }],
        seo: { title: null, description: null },
        ogMedia: null,
        lastModified: null,
        slug: 'first-project',
        title: 'First project',
        summary: 'A focused project summary.'
      }, {
        locale: 'en',
        availableLocales: ['en'],
        canonicalPath: '/en/portfolio/second-project',
        hreflang: [{ locale: 'en', path: '/en/portfolio/second-project' }],
        seo: { title: null, description: null },
        ogMedia: null,
        lastModified: null,
        slug: 'second-project',
        title: 'Second project',
        summary: null
      }],
      page: 0,
      size: 20,
      totalElements: 2,
      totalPages: 1
    },
    itemKeys: [
      'availableLocales', 'canonicalPath', 'hreflang', 'lastModified',
      'locale', 'ogMedia', 'seo', 'slug', 'summary', 'title'
    ]
  },
  {
    name: 'publications',
    pagePath: 'frontend/src/pages/public/PublicationsPage.vue',
    listPath: 'frontend/src/components/public/PublicationList.vue',
    routePath: 'publications',
    apiMethod: 'listPublications',
    response: {
      locale: 'en',
      availableLocales: ['en'],
      canonicalPath: '/en/publications',
      hreflang: [{ locale: 'en', path: '/en/publications' }],
      seo: { title: null, description: null },
      ogMedia: null,
      lastModified: null,
      items: [{
        locale: 'en',
        availableLocales: ['en'],
        canonicalPath: '/en/publications/first-paper',
        hreflang: [{ locale: 'en', path: '/en/publications/first-paper' }],
        seo: { title: null, description: null },
        ogMedia: null,
        lastModified: null,
        slug: 'first-paper',
        title: 'First paper',
        abstractText: 'Escaped <em>research</em> abstract.',
        authorsDisplay: 'A. Researcher',
        venueDisplay: 'Journal of Evidence',
        stage: 'PUBLISHED',
        year: 2026,
        publishedOn: '2026-06-01',
        doi: '10.1000/example',
        externalUrl: 'https://example.org/publications/first-paper'
      }],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1
    },
    itemKeys: [
      'abstractText', 'authorsDisplay', 'availableLocales', 'canonicalPath',
      'doi', 'externalUrl', 'hreflang', 'lastModified', 'locale', 'ogMedia',
      'publishedOn', 'seo', 'slug', 'stage', 'title', 'venueDisplay', 'year'
    ]
  }
]

function readProjectFile(projectRelativePath) {
  const filePath = resolve(projectRoot, projectRelativePath)
  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${projectRelativePath}`)
  }

  return readFileSync(filePath, 'utf8')
}

async function loadComponent(projectRelativePath) {
  readProjectFile(projectRelativePath)
  const module = await vi.importActual(resolve(projectRoot, projectRelativePath))
  return module.default
}

function findLocaleRoute(routes, locale) {
  return routes.find((route) => route.path === `/${locale}`)
}

function findNamedRoute(routes, locale, name) {
  return findLocaleRoute(routes, locale)?.children
    .find((route) => route.name === `${locale}-${name}`)
}

async function mountLocalized(component, collection, api, {
  locale = 'en',
  props = {},
  query = ''
} = {}) {
  i18n.global.locale.value = locale
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{
      path: `/${locale}/${collection.routePath}`,
      name: `${locale}-${collection.name}-contract`,
      component: { template: '<div />' },
      meta: { locale, direction: locale === 'fa' ? 'rtl' : 'ltr' }
    }]
  })
  await router.push(`/${locale}/${collection.routePath}${query}`)
  await router.isReady()

  return mount(component, {
    props,
    global: {
      plugins: [Quasar, router, i18n],
      provide: { [PUBLIC_API_KEY]: api }
    }
  })
}

function localizedResponse(collection, locale) {
  const localizedItems = collection.response.items.map((item) => ({
    ...item,
    locale,
    title: locale === 'fa' ? 'محتوای فارسی' : item.title
  }))
  return {
    ...collection.response,
    locale,
    availableLocales: [locale],
    items: localizedItems
  }
}

describe('localized public collection route contract', () => {
  it('moves only the six collection routes from placeholder ownership', async () => {
    const { default: routes } = await import('src/router/routes')
    const PlaceholderPage = await loadComponent(
      'frontend/src/pages/public/PublicRoutePlaceholderPage.vue'
    )

    for (const [locale, direction] of [['en', 'ltr'], ['fa', 'rtl']]) {
      for (const collection of COLLECTIONS) {
        const Page = await loadComponent(collection.pagePath)
        const route = findNamedRoute(routes, locale, collection.name)
        expect(route).toMatchObject({
          path: collection.routePath,
          name: `${locale}-${collection.name}`,
          meta: {
            locale,
            direction,
            pageKey: collection.name,
            contentType: collection.name === 'blog'
              ? 'post-list'
              : collection.name === 'portfolio'
                ? 'project-list'
                : 'publication-list'
          }
        })
        const routeComponent = await route.component()
        expect(routeComponent.default ?? routeComponent).toBe(Page)
      }

      for (const name of ['about', 'research', 'skills', 'blog-detail', 'portfolio-detail', 'publication-detail', 'contact']) {
        const route = findNamedRoute(routes, locale, name)
        const routeComponent = await route.component()
        expect(routeComponent.default ?? routeComponent).toBe(PlaceholderPage)
      }
    }
  })

  it('uses only request-scoped APIs, exact list methods, and no browser or raw-HTML escape hatches', () => {
    for (const collection of COLLECTIONS) {
      const source = readProjectFile(collection.pagePath)
      expect(source).toMatch(/inject\s*\(\s*PUBLIC_API_KEY\s*\)/)
      expect(source).toMatch(/useAsyncPage/)
      expect(source).toMatch(new RegExp(`${collection.apiMethod}\\s*\\(`))
      expect(source).not.toMatch(/axios\s*[.(]/i)
      expect(source).not.toMatch(/window|document|navigator|location|storage/i)
      expect(source).not.toMatch(/v-html|innerHTML|markdown/i)
    }
  })

  it('renders exact API item order, accepted fields, and escaped text in each domain-specific semantic list', async () => {
    for (const collection of COLLECTIONS) {
      const Page = await loadComponent(collection.pagePath)
      const response = localizedResponse(collection, 'en')
      const api = { [collection.apiMethod]: vi.fn().mockResolvedValue(response) }
      const wrapper = await mountLocalized(Page, collection, api)

      await flushPromises()
      expect(api[collection.apiMethod]).toHaveBeenCalledWith('en', {
        page: 0,
        size: 20
      })
      expect(Object.keys(response.items[0]).sort()).toEqual(
        [...collection.itemKeys].sort()
      )
      expect(wrapper.findAll('main, .q-page')).toHaveLength(0)
      expect(wrapper.findAll('[lang], [dir]')).toHaveLength(0)
      expect(wrapper.findAll('h1')).toHaveLength(1)
      expect(wrapper.findAll('ol')).toHaveLength(1)
      expect(wrapper.findAll('article')).toHaveLength(response.items.length)
      expect(wrapper.findAll('li').map((item) => item.text())).toEqual(
        response.items.map((item) => expect.stringContaining(item.title))
      )
      expect(wrapper.html()).not.toContain('<strong>article</strong>')
      expect(wrapper.html()).not.toContain('<em>research</em>')
      expect(wrapper.text()).not.toContain('internalMediaId')
      expect(wrapper.text()).not.toContain('version')
      if (collection.name === 'blog') {
        expect(wrapper.get('img').attributes('src'))
          .toBe(response.items[0].ogMedia.url)
        expect(wrapper.get('img').attributes('alt'))
          .toBe(response.items[0].ogMedia.altText)
      }
      wrapper.unmount()
    }
  })

  it('uses items alone for emptiness and keeps initial SSR data from causing a duplicate request', async () => {
    for (const collection of COLLECTIONS) {
      const Page = await loadComponent(collection.pagePath)
      const metadataOnly = {
        ...localizedResponse(collection, 'en'),
        items: [],
        totalElements: 10,
        totalPages: 1
      }
      const api = { [collection.apiMethod]: vi.fn() }
      const wrapper = await mountLocalized(Page, collection, api, {
        props: { initialData: metadataOnly }
      })

      await flushPromises()
      expect(api[collection.apiMethod]).not.toHaveBeenCalled()
      expect(wrapper.findAll('h1')).toHaveLength(1)
      expect(wrapper.get('[role="status"]').exists()).toBe(true)
      wrapper.unmount()
    }
  })

  it('keeps EN and FA content isolated without component-level language or direction ownership', async () => {
    for (const collection of COLLECTIONS) {
      const Page = await loadComponent(collection.pagePath)
      const faResponse = localizedResponse(collection, 'fa')
      const api = { [collection.apiMethod]: vi.fn().mockResolvedValue(faResponse) }
      const wrapper = await mountLocalized(Page, collection, api, { locale: 'fa' })

      await flushPromises()
      expect(api[collection.apiMethod]).toHaveBeenCalledWith('fa', {
        page: 0,
        size: 20
      })
      expect(wrapper.text()).toContain(faResponse.items[0].title)
      expect(wrapper.text()).not.toContain(collection.response.items[0].title)
      expect(wrapper.findAll('[lang], [dir]')).toHaveLength(0)
      wrapper.unmount()
    }
  })
})

describe('collection states and pagination contract', () => {
  it('shows loading, stale content, recoverable failure, and offline states through PageState', async () => {
    const collection = COLLECTIONS[0]
    const Page = await loadComponent(collection.pagePath)
    let resolveInitial
    let resolveRefresh
    const initial = new Promise((resolveValue) => { resolveInitial = resolveValue })
    const refresh = new Promise((resolveValue) => { resolveRefresh = resolveValue })
    const api = {
      listPosts: vi.fn()
        .mockReturnValueOnce(initial)
        .mockReturnValueOnce(refresh)
    }
    const wrapper = await mountLocalized(Page, collection, api)

    expect(wrapper.get('[role="status"]').text()).toMatch(/loading/i)
    resolveInitial(collection.response)
    await flushPromises()
    expect(wrapper.text()).toContain('First post')

    await wrapper.get('button[aria-label="Previous page"]').trigger('click')
    expect(api.listPosts).toHaveBeenCalledTimes(1)
    await wrapper.get('button[aria-label="Next page"]').trigger('click')
    await flushPromises()
    expect(wrapper.get('[role="status"]').text()).toMatch(/stale|outdated|refresh/i)
    expect(wrapper.text()).toContain('First post')
    resolveRefresh({ ...collection.response, page: 1 })
    await flushPromises()

    const recoverable = await mountLocalized(Page, collection, {
      listPosts: vi.fn().mockRejectedValue({
        response: { status: 503, data: { code: 'SERVICE_UNAVAILABLE' } }
      })
    })
    await flushPromises()
    expect(recoverable.get('[role="alert"]').exists()).toBe(true)

    const offline = await mountLocalized(Page, collection, {
      listPosts: vi.fn().mockRejectedValue(new Error('network unavailable'))
    })
    await flushPromises()
    expect(offline.get('[role="status"]').text()).toMatch(/offline|connection|network/i)
  })

  it('requests only the backend-supported next zero-based page and preserves the active localized route', async () => {
    const collection = COLLECTIONS[0]
    const Page = await loadComponent(collection.pagePath)
    const api = {
      listPosts: vi.fn()
        .mockResolvedValueOnce(collection.response)
        .mockResolvedValueOnce({ ...collection.response, page: 1 })
    }
    const wrapper = await mountLocalized(Page, collection, api)

    await flushPromises()
    await wrapper.get('button[aria-label="Next page"]').trigger('click')
    await flushPromises()

    expect(api.listPosts).toHaveBeenLastCalledWith('en', { page: 1, size: 20 })
    expect(wrapper.get('[aria-label="Collection pagination"]').text())
      .toMatch(/Page 2 of 2/)
    expect(wrapper.get('button[aria-label="Next page"]').attributes('disabled'))
      .toBeDefined()
  })

  it('uses safe external publication links with unchanged public URLs', async () => {
    const PublicationList = await loadComponent(
      'frontend/src/components/public/PublicationList.vue'
    )
    const publication = COLLECTIONS[2].response.items[0]
    const wrapper = mount(PublicationList, {
      props: { publications: [publication] },
      global: { plugins: [i18n] }
    })
    const link = wrapper.get(`a[href="${publication.externalUrl}"]`)

    expect(link.attributes('target')).toBe('_blank')
    expect(link.attributes('rel')).toContain('noopener')
    expect(link.attributes('rel')).toContain('noreferrer')
    expect(wrapper.text()).toContain(publication.doi)
  })
})
