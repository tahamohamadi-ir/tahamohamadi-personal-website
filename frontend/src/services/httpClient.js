export const XSRF_COOKIE_NAME = 'XSRF-TOKEN'
export const XSRF_HEADER_NAME = 'X-XSRF-TOKEN'

function notImplemented(operation) {
  throw new Error(`NOT_IMPLEMENTED:${operation}`)
}

export function createHttpClient(_options = {}) {
  return notImplemented('createHttpClient')
}

export function normalizeApiError(_error) {
  return notImplemented('normalizeApiError')
}