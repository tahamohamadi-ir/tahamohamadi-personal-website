import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { describe, expect, it, vi } from 'vitest'

import {
  createRequestApiContext,
  HTTP_CLIENT_KEY,
  installApiContext,
  PUBLIC_API_KEY,
  resolveApiClientOptions
} from 'src/services/apiContext'

const trustedEnvironment = {
  TAHA_BACKEND_ORIGIN: 'http://backend.internal:8080/'
}

function createSsrContext(cookie) {
  return {
    req: {
      headers: cookie === undefined
        ? {}
        : { cookie }
    },
    res: {}
  }
}

describe('Quasar API boot contract', () => {
  it('registers the API boot file in Quasar configuration', () => {
    const configSource = readFileSync(
      resolve(process.cwd(), 'quasar.config.js'),
      'utf8'
    )

    expect(configSource).toContain("boot: ['i18n', 'api']")
    expect(
      readFileSync(resolve(process.cwd(), 'src/stores/index.js'), 'utf8')
    ).toMatch(/createPinia/)
  })

  it('recognizes server execution even when Quasar does not expose an SSR request context', () => {
    const bootSource = readFileSync(
      resolve(process.cwd(), 'src/boot/api.js'),
      'utf8'
    )

    expect(bootSource).toMatch(/typeof\s+window\s*===\s*['"]undefined['"]/)
    expect(bootSource).not.toMatch(/Boolean\(process\.env\.SERVER\)/)
  })

  it('uses same-origin relative URLs in the browser', () => {
    expect(
      resolveApiClientOptions({
        isServer: false,
        env: {},
        ssrContext: null
      })
    ).toEqual({})
  })

  it('requires an explicit trusted backend origin in SSR', () => {
    expect(() => {
      resolveApiClientOptions({
        isServer: true,
        env: {},
        ssrContext: createSsrContext()
      })
    }).toThrow('TAHA_BACKEND_ORIGIN')
  })

  it('rejects non-HTTP backend origins in SSR', () => {
    expect(() => {
      resolveApiClientOptions({
        isServer: true,
        env: {
          TAHA_BACKEND_ORIGIN: 'ftp://private-host'
        },
        ssrContext: createSsrContext()
      })
    }).toThrow('http')
  })

  it('uses the trusted SSR origin and forwards only Cookie', () => {
    const options = resolveApiClientOptions({
      isServer: true,
      env: trustedEnvironment,
      ssrContext: createSsrContext(
        'XSRF-TOKEN=token-a; SESSION=session-a'
      )
    })

    expect(options).toEqual({
      baseURL: 'http://backend.internal:8080',
      cookieHeader:
        'XSRF-TOKEN=token-a; SESSION=session-a'
    })

    expect(options).not.toHaveProperty('authorization')
    expect(options).not.toHaveProperty('host')
    expect(options).not.toHaveProperty('forwarded')
  })

  it('creates isolated API contexts for separate SSR requests', () => {
    const first = createRequestApiContext({
      isServer: true,
      env: trustedEnvironment,
      ssrContext: createSsrContext(
        'SESSION=first-session'
      )
    })

    const second = createRequestApiContext({
      isServer: true,
      env: trustedEnvironment,
      ssrContext: createSsrContext(
        'SESSION=second-session'
      )
    })

    expect(first).not.toBe(second)
    expect(first.httpClient).not.toBe(second.httpClient)
    expect(first.publicApi).not.toBe(second.publicApi)

    expect(first.httpClient.defaults.headers.common.Cookie)
      .toBe('SESSION=first-session')

    expect(second.httpClient.defaults.headers.common.Cookie)
      .toBe('SESSION=second-session')
  })

  it('provides request-scoped services through the Vue app', () => {
    const app = {
      provide: vi.fn()
    }

    const context = installApiContext({
      app,
      isServer: true,
      env: trustedEnvironment,
      ssrContext: createSsrContext(
        'SESSION=request-session'
      )
    })

    expect(app.provide).toHaveBeenCalledTimes(2)

    expect(app.provide)
      .toHaveBeenNthCalledWith(
        1,
        HTTP_CLIENT_KEY,
        context.httpClient
      )

    expect(app.provide)
      .toHaveBeenNthCalledWith(
        2,
        PUBLIC_API_KEY,
        context.publicApi
      )
  })
})
