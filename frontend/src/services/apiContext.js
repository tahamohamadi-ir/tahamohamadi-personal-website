export const HTTP_CLIENT_KEY = Symbol('http-client')
export const PUBLIC_API_KEY = Symbol('public-api')

function notImplemented(operation) {
  throw new Error(`NOT_IMPLEMENTED:${operation}`)
}

export function resolveApiClientOptions(_context = {}) {
  return notImplemented('resolveApiClientOptions')
}

export function createRequestApiContext(_context = {}) {
  return notImplemented('createRequestApiContext')
}

export function installApiContext(_context = {}) {
  return notImplemented('installApiContext')
}
