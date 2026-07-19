import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { Quasar } from 'quasar'
import { createMemoryHistory, createRouter } from 'vue-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { i18n } from 'src/boot/i18n'
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
  it('renders a localized personal-site identity and API-owned summary for Home', async () => {
    const wrapper = await mountPublicPage(PublicHomePage, {
      props: { initialData: homeResponse }
    })

    expectPageDoesNotOwnShellLandmarks(wrapper)
    expect(wrapper.findAll('h1')).toHaveLength(1)
    expect(wrapper.get('h1').text()).toBe(i18n.global.t('shell.siteName'))
    expect(wrapper.get('.tm-page-copy').text()).toBe(homeResponse.page.summary)
  })

  it('keeps Home local, escaped, token-driven, and outside shell ownership', () => {
    const homeSource = readProjectFile(HOME_PAGE_PATH)

    expect(homeSource.match(/<h1\b/g) ?? []).toHaveLength(1)
    expect(homeSource).toMatch(/t\(['\"]shell\.siteName['\"]\)/)
    expect(homeSource).toMatch(/MarkdownContent/)
    expect(homeSource).toMatch(/getHome\s*\(/)
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
