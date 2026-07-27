import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { Quasar } from 'quasar'
import { createMemoryHistory, createRouter } from 'vue-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { i18n } from 'src/boot/i18n'
import PageBlockRenderer from 'src/components/public/PageBlockRenderer.vue'
import PublicHomePage from 'src/pages/public/PublicHomePage.vue'
import { PUBLIC_API_KEY } from 'src/services/apiContext'
import {
  createHttpClient,
  normalizeApiError,
  XSRF_COOKIE_NAME,
  XSRF_HEADER_NAME
} from 'src/services/httpClient'

import {
  createPublicApi,
  PUBLIC_API_ROOT,
  SOCIAL_LINKS_ENDPOINT
} from 'src/services/publicApi'

import {
  homeResponse,
  postCollectionResponse,
  postDetailResponse,
  projectDetailResponse,
  publicationDetailResponse,
  translationUnavailableError
} from '../../fixtures/public-api'

const workingDirectory = process.cwd()
const projectRoot = (
  workingDirectory.split(/[\\/]/).pop().toLowerCase() === 'frontend'
)
  ? resolve(workingDirectory, '..')
  : workingDirectory

const HOME_PAGE_PATH = 'frontend/src/pages/public/PublicHomePage.vue'

function readProjectFile(projectRelativePath) {
  const filePath = resolve(projectRoot, projectRelativePath)

  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${projectRelativePath}`)
  }

  return readFileSync(filePath, 'utf8')
}

async function mountPublicPage(component, {
  locale = 'en',
  pageKey = 'home',
  props = {},
  api = {}
} = {}) {
  i18n.global.locale.value = locale

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{
      path: '/page',
      component,
      meta: { pageKey }
    }]
  })

  await router.push('/page')
  await router.isReady()

  return mount(component, {
    props,
    global: {
      plugins: [Quasar, createPinia(), router, i18n],
      provide: { [PUBLIC_API_KEY]: api }
    }
  })
}

function expectPageDoesNotOwnShellLandmarks(wrapper) {
  expect(wrapper.findAll('main, .q-page')).toHaveLength(0)
  expect(wrapper.findAll('[lang], [dir]')).toHaveLength(0)
}

async function mountPageBlockRenderer(props = {}) {
  i18n.global.locale.value = props.locale || 'en'

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/:lang',
        component: { template: '<div />' }
      },
      {
        path: '/:lang/:section/:slug?',
        component: { template: '<div />' }
      }
    ]
  })

  await router.push(`/${props.locale || 'en'}`)
  await router.isReady()

  return mount(PageBlockRenderer, {
    props: {
      locale: 'en',
      blocks: [],
      ...props
    },
    global: {
      plugins: [Quasar, createPinia(), router, i18n]
    }
  })
}

afterEach(() => {
  i18n.global.locale.value = 'en'
})

function createFakeHttpClient() {
  return {
    get: vi.fn(),
    post: vi.fn()
  }
}

describe('public HTTP client contract', () => {
  it('creates isolated request-scoped Axios clients', () => {
    const first = createHttpClient({
      baseURL: 'http://backend.internal:8080'
    })

    const second = createHttpClient({
      baseURL: 'http://backend.internal:8080'
    })

    expect(first).not.toBe(second)

    expect(first.defaults.baseURL)
      .toBe('http://backend.internal:8080')

    expect(first.defaults.withCredentials).toBe(true)
    expect(first.defaults.withXSRFToken).toBe(true)
    expect(first.defaults.xsrfCookieName).toBe(XSRF_COOKIE_NAME)
    expect(first.defaults.xsrfHeaderName).toBe(XSRF_HEADER_NAME)
  })

  it('isolates an SSR Cookie header to one client instance', () => {
    const serverClient = createHttpClient({
      baseURL: 'http://backend.internal:8080',
      cookieHeader: 'XSRF-TOKEN=server-token; SESSION=session-id'
    })

    const browserClient = createHttpClient()

    expect(serverClient.defaults.headers.common.Cookie)
      .toBe('XSRF-TOKEN=server-token; SESSION=session-id')

    expect(browserClient.defaults.headers.common.Cookie)
      .toBeUndefined()
  })
})

describe('public API endpoint contract', () => {
  it('loads localized home and page content', async () => {
    const httpClient = createFakeHttpClient()

    httpClient.get
      .mockResolvedValueOnce({ data: homeResponse })
      .mockResolvedValueOnce({
        data: {
          ...homeResponse,
          canonicalPath: '/fa/about'
        }
      })

    const api = createPublicApi(httpClient)

    await expect(api.getHome('fa'))
      .resolves.toEqual(homeResponse)

    await api.getPage('fa', 'about')

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        1,
        `${PUBLIC_API_ROOT}/fa/home`
      )

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        2,
        `${PUBLIC_API_ROOT}/fa/pages/about`
      )
  })

  it('loads posts with bounded pagination and filters', async () => {
    const httpClient = createFakeHttpClient()

    httpClient.get
      .mockResolvedValueOnce({ data: postCollectionResponse })
      .mockResolvedValueOnce({ data: postDetailResponse })

    const api = createPublicApi(httpClient)

    await expect(
      api.listPosts('en', {
        q: 'architecture',
        category: 'general',
        tag: 'design',
        page: 1,
        size: 20
      })
    ).resolves.toEqual(postCollectionResponse)

    await expect(
      api.getPost('en', 'first-post')
    ).resolves.toEqual(postDetailResponse)

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        1,
        `${PUBLIC_API_ROOT}/en/posts`,
        {
          params: {
            q: 'architecture',
            category: 'general',
            tag: 'design',
            page: 1,
            size: 20
          }
        }
      )

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        2,
        `${PUBLIC_API_ROOT}/en/posts/first-post`
      )
  })

  it('loads localized categories and tags', async () => {
    const httpClient = createFakeHttpClient()

    httpClient.get
      .mockResolvedValueOnce({ data: { items: [] } })
      .mockResolvedValueOnce({ data: { items: [] } })

    const api = createPublicApi(httpClient)

    await api.listCategories('fa')
    await api.listTags('fa')

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        1,
        `${PUBLIC_API_ROOT}/fa/categories`
      )

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        2,
        `${PUBLIC_API_ROOT}/fa/tags`
      )
  })

  it('loads portfolio lists and project details', async () => {
    const httpClient = createFakeHttpClient()

    httpClient.get
      .mockResolvedValueOnce({ data: { items: [] } })
      .mockResolvedValueOnce({ data: projectDetailResponse })

    const api = createPublicApi(httpClient)

    await api.listPortfolio('en', {
      skill: 'java',
      page: 0,
      size: 12
    })

    await expect(
      api.getProject('en', 'sample-project')
    ).resolves.toEqual(projectDetailResponse)

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        1,
        `${PUBLIC_API_ROOT}/en/portfolio`,
        {
          params: {
            skill: 'java',
            page: 0,
            size: 12
          }
        }
      )

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        2,
        `${PUBLIC_API_ROOT}/en/portfolio/sample-project`
      )
  })

  it('loads skills and resume data', async () => {
    const httpClient = createFakeHttpClient()

    httpClient.get
      .mockResolvedValueOnce({ data: { items: [] } })
      .mockResolvedValueOnce({ data: { entries: [] } })

    const api = createPublicApi(httpClient)

    await api.getSkills('fa')
    await api.getResume('fa')

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        1,
        `${PUBLIC_API_ROOT}/fa/skills`
      )

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        2,
        `${PUBLIC_API_ROOT}/fa/resume`
      )
  })

  it('loads publication lists and details', async () => {
    const httpClient = createFakeHttpClient()

    httpClient.get
      .mockResolvedValueOnce({ data: { items: [] } })
      .mockResolvedValueOnce({ data: publicationDetailResponse })

    const api = createPublicApi(httpClient)

    await api.listPublications('en', {
      page: 0,
      size: 20,
      stage: 'PUBLISHED'
    })

    await expect(
      api.getPublication('en', 'sample-publication')
    ).resolves.toEqual(publicationDetailResponse)

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        1,
        `${PUBLIC_API_ROOT}/en/publications`,
        {
          params: {
            page: 0,
            size: 20,
            stage: 'PUBLISHED'
          }
        }
      )

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        2,
        `${PUBLIC_API_ROOT}/en/publications/sample-publication`
      )
  })

  it('loads featured content and global social links', async () => {
    const httpClient = createFakeHttpClient()

    httpClient.get
      .mockResolvedValueOnce({ data: { items: [] } })
      .mockResolvedValueOnce({ data: { items: [] } })

    const api = createPublicApi(httpClient)

    await api.getFeatured('fa', {
      slot: 'home',
      size: 3
    })

    await api.getSocialLinks()

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        1,
        `${PUBLIC_API_ROOT}/fa/featured`,
        {
          params: {
            slot: 'home',
            size: 3
          }
        }
      )

    expect(httpClient.get)
      .toHaveBeenNthCalledWith(
        2,
        SOCIAL_LINKS_ENDPOINT
      )
  })

  it('rejects unsupported locales before any HTTP request', async () => {
    const httpClient = createFakeHttpClient()
    const api = createPublicApi(httpClient)

    await expect(api.getHome('de'))
      .rejects.toThrow('Unsupported locale')

    expect(httpClient.get).not.toHaveBeenCalled()
  })
})

describe('safe API error normalization', () => {
  it('preserves only safe structured backend fields', () => {
    const normalized = normalizeApiError({
      response: {
        status: 404,
        data: {
          ...translationUnavailableError,
          internalException: 'sensitive',
          stackTrace: 'sensitive'
        }
      },
      config: {
        headers: {
          Authorization: 'sensitive'
        }
      }
    })

    expect(normalized).toEqual({
      status: 404,
      code: 'TRANSLATION_UNAVAILABLE',
      message: translationUnavailableError.message,
      path: translationUnavailableError.path,
      fields: [],
      availableLocales: ['fa'],
      alternatePaths: ['/fa/pages/about']
    })

    expect(normalized).not.toHaveProperty('internalException')
    expect(normalized).not.toHaveProperty('stackTrace')
    expect(normalized).not.toHaveProperty('config')
  })

  it('normalizes network failures without leaking request details', () => {
    const normalized = normalizeApiError({
      message: 'connect ECONNREFUSED',
      config: {
        baseURL: 'http://private-host:8080',
        headers: {
          Cookie: 'SESSION=secret'
        }
      }
    })

    expect(normalized).toEqual({
      status: null,
      code: 'NETWORK_ERROR',
      message: 'Unable to reach the service.',
      path: null,
      fields: [],
      availableLocales: [],
      alternatePaths: []
    })
  })
})

describe('public page introduction contract', () => {
  it('renders the CMS-owned Home title and summary', async () => {
    const wrapper = await mountPublicPage(PublicHomePage, {
      props: { initialData: homeResponse }
    })

    expectPageDoesNotOwnShellLandmarks(wrapper)
    expect(wrapper.findAll('h1')).toHaveLength(1)
    expect(wrapper.get('h1').text()).toBe(homeResponse.page.title)
    expect(wrapper.get('.public-home__summary').text()).toBe(homeResponse.page.summary)
    wrapper.unmount()
  })

  it('adds the visual Home collections only for published API data', async () => {
    const wrapper = await mountPublicPage(PublicHomePage, {
      props: {
        initialData: {
          ...homeResponse,
          latestPosts: [{
            slug: 'published-note',
            title: 'Published note',
            excerpt: 'An API-backed excerpt.',
            canonicalPath: '/fa/blog/published-note'
          }],
          selectedProjects: [{
            slug: 'published-project',
            title: 'Published project',
            summary: 'An API-backed project summary.',
            canonicalPath: '/fa/portfolio/published-project'
          }],
          selectedPublications: [{
            slug: 'published-paper',
            title: 'Published paper',
            abstractText: 'An API-backed abstract.',
            year: 2026,
            stage: 'PUBLISHED'
          }]
        }
      }
    })

    expect(wrapper.findAll('.public-home__collection')).toHaveLength(3)
    expect(wrapper.get('.public-home__hero')).toBeTruthy()
    expect(wrapper.get('.public-home__publication-item').text()).toContain('Published paper')
    expect(wrapper.findAll('h1')).toHaveLength(1)
    wrapper.unmount()
  })

  it('renders exactly one H1 for each supported Home ownership state', async () => {
    const scenarios = [
      {
        title: 'Legacy managed home',
        summary: 'Managed summary.',
        bodyMarkdown: 'Managed **body**.',
        blocks: [],
        expectedTitle: 'Legacy managed home'
      },
      {
        title: 'Title-only managed home',
        summary: '',
        bodyMarkdown: '',
        blocks: [],
        expectedTitle: 'Title-only managed home'
      },
      {
        title: 'Managed page title',
        summary: '',
        bodyMarkdown: '',
        blocks: [{
          id: 'primary-hero',
          type: 'HERO',
          enabled: true,
          title: 'Managed hero title'
        }],
        expectedTitle: 'Managed hero title'
      },
      {
        title: 'Fallback after disabled hero',
        summary: '',
        bodyMarkdown: '',
        blocks: [{
          id: 'disabled-hero',
          type: 'HERO',
          enabled: false,
          title: 'Hidden hero title'
        }],
        expectedTitle: 'Fallback after disabled hero'
      },
      {
        title: 'Fallback after empty hero',
        summary: '',
        bodyMarkdown: '',
        blocks: [{
          id: 'empty-hero',
          type: 'HERO',
          enabled: true,
          title: '',
          lead: 'Lead without a heading.'
        }],
        expectedTitle: 'Fallback after empty hero'
      }
    ]

    for (const scenario of scenarios) {
      const wrapper = await mountPublicPage(PublicHomePage, {
        props: {
          initialData: {
            page: {
              title: scenario.title,
              summary: scenario.summary,
              bodyMarkdown: scenario.bodyMarkdown,
              blocks: scenario.blocks
            },
            latestPosts: [],
            selectedProjects: [],
            selectedPublications: [],
            skills: { items: [] },
            socialLinks: { items: [] }
          }
        }
      })

      expect(wrapper.findAll('h1')).toHaveLength(1)
      expect(wrapper.get('h1').text()).toBe(scenario.expectedTitle)
      wrapper.unmount()
    }
  })

  it('renders approved hero media from the existing composer contract with localized alt text', async () => {
    const wrapper = await mountPageBlockRenderer({
      locale: 'en',
      heroHeadingLevel: 1,
      blocks: [{
        id: 'home-hero',
        type: 'HERO',
        enabled: true,
        mediaId: '550e8400-e29b-41d4-a716-446655440001',
        title: 'Taha Mohamadi',
        lead: 'Research, design, and reliable systems.',
        alt: 'Portrait of Taha Mohamadi'
      }]
    })

    expect(wrapper.findAll('h1')).toHaveLength(1)
    expect(wrapper.get('h1').text()).toBe('Taha Mohamadi')
    const image = wrapper.get('.page-block__hero-media img')

    expect(image.attributes('src'))
      .toBe('/api/v1/public/media/550e8400-e29b-41d4-a716-446655440001')
    expect(image.attributes('alt')).toBe('Portrait of Taha Mohamadi')
    expect(image.attributes('width')).toBe('1600')
    expect(image.attributes('height')).toBe('900')
    expect(image.attributes('fetchpriority')).toBe('high')
    wrapper.unmount()
  })

  it('does not publish hero media without approved alt text', async () => {
    const wrapper = await mountPageBlockRenderer({
      locale: 'fa',
      heroHeadingLevel: 1,
      blocks: [{
        id: 'home-hero-without-alt',
        type: 'HERO',
        enabled: true,
        mediaUrl: '/api/v1/public/media/550e8400-e29b-41d4-a716-446655440001',
        title: '\u0637\u0647 \u0645\u062d\u0645\u062f\u06cc',
        alt: ''
      }]
    })

    expect(wrapper.findAll('h1')).toHaveLength(1)
    expect(wrapper.find('.page-block__hero-media img').exists()).toBe(false)
    wrapper.unmount()
  })

  it('renders approved hero media when the API provides a direct mediaUrl', async () => {
    const wrapper = await mountPageBlockRenderer({
      locale: 'en',
      heroHeadingLevel: 1,
      blocks: [{
        id: 'home-hero-media-url',
        type: 'HERO',
        enabled: true,
        mediaUrl: '/api/v1/public/media/approved-home-hero',
        title: 'Taha Mohamadi',
        alt: 'Taha Mohamadi working at a desk'
      }]
    })

    const image = wrapper.get('.page-block__hero-media img')

    expect(image.attributes('src')).toBe('/api/v1/public/media/approved-home-hero')
    expect(image.attributes('alt')).toBe('Taha Mohamadi working at a desk')
    wrapper.unmount()
  })

  it('omits empty collection sections instead of rendering placeholders', async () => {
    const wrapper = await mountPageBlockRenderer({
      locale: 'en',
      blocks: [
        {
          id: 'empty-selected-work',
          type: 'COLLECTION',
          enabled: true,
          source: 'PORTFOLIO',
          limit: 3,
          title: 'Selected work',
          lead: 'This should not appear without published items.'
        },
        {
          id: 'latest-writing',
          type: 'COLLECTION',
          enabled: true,
          source: 'BLOG',
          limit: 1,
          title: 'Latest writing'
        }
      ],
      collectionItems: {
        BLOG: [{
          slug: 'systems-for-people',
          title: 'Systems for people',
          excerpt: 'A short note.'
        }],
        PORTFOLIO: []
      }
    })

    expect(wrapper.text()).not.toContain('Selected work')
    expect(wrapper.text()).not.toContain('This should not appear')
    expect(wrapper.get('.page-block--collection').text()).toContain('Latest writing')
    expect(wrapper.get('.page-block__collection-link').attributes('href'))
      .toBe('/en/blog/systems-for-people')
    wrapper.unmount()
  })

  it('keeps renderer CTAs limited to locale-owned paths or secure HTTPS URLs', async () => {
    const wrapper = await mountPageBlockRenderer({
      locale: 'en',
      blocks: [
        {
          id: 'internal-cta',
          type: 'CALL_TO_ACTION',
          enabled: true,
          title: 'Internal CTA',
          actionLabel: 'Read writing',
          actionPath: '/en/blog'
        },
        {
          id: 'external-cta',
          type: 'CALL_TO_ACTION',
          enabled: true,
          title: 'External CTA',
          actionLabel: 'Visit profile',
          actionPath: 'https://example.com/profile'
        },
        {
          id: 'unsafe-cta',
          type: 'CALL_TO_ACTION',
          enabled: true,
          title: 'Unsafe CTA',
          actionLabel: 'Do not render',
          actionPath: 'javascript:alert(1)'
        }
      ]
    })

    const internal = wrapper.get('a[href="/en/blog"]')
    const external = wrapper.get('a[href="https://example.com/profile"]')

    expect(internal.attributes('target')).toBeUndefined()
    expect(external.attributes('target')).toBe('_blank')
    expect(external.attributes('rel')).toContain('noopener')
    expect(external.attributes('rel')).toContain('noreferrer')
    expect(wrapper.text()).not.toContain('Do not render')
    expect(wrapper.html()).not.toContain('javascript:alert')
    wrapper.unmount()
  })

  it('uses only safe canonical collection paths before falling back to the current locale slug route', async () => {
    const wrapper = await mountPageBlockRenderer({
      locale: 'fa',
      blocks: [{
        id: 'featured-publications',
        type: 'COLLECTION',
        enabled: true,
        source: 'PUBLICATIONS',
        title: 'Featured publications'
      }],
      collectionItems: {
        PUBLICATIONS: [
          {
            slug: 'safe-paper',
            title: 'Safe paper',
            canonicalPath: '/fa/publications/safe-paper'
          },
          {
            slug: 'fallback-paper',
            title: 'Fallback paper',
            canonicalPath: 'https://untrusted.example/fa/publications/fallback-paper'
          },
          {
            slug: 'cross-locale-paper',
            title: 'Cross-locale paper',
            canonicalPath: '/en/publications/cross-locale-paper'
          }
        ]
      }
    })

    const links = wrapper.findAll('.page-block__collection-link')

    expect(links).toHaveLength(3)
    expect(links[0].attributes('href')).toBe('/fa/publications/safe-paper')
    expect(links[1].attributes('href')).toBe('/fa/publications/fallback-paper')
    expect(links[2].attributes('href')).toBe('/fa/publications/cross-locale-paper')
    wrapper.unmount()
  })

  it('keeps Home CMS-first, escaped, token-driven, and outside shell ownership', () => {
    const homeSource = readProjectFile(HOME_PAGE_PATH)

    expect(homeSource).toMatch(/PageBlockRenderer/)
    expect(homeSource).toMatch(/page\.value\?\.blocks/)
    expect(homeSource).toMatch(/:hero-heading-level="1"/)
    expect(homeSource).toMatch(/MarkdownContent/)
    expect(homeSource).toMatch(/getHome\s*\(/)
    expect(homeSource).not.toMatch(/t\(['"](?:shell\.siteName|public\.home)/)
    expect(homeSource).not.toMatch(/public\.placeholder|Public profile/i)

    for (const source of [homeSource]) {
      expect(source).not.toMatch(/<main\b|<q-page\b/i)
      expect(source).not.toMatch(/\b(?:lang|dir)\s*=/)
      expect(source).not.toMatch(/v-html|innerHTML|outerHTML|insertAdjacentHTML/i)
      expect(source).not.toMatch(/#[0-9a-f]{3,8}\b/i)
    }

    expect(homeSource).not.toMatch(/markdown-it|isomorphic-dompurify|sanitizer/i)

    expect(existsSync(resolve(
      projectRoot,
      'frontend/src/components/public/PageIntro.vue'
    ))).toBe(false)
  })
})
