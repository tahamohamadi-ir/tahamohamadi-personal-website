import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { Quasar } from 'quasar'
import { createSSRApp, h } from 'vue'
import { renderToString } from '@vue/server-renderer'
import { createMemoryHistory, createRouter, RouterView } from 'vue-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { i18n } from 'src/boot/i18n'
import { PUBLIC_API_KEY } from 'src/services/apiContext'

const workingDirectory = process.cwd()
const projectRoot = (
  workingDirectory.split(/[\\/]/).pop().toLowerCase() === 'frontend'
)
  ? resolve(workingDirectory, '..')
  : workingDirectory

const DETAILS = [
  {
    name: 'blog-detail',
    routePath: 'blog/:slug',
    pagePath: 'frontend/src/pages/public/BlogPostPage.vue',
    apiMethod: 'getPost',
    stateType: 'post',
    response: {
      locale: 'en',
      availableLocales: ['en', 'fa'],
      canonicalPath: '/en/posts/evidence-first',
      hreflang: [
        { locale: 'en', path: '/en/posts/evidence-first' },
        { locale: 'fa', path: '/fa/posts/شواهد-نخست' }
      ],
      seo: { title: 'Evidence first', description: 'A safe detail.' },
      ogMedia: null,
      lastModified: '2026-07-19T10:00:00Z',
      slug: 'evidence-first',
      title: 'Evidence first',
      excerpt: 'Plain-text <strong>excerpt</strong>.',
      bodyMarkdown: '## Evidence\n\nSafe **Markdown** content.',
      publishedAt: '2026-07-18T10:00:00Z'
    },
    requiredText: ['Plain-text <strong>excerpt</strong>.']
  },
  {
    name: 'portfolio-detail',
    routePath: 'portfolio/:slug',
    pagePath: 'frontend/src/pages/public/PortfolioProjectPage.vue',
    apiMethod: 'getProject',
    stateType: 'project',
    response: {
      locale: 'en',
      availableLocales: ['en', 'fa'],
      canonicalPath: '/en/portfolio/safe-project',
      hreflang: [
        { locale: 'en', path: '/en/portfolio/safe-project' },
        { locale: 'fa', path: '/fa/portfolio/پروژه-ایمن' }
      ],
      seo: { title: 'Safe project', description: 'A safe project detail.' },
      ogMedia: null,
      lastModified: '2026-07-19T10:00:00Z',
      slug: 'safe-project',
      title: 'Safe project',
      summary: 'Plain-text <em>project summary</em>.',
      bodyMarkdown: '## Project notes\n\nSafe **Markdown** content.'
    },
    requiredText: ['Plain-text <em>project summary</em>.']
  },
  {
    name: 'publication-detail',
    routePath: 'publications/:slug',
    pagePath: 'frontend/src/pages/public/PublicationDetailPage.vue',
    apiMethod: 'getPublication',
    stateType: 'publication',
    response: {
      locale: 'en',
      availableLocales: ['en', 'fa'],
      canonicalPath: '/en/publications/safe-paper',
      hreflang: [
        { locale: 'en', path: '/en/publications/safe-paper' },
        { locale: 'fa', path: '/fa/publications/مقاله-ایمن' }
      ],
      seo: { title: 'Safe paper', description: 'A safe publication detail.' },
      ogMedia: null,
      lastModified: '2026-07-19T10:00:00Z',
      slug: 'safe-paper',
      title: 'Safe paper',
      abstractText: 'Plain-text <script>unsafe</script> abstract.',
      authorsDisplay: 'Taha Mohamadi',
      venueDisplay: 'Journal of Careful Engineering',
      stage: 'PUBLISHED',
      year: 2026,
      publishedOn: '2026-07-18',
      doi: '10.1000/safe.paper',
      externalUrl: 'https://example.test/publications/safe-paper'
    },
    requiredText: ['Plain-text <script>unsafe</script> abstract.']
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

function findDetailRoute(routes, locale, detail) {
  return routes.find((route) => route.path === `/${locale}`)?.children
    .find((route) => route.name === `${locale}-${detail.name}`)
}

function localizedResponse(detail, locale) {
  return {
    ...detail.response,
    locale,
    title: locale === 'fa' ? `عنوان ${detail.name}` : detail.response.title
  }
}

async function mountDetail(detail, component, api, {
  locale = 'en',
  props = {},
  apiKey = PUBLIC_API_KEY
} = {}) {
  i18n.global.locale.value = locale
  const slug = locale === 'fa' ? `fa-${detail.response.slug}` : detail.response.slug
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{
      path: `/${locale}/${detail.routePath}`,
      component: { template: '<div />' },
      meta: { locale, direction: locale === 'fa' ? 'rtl' : 'ltr' }
    }]
  })
  const target = `/${locale}/${detail.routePath.replace(':slug', slug)}`

  await router.push(target)
  await router.isReady()

  return mount(component, {
    props,
    global: {
      plugins: [Quasar, createPinia(), router, i18n],
      provide: { [apiKey]: api }
    }
  })
}

