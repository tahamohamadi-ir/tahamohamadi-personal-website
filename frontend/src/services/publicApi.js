export const PUBLIC_API_ROOT = '/api/v1/public'
export const CONTACT_ENDPOINT = `${PUBLIC_API_ROOT}/contact`
export const SOCIAL_LINKS_ENDPOINT =
  `${PUBLIC_API_ROOT}/social-links`

const SUPPORTED_LOCALES = new Set(['fa', 'en'])

function assertHttpClient(httpClient) {
  if (
    !httpClient ||
    typeof httpClient.get !== 'function' ||
    typeof httpClient.post !== 'function'
  ) {
    throw new TypeError(
      'A compatible request-scoped HTTP client is required.'
    )
  }
}

function assertLocale(locale) {
  if (!SUPPORTED_LOCALES.has(locale)) {
    throw new Error(`Unsupported locale: ${locale}`)
  }

  return locale
}

function localizedRoot(locale) {
  return `${PUBLIC_API_ROOT}/${assertLocale(locale)}`
}

function encodePathSegment(value, fieldName) {
  if (
    typeof value !== 'string' ||
    value.trim().length === 0
  ) {
    throw new TypeError(`${fieldName} must be a non-empty string.`)
  }

  return encodeURIComponent(value)
}

async function getData(httpClient, url, config) {
  const response = config === undefined
    ? await httpClient.get(url)
    : await httpClient.get(url, config)

  return response.data
}

export function createPublicApi(httpClient) {
  assertHttpClient(httpClient)

  return {
    async getHome(locale) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/home`
      )
    },

    async getPage(locale, slug) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/pages/` +
          encodePathSegment(slug, 'slug')
      )
    },

    async listPosts(locale, params) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/posts`,
        { params }
      )
    },

    async getPost(locale, slug) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/posts/` +
          encodePathSegment(slug, 'slug')
      )
    },

    async listCategories(locale) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/categories`
      )
    },

    async listTags(locale) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/tags`
      )
    },

    async listPortfolio(locale, params) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/portfolio`,
        { params }
      )
    },

    async getProject(locale, slug) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/portfolio/` +
          encodePathSegment(slug, 'slug')
      )
    },

    async getSkills(locale) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/skills`
      )
    },

    async getResume(locale) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/resume`
      )
    },

    async listPublications(locale, params) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/publications`,
        { params }
      )
    },

    async getPublication(locale, slug) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/publications/` +
          encodePathSegment(slug, 'slug')
      )
    },

    async getFeatured(locale, params) {
      return getData(
        httpClient,
        `${localizedRoot(locale)}/featured`,
        { params }
      )
    },

    async getSocialLinks() {
      return getData(
        httpClient,
        SOCIAL_LINKS_ENDPOINT
      )
    },

    async submitContact(payload) {
      const response = await httpClient.post(
        CONTACT_ENDPOINT,
        payload
      )

      return response.data
    }
  }
}
