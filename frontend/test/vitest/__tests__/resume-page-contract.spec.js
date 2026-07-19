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

const RESUME_PAGE_PATH = 'frontend/src/pages/public/ResumePage.vue'
const RESUME_TIMELINE_PATH = (
  'frontend/src/components/public/ResumeTimeline.vue'
)
const IMPLEMENTED_COLLECTION_ROUTES = [
  {
    pageKey: 'blog',
    path: 'blog',
    contentType: 'post-list',
    componentPath: 'frontend/src/pages/public/BlogPage.vue'
  },
  {
    pageKey: 'portfolio',
    path: 'portfolio',
    contentType: 'project-list',
    componentPath: 'frontend/src/pages/public/PortfolioPage.vue'
  },
  {
    pageKey: 'publications',
    path: 'publications',
    contentType: 'publication-list',
    componentPath: 'frontend/src/pages/public/PublicationsPage.vue'
  }
]

const IMPLEMENTED_DETAIL_ROUTES = [
  {
    pageKey: 'blog-detail',
    componentPath: 'frontend/src/pages/public/BlogPostPage.vue'
  },
  {
    pageKey: 'portfolio-detail',
    componentPath: 'frontend/src/pages/public/PortfolioProjectPage.vue'
  },
  {
    pageKey: 'publication-detail',
    componentPath: 'frontend/src/pages/public/PublicationDetailPage.vue'
  }
]

const RESUME_RESPONSE = {
  locale: 'en',
  availableLocales: ['en'],
  canonicalPath: '/en/resume',
  hreflang: [{ locale: 'en', path: '/en/resume' }],
  seo: {
    title: null,
    description: null
  },
  ogMedia: null,
  lastModified: null,
  entries: [
    {
      title: 'Research Assistant',
      organization: 'Example University',
      location: 'Tehran',
      summary: 'Built <strong>safe</strong> research tools.',
      entryType: 'RESEARCH',
      startedOn: '2022-09-01',
      endedOn: null,
      current: true
    },
    {
      title: 'Bachelor of Engineering',
      organization: 'Example Institute',
      location: null,
      summary: null,
      entryType: 'EDUCATION',
      startedOn: '2018-09-01',
      endedOn: '2022-06-30',
      current: false
    }
  ],
  document: {
    mediaUrl: '/api/v1/public/media/resume-document',
    publishedAt: '2026-07-10T12:00:00Z'
  }
}

const PERSIAN_RESUME_RESPONSE = {
  ...RESUME_RESPONSE,
  locale: 'fa',
  availableLocales: ['fa'],
  canonicalPath: '/fa/resume',
  hreflang: [{ locale: 'fa', path: '/fa/resume' }],
  entries: [
    {
      ...RESUME_RESPONSE.entries[0],
      title: 'پژوهشگر',
      organization: 'دانشگاه نمونه',
      location: 'تهران',
      summary: 'متن رزومه'
    }
  ]
}

