import axios from 'axios'

export const XSRF_COOKIE_NAME = 'XSRF-TOKEN'
export const XSRF_HEADER_NAME = 'X-XSRF-TOKEN'

const NETWORK_ERROR = Object.freeze({
  status: null,
  code: 'NETWORK_ERROR',
  message: 'Unable to reach the service.',
  path: null,
  fields: [],
  availableLocales: [],
  alternatePaths: []
})

function asString(value, fallback = null) {
  return typeof value === 'string' && value.length > 0
    ? value
    : fallback
}

function asStringArray(value) {
  if (!Array.isArray(value)) {
    return []
  }

  return value.filter((item) => typeof item === 'string')
}

function asFieldErrors(value) {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .filter((item) => (
      item &&
      typeof item === 'object' &&
      typeof item.field === 'string' &&
      typeof item.message === 'string'
    ))
    .map((item) => ({
      field: item.field,
      message: item.message
    }))
}

export function createHttpClient(options = {}) {
  const {
    baseURL,
    cookieHeader
  } = options

  const client = axios.create({
    baseURL,
    withCredentials: true,
    withXSRFToken: true,
    xsrfCookieName: XSRF_COOKIE_NAME,
    xsrfHeaderName: XSRF_HEADER_NAME
  })

  if (
    typeof cookieHeader === 'string' &&
    cookieHeader.length > 0
  ) {
    client.defaults.headers.common.Cookie = cookieHeader
  }

  return client
}

export function normalizeApiError(error) {
  const response = error?.response

  if (!response) {
    return {
      ...NETWORK_ERROR,
      fields: [],
      availableLocales: [],
      alternatePaths: []
    }
  }

  const data = (
    response.data &&
    typeof response.data === 'object'
  )
    ? response.data
    : {}

  const responseStatus = Number.isInteger(response.status)
    ? response.status
    : null

  return {
    status: responseStatus,
    code: asString(data.code, 'HTTP_ERROR'),
    message: asString(
      data.message,
      'The request could not be completed.'
    ),
    path: asString(data.path),
    fields: asFieldErrors(data.fields),
    availableLocales: asStringArray(data.availableLocales),
    alternatePaths: asStringArray(data.alternatePaths)
  }
}
