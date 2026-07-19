import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { flushPromises, mount } from '@vue/test-utils'
import { Quasar } from 'quasar'
import { createSSRApp, h } from 'vue'
import { renderToString } from '@vue/server-renderer'
import { createMemoryHistory, createRouter } from 'vue-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { i18n } from 'src/boot/i18n'
import { PUBLIC_API_KEY } from 'src/services/apiContext'

const workingDirectory = process.cwd()
const projectRoot = (
  workingDirectory.split(/[\\/]/).pop().toLowerCase() === 'frontend'
)
  ? resolve(workingDirectory, '..')
  : workingDirectory

const PAGES = [
  {
    name: 'home',
    routePath: '',
    pagePath: 'frontend/src/pages/public/PublicHomePage.vue',
    apiMethod: 'getHome',
    routeTitle: 'shell.siteName',
    response: {
      page: {
        title: 'Taha Mohamadi',
        summary: 'A factual home summary.',
        bodyMarkdown: '## Home evidence\n\nSafe **home** prose.'
      }
    }
  },
  {
    name: 'about',
    routePath: 'about',
    pagePath: 'frontend/src/pages/public/AboutPage.vue',
    apiMethod: 'getPage',
    routeTitle: 'shell.navigation.about',
    response: {
      title: 'About',
      summary: 'A factual about summary.',
      bodyMarkdown: '## About evidence\n\nSafe **about** prose.'
    }
  },
  {
    name: 'research',
    routePath: 'research',
    pagePath: 'frontend/src/pages/public/ResearchPage.vue',
    apiMethod: 'getPage',
    routeTitle: 'shell.navigation.research',
    response: {
      title: 'Research',
      summary: 'A factual research summary.',
      bodyMarkdown: '## Research evidence\n\nSafe **research** prose.'
    }
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

function findNamedRoute(routes, locale, name) {
  return routes.find((route) => route.path === `/${locale}`)?.children
    .find((route) => route.name === `${locale}-${name}`)
}

function apiFor(page, response) {
  if (page.name === 'home') {
    return { getHome: vi.fn().mockResolvedValue(response) }
  }

  return {
    getPage: vi.fn().mockResolvedValue(response)
  }
}

async function mountPage(page, component, api, {
  locale = 'en',
  props = {},
  apiKey = PUBLIC_API_KEY
} = {}) {
  i18n.global.locale.value = locale
  const routePath = `/${locale}${page.routePath ? `/${page.routePath}` : ''}`
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{
      path: routePath,
      component: { template: '<div />' },
      meta: { locale, direction: locale === 'fa' ? 'rtl' : 'ltr' }
    }]
  })

  await router.push(routePath)
  await router.isReady()

  return mount(component, {
    props,
    global: {
      plugins: [Quasar, router, i18n],
      provide: { [apiKey]: api }
    }
  })
}

async function renderPageOnServer(page, component, data, {
  locale = 'en'
} = {}) {
  i18n.global.locale.value = locale
  const routePath = `/${locale}${page.routePath ? `/${page.routePath}` : ''}`
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{
      path: routePath,
      component: { template: '<div />' },
      meta: { locale, direction: locale === 'fa' ? 'rtl' : 'ltr' }
    }]
  })

  await router.push(routePath)
  await router.isReady()

  const app = createSSRApp({
    render: () => h(component, { initialData: data })
  })
  app.use(router)
  app.use(i18n)
  app.provide(PUBLIC_API_KEY, {})

  return renderToString(app)
}

function expectedArgs(page, locale) {
  return page.name === 'home'
    ? [locale]
    : [locale, page.name]
}

function emptyResponse(page) {
  return page.name === 'home'
    ? { page: { title: '', summary: '', bodyMarkdown: '' } }
    : { title: '', summary: '', bodyMarkdown: '' }
}

afterEach(() => {
  i18n.global.locale.value = 'en'
})