function createSsrApplication(detail, component, api, pinia = createPinia()) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', redirect: '/en/detail/initial' },
      {
        path: '/:locale/:resource/:slug',
        component,
        meta: { locale: 'en', direction: 'ltr' }
      }
    ]
  })
  const app = createSSRApp({ render: () => h(RouterView) })

  app.use(pinia)
  app.use(router)
  app.use(i18n)
  app.provide(PUBLIC_API_KEY, api)

  return { app, pinia, router }
}

async function renderDetail(detail, component, api, locale = 'en') {
  i18n.global.locale.value = locale
  const { app, pinia, router } = createSsrApplication(detail, component, api)
  const path = `/${locale}/${detail.routePath.replace(':slug', detail.response.slug)}`

  await router.push(path)
  await router.isReady()

  return {
    html: await renderToString(app),
    state: JSON.parse(JSON.stringify(pinia.state.value)),
    path
  }
}

describe('localized public detail page contract', () => {
  it('assigns all supported detail routes to their dedicated pages in both locales', async () => {
    const { default: routes } = await import('src/router/routes')

    for (const locale of ['en', 'fa']) {
      for (const detail of DETAILS) {
        const Page = await loadComponent(detail.pagePath)
        const route = findDetailRoute(routes, locale, detail)
        const routeComponent = await route.component()

        expect(route).toMatchObject({
          path: detail.routePath,
          name: `${locale}-${detail.name}`,
          meta: { locale, pageKey: detail.name, contentType: detail.stateType }
        })
        expect(routeComponent.default ?? routeComponent).toBe(Page)
      }
    }
  })

  it('forwards locale and slug to the actual API detail method and renders only evidenced fields', async () => {
    for (const locale of ['en', 'fa']) {
      for (const detail of DETAILS) {
        const Page = await loadComponent(detail.pagePath)
        const response = localizedResponse(detail, locale)
        const api = { [detail.apiMethod]: vi.fn().mockResolvedValue(response) }
        const wrapper = await mountDetail(detail, Page, api, { locale })

        await flushPromises()

        expect(api[detail.apiMethod]).toHaveBeenCalledWith(
          locale,
          locale === 'fa' ? `fa-${detail.response.slug}` : detail.response.slug
        )
        expect(wrapper.findAll('h1')).toHaveLength(1)
        expect(wrapper.get('h1').text()).toBe(response.title)
        expect(wrapper.findAll('main, .q-page')).toHaveLength(0)
        expect(wrapper.findAll('[lang], [dir]')).toHaveLength(0)
        expect(wrapper.text()).toContain(response.title)
        expect(wrapper.html()).not.toContain('<strong>excerpt</strong>')
        expect(wrapper.html()).not.toContain('<em>project summary</em>')
        expect(wrapper.html()).not.toContain('<script>unsafe</script>')

        for (const text of detail.requiredText) {
          expect(wrapper.text()).toContain(text)
        }

        if (detail.name === 'publication-detail') {
          expect(wrapper.get(`a[href="${response.externalUrl}"]`).exists())
            .toBe(true)
          expect(wrapper.text()).toContain(response.doi)
          expect(wrapper.html()).not.toContain('tm-rich-content')
        }
        else {
          expect(wrapper.find('.tm-rich-content').html()).toContain('<h2>')
        }

        wrapper.unmount()
      }
    }
  })

  it('uses canonical page states, retry, untranslated recovery, and a safe Markdown failure slot', async () => {
    const blog = DETAILS[0]
    const BlogPostPage = await loadComponent(blog.pagePath)
    const loading = await mountDetail(blog, BlogPostPage, {
      getPost: vi.fn(() => new Promise(() => {}))
    })

    expect(loading.get('[role="status"]').text()).toMatch(/loading/i)
    expect(loading.findAll('h1')).toHaveLength(1)
    loading.unmount()

    const retryApi = {
      getPost: vi.fn()
        .mockRejectedValueOnce({ response: { status: 503, data: { code: 'SERVICE_UNAVAILABLE' } } })
        .mockResolvedValueOnce(blog.response)
    }
    const retry = await mountDetail(blog, BlogPostPage, retryApi)
    await flushPromises()
    expect(retry.get('[role="alert"]').text()).toContain(
      i18n.global.t('pageState.recoverableFailure')
    )
    await retry.get('button').trigger('click')
    await flushPromises()
    expect(retryApi.getPost).toHaveBeenCalledTimes(2)
    expect(retry.text()).toContain(blog.response.title)
    retry.unmount()

    const unavailable = await mountDetail(blog, BlogPostPage, {
      getPost: vi.fn().mockRejectedValue({
        response: {
          status: 404,
          data: {
            code: 'TRANSLATION_UNAVAILABLE',
            alternatePaths: ['/fa/posts/شواهد-نخست']
          }
        }
      })
    })
    await flushPromises()
    expect(unavailable.get('a').attributes('href')).toBe('/fa/posts/شواهد-نخست')
    unavailable.unmount()

    expect(readProjectFile(blog.pagePath)).toMatch(
      /<template\s+#error>/
    )
  })

  it('loads direct SSR details into bounded route state without a client hydration refetch', async () => {
    for (const detail of DETAILS) {
      const Page = await loadComponent(detail.pagePath)
      const serverApi = { [detail.apiMethod]: vi.fn().mockResolvedValue(detail.response) }
      const server = await renderDetail(detail, Page, serverApi)

      expect(serverApi[detail.apiMethod]).toHaveBeenCalledWith(
        'en', detail.response.slug
      )
      expect(server.html).toContain(detail.response.title)
      expect(Object.keys(server.state['public-route-data'].entries)).toEqual([
        `public:en:${detail.stateType}:${detail.response.slug}`
      ])
      expect(JSON.stringify(server.state)).not.toContain(detail.apiMethod)

      const clientApi = { [detail.apiMethod]: vi.fn().mockResolvedValue(detail.response) }
      const clientPinia = createPinia()
      clientPinia.state.value = JSON.parse(JSON.stringify(server.state))
      const { app, router } = createSsrApplication(detail, Page, clientApi, clientPinia)
      const container = document.createElement('div')

      try {
        container.innerHTML = server.html
        document.body.append(container)
        await router.push(server.path)
        await router.isReady()
        app.mount(container)
        await flushPromises()

        expect(clientApi[detail.apiMethod]).not.toHaveBeenCalled()
        expect(clientPinia.state.value['public-route-data'].entries).toEqual({})
      }
      finally {
        app.unmount()
        container.remove()
      }
    }
  })

  it('keeps Markdown rendering isolated to Blog and Portfolio pages, renders its safe error slot, and rejects unsafe external schemes', async () => {
    const sources = DETAILS.map((detail) => readProjectFile(detail.pagePath))

    for (const source of sources) {
      expect(source).toMatch(/inject\s*\(\s*PUBLIC_API_KEY\b/)
      expect(source).toMatch(/useAsyncPage/)
      expect(source).not.toMatch(/v-html|innerHTML|outerHTML|insertAdjacentHTML/i)
      expect(source).not.toMatch(/markdown-it|isomorphic-dompurify/i)
      expect(source).not.toMatch(/<main\b|<q-page\b/i)
      expect(source).not.toMatch(/\b(?:lang|dir)\s*=/)
    }

    expect(sources[0]).toMatch(/MarkdownContent/)
    expect(sources[1]).toMatch(/MarkdownContent/)
    expect(sources[2]).not.toMatch(/MarkdownContent/)
    expect(sources[2]).toMatch(/new URL\(/)
    expect(sources[2]).toMatch(/https:/)

    vi.resetModules()
    vi.doMock('src/components/content/safeMarkdown', () => ({
      renderSafeMarkdown: () => ({ status: 'error', html: '' })
    }))

    try {
      const { default: RenderingFailurePage } = await import(
        'src/pages/public/BlogPostPage.vue'
      )
      const { PUBLIC_API_KEY: renderingPublicApiKey } = await import(
        'src/services/apiContext'
      )
      const renderingFailure = await mountDetail(DETAILS[0], RenderingFailurePage, {
        getPost: vi.fn()
      }, {
        apiKey: renderingPublicApiKey,
        props: { initialData: DETAILS[0].response }
      })

      expect(renderingFailure.text()).toContain(
        i18n.global.t('public.richContent.renderingFailure')
      )
      expect(renderingFailure.html()).not.toContain(
        DETAILS[0].response.bodyMarkdown
      )
      renderingFailure.unmount()
    }
    finally {
      vi.doUnmock('src/components/content/safeMarkdown')
      vi.resetModules()
    }
  })
})

afterEach(() => {
  i18n.global.locale.value = 'en'
})
