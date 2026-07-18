import { existsSync } from 'node:fs'
import { resolve } from 'node:path'

import { describe, expect, it, vi } from 'vitest'

import { normalizeApiError } from 'src/services/httpClient'

import {
  postDetailResponse,
  translationUnavailableError
} from '../../fixtures/public-api'

const workingDirectory = process.cwd()
const projectRoot = (
  workingDirectory.split(/[\\/]/).pop().toLowerCase() === 'frontend'
)
  ? resolve(workingDirectory, '..')
  : workingDirectory

const ASYNC_PAGE_PATH = 'frontend/src/composables/useAsyncPage.js'

async function loadUseAsyncPage() {
  const filePath = resolve(projectRoot, ASYNC_PAGE_PATH)

  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${ASYNC_PAGE_PATH}`)
  }

  const module = await vi.importActual(filePath)

  if (typeof module.useAsyncPage !== 'function') {
    throw new Error(`NOT_IMPLEMENTED:${ASYNC_PAGE_PATH}`)
  }

  return module.useAsyncPage
}

function deferred() {
  let resolvePromise
  let rejectPromise

  const promise = new Promise((resolvePromiseValue, rejectPromiseValue) => {
    resolvePromise = resolvePromiseValue
    rejectPromise = rejectPromiseValue
  })

  return {
    promise,
    resolve: resolvePromise,
    reject: rejectPromise
  }
}

function createRecoverableError() {
  return {
    response: {
      status: 503,
      data: {
        code: 'SERVICE_UNAVAILABLE',
        message: 'Content service is temporarily unavailable.',
        path: '/api/v1/public/en/posts'
      }
    }
  }
}

function createTranslationError() {
  return {
    response: {
      status: 404,
      data: translationUnavailableError
    }
  }
}

async function withoutBrowserGlobals(action) {
  const globalNames = [
    'window',
    'document',
    'localStorage',
    'sessionStorage'
  ]
  const descriptors = new Map(
    globalNames.map((name) => [
      name,
      Object.getOwnPropertyDescriptor(globalThis, name)
    ])
  )

  try {
    for (const name of globalNames) {
      Object.defineProperty(globalThis, name, {
        configurable: true,
        get() {
          throw new Error(`BROWSER_GLOBAL_ACCESSED:${name}`)
        }
      })
    }

    return await action()
  }
  finally {
    for (const name of globalNames) {
      const descriptor = descriptors.get(name)

      if (descriptor) {
        Object.defineProperty(globalThis, name, descriptor)
      }
      else {
        delete globalThis[name]
      }
    }
  }
}

describe('request-isolated public page loader contract', () => {
  it('uses caller-provided request API and preserves API content unchanged', async () => {
    const useAsyncPage = await loadUseAsyncPage()
    const alternatePath = '/fa/blog/نخستین-نوشته'
    const content = {
      ...postDetailResponse,
      alternatePath
    }
    const api = {
      getPost: vi.fn().mockResolvedValue(content)
    }
    const page = useAsyncPage({
      api,
      load: (currentApi) => currentApi.getPost('en', 'first-post')
    })

    await page.load()

    expect(api.getPost).toHaveBeenCalledWith('en', 'first-post')
    expect(page.data.value).toBe(content)
    expect(page.data.value.alternatePath).toBe(alternatePath)
    expect(page.state.value).toBeNull()
    expect(page.error.value).toBeNull()
  })

  it('keeps data, errors, and operations isolated between simulated SSR requests', async () => {
    const useAsyncPage = await loadUseAsyncPage()
    const firstError = createRecoverableError()
    const firstApi = {
      getPage: vi.fn().mockRejectedValue(firstError)
    }
    const secondData = { title: 'Second request only' }
    const secondApi = {
      getPage: vi.fn().mockResolvedValue(secondData)
    }
    const firstPage = useAsyncPage({
      api: firstApi,
      load: (currentApi) => currentApi.getPage('fa', 'about')
    })
    const secondPage = useAsyncPage({
      api: secondApi,
      load: (currentApi) => currentApi.getPage('en', 'about')
    })

    await Promise.all([firstPage.load(), secondPage.load()])

    expect(firstPage).not.toBe(secondPage)
    expect(firstPage.data.value).toBeNull()
    expect(firstPage.state.value).toBe('recoverable-failure')
    expect(firstPage.error.value).toEqual(normalizeApiError(firstError))
    expect(secondPage.data.value).toBe(secondData)
    expect(secondPage.state.value).toBeNull()
    expect(secondPage.error.value).toBeNull()
  })

  it('transitions from initial loading to resolved content without a success alias', async () => {
    const useAsyncPage = await loadUseAsyncPage()
    const request = deferred()
    const content = { title: 'Loaded page' }
    const page = useAsyncPage({
      api: {},
      load: () => request.promise
    })

    const load = page.load()

    expect(page.state.value).toBe('loading')
    expect(page.data.value).toBeNull()

    request.resolve(content)
    await load

    expect(page.state.value).toBeNull()
    expect(page.data.value).toBe(content)
    expect(page.error.value).toBeNull()
  })

  it('maps caller-declared empty content to the canonical empty state', async () => {
    const useAsyncPage = await loadUseAsyncPage()
    const emptyCollection = []
    const page = useAsyncPage({
      api: {},
      isEmpty: (value) => Array.isArray(value) && value.length === 0,
      load: () => Promise.resolve(emptyCollection)
    })

    await page.load()

    expect(page.state.value).toBe('empty')
    expect(page.data.value).toBe(emptyCollection)
    expect(page.error.value).toBeNull()
  })

  it('normalizes transport failure as offline without inventing content', async () => {
    const useAsyncPage = await loadUseAsyncPage()
    const transportError = new Error('connect ECONNREFUSED')
    const page = useAsyncPage({
      api: {},
      load: () => Promise.reject(transportError)
    })

    await page.load()

    expect(page.state.value).toBe('offline')
    expect(page.data.value).toBeNull()
    expect(page.error.value).toEqual(normalizeApiError(transportError))
  })

  it('normalizes recoverable and unavailable-translation failures with existing HTTP shape', async () => {
    const useAsyncPage = await loadUseAsyncPage()
    const recoverableError = createRecoverableError()
    const translationError = createTranslationError()
    const recoverablePage = useAsyncPage({
      api: {},
      load: () => Promise.reject(recoverableError)
    })
    const translationPage = useAsyncPage({
      api: {},
      load: () => Promise.reject(translationError)
    })

    await Promise.all([recoverablePage.load(), translationPage.load()])

    expect(recoverablePage.state.value).toBe('recoverable-failure')
    expect(recoverablePage.error.value).toEqual(
      normalizeApiError(recoverableError)
    )
    expect(translationPage.state.value).toBe('translation-unavailable')
    expect(translationPage.error.value).toEqual(
      normalizeApiError(translationError)
    )
    expect(translationPage.error.value.alternatePaths)
      .toEqual(['/fa/pages/about'])
  })

  it('keeps prior content visible as stale during refresh and ignores older results', async () => {
    const useAsyncPage = await loadUseAsyncPage()
    const firstRequest = deferred()
    const secondRequest = deferred()
    const thirdRequest = deferred()
    const results = [firstRequest, secondRequest, thirdRequest]
    const page = useAsyncPage({
      api: {},
      load: () => results.shift().promise
    })

    const initialLoad = page.load()
    firstRequest.resolve({ title: 'Initial content' })
    await initialLoad

    const olderRefresh = page.refresh()
    const newerRefresh = page.refresh()

    expect(page.state.value).toBe('stale')
    expect(page.data.value).toEqual({ title: 'Initial content' })

    thirdRequest.resolve({ title: 'Newest content' })
    await newerRefresh
    secondRequest.resolve({ title: 'Older content' })
    await olderRefresh

    expect(page.state.value).toBeNull()
    expect(page.data.value).toEqual({ title: 'Newest content' })
  })

  it('uses completed SSR data without a second load and exposes serializable state only', async () => {
    const useAsyncPage = await loadUseAsyncPage()
    const initialData = {
      ...postDetailResponse,
      alternatePath: '/fa/blog/نخستین-نوشته'
    }
    const api = {
      getPost: vi.fn()
    }
    const page = useAsyncPage({
      api,
      initialData,
      load: (currentApi) => currentApi.getPost('en', 'first-post')
    })
    const renderedState = {
      data: page.data.value,
      state: page.state.value,
      error: page.error.value
    }

    expect(api.getPost).not.toHaveBeenCalled()
    expect(renderedState).toEqual({
      data: initialData,
      state: null,
      error: null
    })
    expect(JSON.parse(JSON.stringify(renderedState))).toEqual(renderedState)
  })

  it('needs neither browser globals nor request host headers to load deterministic SSR data', async () => {
    await withoutBrowserGlobals(async () => {
      const useAsyncPage = await loadUseAsyncPage()
      const api = {
        getHome: vi.fn().mockResolvedValue({ title: 'SSR-safe content' })
      }

      Object.defineProperties(api, {
        host: {
          get() {
            throw new Error('REQUEST_HOST_ACCESSED')
          }
        },
        forwarded: {
          get() {
            throw new Error('FORWARDED_HEADER_ACCESSED')
          }
        }
      })

      const page = useAsyncPage({
        api,
        load: (currentApi) => currentApi.getHome('en')
      })

      await page.load()

      expect(page.data.value).toEqual({ title: 'SSR-safe content' })
      expect(page.state.value).toBeNull()
    })
  })
})