function readProjectFile(projectRelativePath) {
  const filePath = resolve(projectRoot, projectRelativePath)

  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${projectRelativePath}`)
  }

  return readFileSync(filePath, 'utf8')
}

async function loadContractComponent(projectRelativePath) {
  readProjectFile(projectRelativePath)

  const module = await vi.importActual(
    resolve(projectRoot, projectRelativePath)
  )

  if (!module.default) {
    throw new Error(`NOT_IMPLEMENTED:${projectRelativePath}`)
  }

  return module.default
}

async function loadResumePage() {
  readProjectFile(RESUME_PAGE_PATH)
  readProjectFile(RESUME_TIMELINE_PATH)

  return loadContractComponent(RESUME_PAGE_PATH)
}

function findLocaleRoute(routes, locale) {
  return routes.find((route) => route.path === `/${locale}`)
}

function findResumeRoute(routes, locale) {
  return findLocaleRoute(routes, locale)?.children.find(
    (route) => route.name === `${locale}-resume`
  )
}

function deferred() {
  let resolvePromise
  let rejectPromise

  const promise = new Promise((resolveValue, rejectValue) => {
    resolvePromise = resolveValue
    rejectPromise = rejectValue
  })

  return {
    promise,
    resolve: resolvePromise,
    reject: rejectPromise
  }
}

function createRouterFor(locale) {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: `/${locale}/resume`,
        name: `${locale}-resume-contract`,
        component: { template: '<div />' },
        meta: {
          locale,
          direction: locale === 'fa' ? 'rtl' : 'ltr'
        }
      }
    ]
  })
}

async function mountLocalized(component, {
  locale = 'en',
  api,
  props = {}
} = {}) {
  i18n.global.locale.value = locale
  const router = createRouterFor(locale)

  await router.push(`/${locale}/resume`)
  await router.isReady()

  return mount(component, {
    props,
    global: {
      plugins: [Quasar, router, i18n],
      provide: {
        [PUBLIC_API_KEY]: api
      }
    }
  })
}

function expectNoPageLandmarkOwnership(wrapper) {
  expect(wrapper.find('main').exists()).toBe(false)
  expect(wrapper.find('.q-page').exists()).toBe(false)
  expect(wrapper.findAll('[lang], [dir]')).toHaveLength(0)
}

function expectNoChildPageOwnership(wrapper) {
  expectNoPageLandmarkOwnership(wrapper)
  expect(wrapper.findAll('h1')).toHaveLength(0)
}

function expectResumeShape(value) {
  expect(Object.keys(value).sort()).toEqual([
    'availableLocales',
    'canonicalPath',
    'document',
    'entries',
    'hreflang',
    'lastModified',
    'locale',
    'ogMedia',
    'seo'
  ])

  for (const entry of value.entries) {
    expect(Object.keys(entry).sort()).toEqual([
      'current',
      'endedOn',
      'entryType',
      'location',
      'organization',
      'startedOn',
      'summary',
      'title'
    ])
  }

  if (value.document) {
    expect(Object.keys(value.document).sort()).toEqual([
      'mediaUrl',
      'publishedAt'
    ])
  }
}

describe('localized public Resume route contract', () => {
  it('assigns implemented public routes while preserving locale ownership', async () => {
    const ResumePage = await loadResumePage()
    const { default: routes } = await import('src/router/routes')
    const PlaceholderPage = await loadContractComponent(
      'frontend/src/pages/public/PublicRoutePlaceholderPage.vue'
    )
    const collectionPages = new Map(await Promise.all(
      IMPLEMENTED_COLLECTION_ROUTES.map(async (definition) => [
        definition.pageKey,
        await loadContractComponent(definition.componentPath)
      ])
    ))
    const richContentPages = new Map(await Promise.all(
      [
        ['about', 'frontend/src/pages/public/AboutPage.vue'],
        ['research', 'frontend/src/pages/public/ResearchPage.vue']
      ].map(async ([pageKey, componentPath]) => [
        pageKey,
        await loadContractComponent(componentPath)
      ])
    ))
    const detailPages = new Map(await Promise.all(
      IMPLEMENTED_DETAIL_ROUTES.map(async (definition) => [
        definition.pageKey,
        await loadContractComponent(definition.componentPath)
      ])
    ))
    const ownedPages = new Map(await Promise.all(
      [
        ['skills', 'frontend/src/pages/public/SkillsPage.vue'],
        ['contact', 'frontend/src/pages/public/ContactPage.vue']
      ].map(async ([pageKey, componentPath]) => [
        pageKey,
        await loadContractComponent(componentPath)
      ])
    ))

    for (const [locale, direction] of [
      ['fa', 'rtl'],
      ['en', 'ltr']
    ]) {
      const localeRoute = findLocaleRoute(routes, locale)
      const resumeRoute = findResumeRoute(routes, locale)

      expect(localeRoute?.meta).toMatchObject({ locale, direction })
      expect(resumeRoute?.path).toBe('resume')
      expect(resumeRoute?.name).toBe(`${locale}-resume`)
      expect(resumeRoute?.meta).toMatchObject({
        locale,
        direction,
        pageKey: 'resume',
        contentType: 'resume'
      })

      const routeComponent = await resumeRoute.component()
      expect(routeComponent.default ?? routeComponent).toBe(ResumePage)

      const expectedComponents = new Map([
        [`${locale}-resume`, ResumePage],
        [`${locale}-about`, richContentPages.get('about')],
        [`${locale}-research`, richContentPages.get('research')],
        [`${locale}-skills`, ownedPages.get('skills')],
        [`${locale}-contact`, ownedPages.get('contact')]
      ])

      for (const definition of IMPLEMENTED_COLLECTION_ROUTES) {
        const collectionRoute = localeRoute.children.find(
          (route) => route.name === `${locale}-${definition.pageKey}`
        )
        const CollectionPage = collectionPages.get(definition.pageKey)

        expect(collectionRoute).toMatchObject({
          path: definition.path,
          name: `${locale}-${definition.pageKey}`,
          meta: {
            locale,
            direction,
            pageKey: definition.pageKey,
            contentType: definition.contentType
          }
        })

        const collectionComponent = await collectionRoute.component()
        expect(collectionComponent.default ?? collectionComponent)
          .toBe(CollectionPage)
        expectedComponents.set(collectionRoute.name, CollectionPage)
      }

      for (const definition of IMPLEMENTED_DETAIL_ROUTES) {
        expectedComponents.set(
          `${locale}-${definition.pageKey}`,
          detailPages.get(definition.pageKey)
        )
      }

      for (const route of localeRoute.children) {
        if (
          route.name.endsWith('-home') ||
          route.name.endsWith('-not-found') ||
          route.name.endsWith('-translation-unavailable')
        ) {
          continue
        }

        const component = await route.component()
        expect(component.default ?? component).toBe(
          expectedComponents.get(route.name) ?? PlaceholderPage
        )
      }
    }
  })

  it('uses the request-scoped public API and useAsyncPage without browser or origin globals', () => {
    const source = readProjectFile(RESUME_PAGE_PATH)

    expect(source).toMatch(/inject\s*\(\s*PUBLIC_API_KEY\s*\)/)
    expect(source).toMatch(/useAsyncPage/)
    expect(source).toMatch(/getResume\s*\(\s*(?:locale|route\.meta\.locale)/)
    expect(source).not.toMatch(/axios\s*[.(]/i)
    expect(source).not.toMatch(/window|document|localStorage|sessionStorage/)
    expect(source).not.toMatch(/\bhost\b|forwarded/i)
    expect(source).not.toMatch(/v-html|markdown/i)
  })

  it('loads only active-locale Resume data, keeps accepted DTO fields intact, and exposes no request object in rendered content', async () => {
    const ResumePage = await loadResumePage()
    const api = {
      requestSecret: 'must-not-render',
      getResume: vi.fn().mockResolvedValue(RESUME_RESPONSE)
    }

    expectResumeShape(RESUME_RESPONSE)
    const wrapper = await mountLocalized(ResumePage, { api })

    await flushPromises()

    expect(api.getResume).toHaveBeenCalledTimes(1)
    expect(api.getResume).toHaveBeenCalledWith('en')
    expect(wrapper.text()).toContain('Research Assistant')
    expect(wrapper.text()).not.toContain('must-not-render')
    expect(wrapper.text()).not.toContain('storageKey')
    expect(wrapper.text()).not.toContain('sortOrder')
  })

  it('uses caller-provided empty semantics: only no entries and no document is empty', async () => {
    const ResumePage = await loadResumePage()
    const scenarios = [
      {
        response: { ...RESUME_RESPONSE, entries: [], document: null },
        expectedState: 'empty'
      },
      {
        response: { ...RESUME_RESPONSE, document: null },
        expectedState: null
      },
      {
        response: { ...RESUME_RESPONSE, entries: [] },
        expectedState: null
      }
    ]

    for (const scenario of scenarios) {
      const api = {
        getResume: vi.fn().mockResolvedValue(scenario.response)
      }
      const wrapper = await mountLocalized(ResumePage, { api })

      await flushPromises()

      if (scenario.expectedState === 'empty') {
        expect(wrapper.get('[role="status"]').text())
          .toMatch(/no|empty|available/i)
      }
      else {
        expect(wrapper.findAll('h1')).toHaveLength(1)
      }

      wrapper.unmount()
    }
  })

  it('presents loading, recoverable failure, offline, stale refresh, and SSR data without inventing Resume content', async () => {
    const ResumePage = await loadResumePage()
    const initialRequest = deferred()
    const refreshRequest = deferred()
    const api = {
      getResume: vi.fn()
        .mockReturnValueOnce(initialRequest.promise)
        .mockReturnValueOnce(refreshRequest.promise)
    }
    const wrapper = await mountLocalized(ResumePage, { api })

    expect(wrapper.get('[role="status"]').text()).toMatch(/loading/i)
    expect(wrapper.findAll('h1')).toHaveLength(0)

    initialRequest.resolve(RESUME_RESPONSE)
    await flushPromises()

    expect(wrapper.text()).toContain('Research Assistant')
    await wrapper.get('button').trigger('click')

    expect(wrapper.get('[role="status"]').text()).toMatch(/stale|outdated|refresh/i)
    expect(wrapper.text()).toContain('Research Assistant')

    refreshRequest.resolve({
      ...RESUME_RESPONSE,
      entries: [{
        ...RESUME_RESPONSE.entries[0],
        title: 'Updated Research Assistant'
      }]
    })
    await flushPromises()

    expect(wrapper.text()).toContain('Updated Research Assistant')

    const recoverableApi = {
      getResume: vi.fn().mockRejectedValue({
        response: {
          status: 503,
          data: {
            code: 'SERVICE_UNAVAILABLE',
            message: 'Try again later.'
          }
        }
      })
    }
    const recoverable = await mountLocalized(ResumePage, {
      api: recoverableApi
    })

    await flushPromises()
    expect(recoverable.get('[role="alert"]').text()).toMatch(/try|again|failed|error/i)
    await recoverable.get('button').trigger('click')
    expect(recoverableApi.getResume).toHaveBeenCalledTimes(2)

    const offlineApi = {
      getResume: vi.fn().mockRejectedValue(
        new Error('network unavailable')
      )
    }
    const offline = await mountLocalized(ResumePage, { api: offlineApi })

    await flushPromises()
    expect(offline.get('[role="status"]').text())
      .toMatch(/offline|connection|network/i)
    expect(offline.text()).not.toMatch(/no content|empty resume/i)

    const ssrApi = { getResume: vi.fn() }
    const ssr = await mountLocalized(ResumePage, {
      api: ssrApi,
      props: { initialData: RESUME_RESPONSE }
    })

    await flushPromises()
    expect(ssrApi.getResume).not.toHaveBeenCalled()
    expect(ssr.text()).toContain('Research Assistant')
  })

  it('renders localized heading and data without cross-locale fallback, extra landmarks, or invented introduction text', async () => {
    const ResumePage = await loadResumePage()

    for (const [locale, response, expectedTitle, absentTitle] of [
      ['en', RESUME_RESPONSE, 'Research Assistant', 'پژوهشگر'],
      ['fa', PERSIAN_RESUME_RESPONSE, 'پژوهشگر', 'Research Assistant']
    ]) {
      const api = { getResume: vi.fn().mockResolvedValue(response) }
      const wrapper = await mountLocalized(ResumePage, { locale, api })

      await flushPromises()

      expect(wrapper.findAll('h1')).toHaveLength(1)
      expect(wrapper.get('h1').text()).not.toHaveLength(0)
      expect(wrapper.text()).toContain(expectedTitle)
      expect(wrapper.text()).not.toContain(absentTitle)
      expectNoPageLandmarkOwnership(wrapper)
      wrapper.unmount()
    }
  })
})

describe('ResumeTimeline semantic and content-integrity contract', () => {
  it('accepts explicit entries and renders exact API order as semantic list chronology without page ownership or cards', async () => {
    const ResumeTimeline = await loadContractComponent(RESUME_TIMELINE_PATH)
    const wrapper = await mountLocalized(ResumeTimeline, {
      props: { entries: RESUME_RESPONSE.entries }
    })

    expectNoChildPageOwnership(wrapper)
    expect(wrapper.find('.q-card').exists()).toBe(false)
    expect(wrapper.findAll('ol, ul')).toHaveLength(1)

    const items = wrapper.findAll('li')
    expect(items).toHaveLength(RESUME_RESPONSE.entries.length)
    expect(items.map((item) => item.text())).toEqual([
      expect.stringContaining('Research Assistant'),
      expect.stringContaining('Bachelor of Engineering')
    ])

    expect(items[0].text()).toContain('Example University')
    expect(items[0].text()).toContain('Tehran')
    expect(items[1].text()).toContain('Example Institute')
    expect(items[1].text()).not.toContain('Tehran')
    expect(items[1].text()).not.toMatch(/Built|safe research tools/i)

    expect(items[0].text()).toContain('<strong>safe</strong>')
    expect(items[0].find('strong').exists()).toBe(false)
    expect(wrapper.html()).not.toContain('<strong>safe</strong>')
  })

  it('supports each accepted entry type in an independent response without client grouping, ranking, badges, or media invention', async () => {
    const ResumeTimeline = await loadContractComponent(RESUME_TIMELINE_PATH)

    for (const entryType of [
      'EDUCATION',
      'EXPERIENCE',
      'RESEARCH',
      'AWARD',
      'CERTIFICATION'
    ]) {
      const wrapper = await mountLocalized(ResumeTimeline, {
        props: {
          entries: [{
            ...RESUME_RESPONSE.entries[0],
            entryType
          }]
        }
      })

      expect(wrapper.findAll('li')).toHaveLength(1)
      expect(wrapper.find('img').exists()).toBe(false)
      expect(wrapper.text()).not.toMatch(/proficiency|rank|status/i)
      wrapper.unmount()
    }
  })

  it('preserves LocalDate ISO semantics, treats current as present, and never invents end dates or durations', async () => {
    const ResumeTimeline = await loadContractComponent(RESUME_TIMELINE_PATH)
    const wrapper = await mountLocalized(ResumeTimeline, {
      props: { entries: RESUME_RESPONSE.entries }
    })

    expect(wrapper.get('time[datetime="2022-09-01"]').text())
      .toContain('2022-09-01')
    expect(wrapper.find('time[datetime="2022-09-01"]').exists()).toBe(true)
    expect(wrapper.find('time[datetime="2022-09-01"] + time').exists())
      .toBe(false)
    expect(wrapper.text()).toMatch(/present|current/i)
    expect(wrapper.get('time[datetime="2022-06-30"]').text())
      .toContain('2022-06-30')
    expect(wrapper.text()).not.toMatch(/years? of experience|duration/i)
  })
})

describe('Resume document and localization contract', () => {
  it('renders one localized, visible-focus document action with unchanged API mediaUrl and no absent-document placeholder', async () => {
    const ResumePage = await loadResumePage()
    const api = { getResume: vi.fn().mockResolvedValue(RESUME_RESPONSE) }
    const wrapper = await mountLocalized(ResumePage, { api })

    await flushPromises()

    const documentActions = wrapper.findAll(
      `a[href="${RESUME_RESPONSE.document.mediaUrl}"]`
    )
    expect(documentActions).toHaveLength(1)
    expect(documentActions[0].text()).not.toHaveLength(0)
    expect(documentActions[0].attributes('aria-label')).not.toHaveLength(0)

    const source = readProjectFile(RESUME_PAGE_PATH)
    expect(source).toContain('--tm-control-min-size')
    expect(source).toMatch(/focus-visible/)
    expect(source).not.toMatch(/storageKey|mediaAssetId|fileName|fileSize/)

    const withoutDocument = await mountLocalized(ResumePage, {
      api: {
        getResume: vi.fn().mockResolvedValue({
          ...RESUME_RESPONSE,
          document: null
        })
      }
    })

    await flushPromises()
    expect(withoutDocument.findAll('a[href*="/api/v1/public/media/"]'))
      .toHaveLength(0)
  })

  it('keeps current-entry and document actions localized in Persian without component lang or dir ownership', async () => {
    const ResumePage = await loadResumePage()
    const wrapper = await mountLocalized(ResumePage, {
      locale: 'fa',
      api: {
        getResume: vi.fn().mockResolvedValue(PERSIAN_RESUME_RESPONSE)
      }
    })

    await flushPromises()

    expect(wrapper.text()).toMatch(/[\u0600-\u06ff]/)
    expect(wrapper.get('a').attributes('aria-label')).toMatch(/[\u0600-\u06ff]/)
    expectNoPageLandmarkOwnership(wrapper)
  })
})