describe('public rich-content route integration', () => {
  it('assigns Home, About, and Research to dedicated route-owned components in both locales', async () => {
    const { default: routes } = await import('src/router/routes')
    const PlaceholderPage = await loadComponent(
      'frontend/src/pages/public/PublicRoutePlaceholderPage.vue'
    )

    for (const locale of ['en', 'fa']) {
      for (const page of PAGES) {
        const Page = await loadComponent(page.pagePath)
        const route = findNamedRoute(routes, locale, page.name)
        const routeComponent = await route.component()

        expect(routeComponent.default ?? routeComponent).toBe(Page)
      }

      for (const name of ['skills', 'contact']) {
        const route = findNamedRoute(routes, locale, name)
        const routeComponent = await route.component()

        expect(routeComponent.default ?? routeComponent).toBe(PlaceholderPage)
      }
    }
  })

  it('loads localized request-scoped API data and renders safe Markdown without shell ownership', async () => {
    for (const locale of ['en', 'fa']) {
      for (const page of PAGES) {
        const Page = await loadComponent(page.pagePath)
        const response = page.response
        const api = apiFor(page, response)
        const wrapper = await mountPage(page, Page, api, { locale })

        await flushPromises()

        expect(api[page.apiMethod]).toHaveBeenCalledWith(
          ...expectedArgs(page, locale)
        )
        expect(wrapper.findAll('h1')).toHaveLength(1)
        expect(wrapper.get('h1').text()).toBe(i18n.global.t(page.routeTitle))
        expect(wrapper.text()).toContain(
          page.name === 'home'
            ? response.page.summary
            : response.summary
        )
        expect(wrapper.find('.tm-rich-content').html())
          .toContain('<h2>')
        expect(wrapper.find('.tm-rich-content').html())
          .toContain('<strong>')
        expect(wrapper.findAll('main, .q-page')).toHaveLength(0)
        expect(wrapper.findAll('[lang], [dir]')).toHaveLength(0)
        wrapper.unmount()
      }
    }
  })

  it('renders sanitized API-backed ready content on the server without active unsafe markup', async () => {
    for (const page of PAGES) {
      const Page = await loadComponent(page.pagePath)
      const data = structuredClone(page.response)
      const markdownTarget = page.name === 'home' ? data.page : data
      markdownTarget.bodyMarkdown += '\n\n<script>alert("unsafe")</script>'

      const html = await renderPageOnServer(page, Page, data)

      expect(html).toContain('tm-rich-content')
      expect(html).toContain('<h2>')
      expect(html).toContain('Safe')
      expect(html).not.toMatch(/<script\b/i)
      expect(html).not.toMatch(/<h1\b[^>]*>[^<]*CMS/i)
    }
  })

  it('keeps the route H1 while rendering canonical empty, retry, translation-unavailable, and Markdown-error states', async () => {
    const AboutPage = await loadComponent(
      'frontend/src/pages/public/AboutPage.vue'
    )
    const about = PAGES.find((page) => page.name === 'about')

    const emptyApi = { getPage: vi.fn() }
    const empty = await mountPage(about, AboutPage, emptyApi, {
      props: { initialData: emptyResponse(about) }
    })
    expect(emptyApi.getPage).not.toHaveBeenCalled()
    expect(empty.findAll('h1')).toHaveLength(1)
    expect(empty.get('[role="status"]').text())
      .toBe(i18n.global.t('pageState.empty'))
    empty.unmount()

    const retryApi = {
      getPage: vi.fn()
        .mockRejectedValueOnce({
          response: {
            status: 503,
            data: { code: 'SERVICE_UNAVAILABLE' }
          }
        })
        .mockResolvedValueOnce(about.response)
    }
    const retry = await mountPage(about, AboutPage, retryApi)
    await flushPromises()
    expect(retry.get('[role="alert"]').text())
      .toContain(i18n.global.t('pageState.recoverableFailure'))
    await retry.get('button').trigger('click')
    await flushPromises()
    expect(retryApi.getPage).toHaveBeenCalledTimes(2)
    expect(retry.text()).toContain(about.response.summary)
    retry.unmount()

    const unavailableApi = {
      getPage: vi.fn().mockRejectedValue({
        response: {
          status: 404,
          data: {
            code: 'TRANSLATION_UNAVAILABLE',
            alternatePaths: ['/fa/pages/about']
          }
        }
      })
    }
    const unavailable = await mountPage(about, AboutPage, unavailableApi)
    await flushPromises()
    expect(unavailable.find('a').attributes('href')).toBe('/fa/pages/about')
    expect(unavailable.text()).not.toContain(about.response.bodyMarkdown)
    unavailable.unmount()

    vi.resetModules()
    vi.doMock('src/components/content/safeMarkdown', () => ({
      renderSafeMarkdown: () => ({ status: 'error', html: '' })
    }))

    try {
      const { default: RenderingFailurePage } = await import(
        'src/pages/public/AboutPage.vue'
      )
      const { PUBLIC_API_KEY: renderingPublicApiKey } = await import(
        'src/services/apiContext'
      )
      const renderingError = await mountPage(
        about,
        RenderingFailurePage,
        { getPage: vi.fn() },
        {
          apiKey: renderingPublicApiKey,
          props: { initialData: about.response }
        }
      )

      expect(renderingError.text()).toContain(
        i18n.global.t('public.richContent.renderingFailure')
      )
      expect(renderingError.html()).not.toContain(about.response.bodyMarkdown)
      renderingError.unmount()
    }
    finally {
      vi.doUnmock('src/components/content/safeMarkdown')
      vi.resetModules()
    }
  })

  it('keeps MarkdownContent as the only HTML sink and keeps placeholder ownership narrow', () => {
    const sources = PAGES.map((page) => readProjectFile(page.pagePath))
    const routesSource = readProjectFile('frontend/src/router/routes.js')

    for (const source of sources) {
      expect(source).toMatch(/inject\s*\(\s*PUBLIC_API_KEY\b/)
      expect(source).toMatch(/useAsyncPage/)
      expect(source).toMatch(/MarkdownContent/)
      expect(source).not.toMatch(/v-html|innerHTML|outerHTML|insertAdjacentHTML/i)
      expect(source).not.toMatch(/markdown-it|isomorphic-dompurify/i)
      expect(source).not.toMatch(/<main\b|<q-page\b/i)
      expect(source).not.toMatch(/\b(?:lang|dir)\s*=/)
    }

    expect(routesSource).toMatch(/AboutPage/)
    expect(routesSource).toMatch(/ResearchPage/)
    expect(routesSource).not.toMatch(/component:\s*PublicRoutePlaceholderPage[\s\S]*pageKey:\s*'about'/)
  })
})
