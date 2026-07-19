import { createHttpClient } from './httpClient'
import { createPublicApi } from './publicApi'

export const HTTP_CLIENT_KEY = Symbol('http-client')
export const PUBLIC_API_KEY = Symbol('public-api')

const HTTP_PROTOCOLS = new Set([
  'http:',
  'https:'
])

function resolveTrustedBackendOrigin(value) {
  if (
    typeof value !== 'string' ||
    value.trim().length === 0
  ) {
    throw new Error(
      'TAHA_BACKEND_ORIGIN is required for SSR API requests.'
    )
  }

  let backendUrl

  try {
    backendUrl = new URL(value.trim())
  }
  catch {
    throw new Error(
      'TAHA_BACKEND_ORIGIN must be an absolute http or https origin.'
    )
  }

  if (!HTTP_PROTOCOLS.has(backendUrl.protocol)) {
    throw new Error(
      'TAHA_BACKEND_ORIGIN must use the http or https protocol.'
    )
  }

  if (backendUrl.username || backendUrl.password) {
    throw new Error(
      'TAHA_BACKEND_ORIGIN must not contain credentials.'
    )
  }

  if (
    backendUrl.pathname !== '/' ||
    backendUrl.search.length > 0 ||
    backendUrl.hash.length > 0
  ) {
    throw new Error(
      'TAHA_BACKEND_ORIGIN must contain only an origin.'
    )
  }

  return backendUrl.origin
}

function resolveRequestCookie(ssrContext) {
  const cookieHeader = ssrContext?.req?.headers?.cookie

  return (
    typeof cookieHeader === 'string' &&
    cookieHeader.trim().length > 0
  )
    ? cookieHeader
    : null
}

export function resolveApiClientOptions(context = {}) {
  const {
    isServer = false,
    env = {},
    ssrContext = null
  } = context

  if (!isServer) {
    return {}
  }

  const environment = (
    env &&
    typeof env === 'object'
  )
    ? env
    : {}

  const baseURL = resolveTrustedBackendOrigin(
    environment.TAHA_BACKEND_ORIGIN
  )

  const cookieHeader = resolveRequestCookie(ssrContext)

  if (cookieHeader === null) {
    return { baseURL }
  }

  return {
    baseURL,
    cookieHeader
  }
}

export function createRequestApiContext(context = {}) {
  const clientOptions = resolveApiClientOptions(context)
  const httpClient = createHttpClient(clientOptions)
  const publicApi = createPublicApi(httpClient)

  return {
    httpClient,
    publicApi
  }
}

export function installApiContext(context = {}) {
  const { app } = context

  if (!app || typeof app.provide !== 'function') {
    throw new TypeError(
      'A Vue application with provide() is required.'
    )
  }

  const requestContext = createRequestApiContext(context)

  app.provide(
    HTTP_CLIENT_KEY,
    requestContext.httpClient
  )

  app.provide(
    PUBLIC_API_KEY,
    requestContext.publicApi
  )

  return requestContext
}
