export const homeResponse = {
  locale: 'fa',
  availableLocales: ['fa', 'en'],
  canonicalPath: '/fa',
  hreflang: [
    { locale: 'fa', path: '/fa' },
    { locale: 'en', path: '/en' }
  ],
  seo: {
    title: '\u0637\u0647\u0020\u0645\u062d\u0645\u062f\u06cc',
    description: '\u0648\u0628\u200c\u0633\u0627\u06cc\u062a\u0020\u0634\u062e\u0635\u06cc\u0020\u0637\u0647\u0020\u0645\u062d\u0645\u062f\u06cc',
    openGraph: {
      title: '\u0637\u0647\u0020\u0645\u062d\u0645\u062f\u06cc',
      description: '\u0648\u0628\u200c\u0633\u0627\u06cc\u062a\u0020\u0634\u062e\u0635\u06cc\u0020\u0637\u0647\u0020\u0645\u062d\u0645\u062f\u06cc',
      imageUrl: '/api/v1/public/media/550e8400-e29b-41d4-a716-446655440001'
    }
  },
  ogMedia: {
    url: '/api/v1/public/media/550e8400-e29b-41d4-a716-446655440001',
    altText: '\u067e\u0631\u062a\u0631\u0647\u0020\u0637\u0647\u0020\u0645\u062d\u0645\u062f\u06cc'
  },
  lastModified: '2026-07-01T00:00:00Z',
  page: {
    title: '\u0637\u0647\u0020\u0645\u062d\u0645\u062f\u06cc',
    summary: '\u067e\u0698\u0648\u0647\u0634\u06af\u0631\u0020\u0648\u0020\u062a\u0648\u0633\u0639\u0647\u200c\u062f\u0647\u0646\u062f\u0647',
    bodyMarkdown: '\u0645\u062d\u062a\u0648\u0627\u06cc\u0020\u0645\u0639\u0631\u0641\u06cc\u0020\u0635\u0641\u062d\u0647\u0020\u0627\u0635\u0644\u06cc'
  },
  featured: {
    locale: 'fa',
    availableLocales: ['fa'],
    canonicalPath: '/fa/featured',
    hreflang: [{ locale: 'fa', path: '/fa/featured' }],
    seo: {
      title: null,
      description: null
    },
    ogMedia: null,
    lastModified: null,
    slot: 'home',
    items: []
  },
  latestPosts: [],
  selectedProjects: [],
  selectedPublications: [],
  skills: {
    locale: 'fa',
    availableLocales: ['fa'],
    canonicalPath: '/fa/skills',
    hreflang: [{ locale: 'fa', path: '/fa/skills' }],
    seo: {
      title: null,
      description: null,
      openGraph: {
        title: null,
        description: null,
        imageUrl: null
      }
    },
    ogMedia: null,
    lastModified: null,
    items: [],
    page: 0,
    size: 6,
    totalElements: 0,
    totalPages: 0
  },
  socialLinks: {
    items: []
  }
}

export const postCollectionResponse = {
  locale: 'en',
  availableLocales: ['en', 'fa'],
  canonicalPath: '/en/posts',
  hreflang: [
    { locale: 'en', path: '/en/posts' },
    { locale: 'fa', path: '/fa/posts' }
  ],
  seo: {
    title: 'Blog',
    description: 'Blog posts by Taha Mohamadi',
    openGraph: {
      title: 'Blog',
      description: 'Blog posts by Taha Mohamadi',
      imageUrl: null
    }
  },
  ogMedia: null,
  lastModified: '2026-07-01T00:00:00Z',
  items: [
    {
      locale: 'en',
      availableLocales: ['en', 'fa'],
      canonicalPath: '/en/posts/first-post',
      hreflang: [
        { locale: 'en', path: '/en/posts/first-post' },
        { locale: 'fa', path: '/fa/posts/first-post' }
      ],
      seo: {
        title: 'First Post',
        description: 'An introductory post.',
        openGraph: {
          title: 'First Post',
          description: 'An introductory post.',
          imageUrl: null
        }
      },
      ogMedia: null,
      lastModified: '2026-06-15T10:00:00Z',
      slug: 'first-post',
      title: 'First Post',
      excerpt: 'An introductory post.',
      publishedAt: '2026-06-15T10:00:00Z'
    }
  ],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1
}

export const postDetailResponse = {
  locale: 'en',
  availableLocales: ['en', 'fa'],
  canonicalPath: '/en/posts/first-post',
  hreflang: [
    { locale: 'en', path: '/en/posts/first-post' },
    { locale: 'fa', path: '/fa/posts/first-post' }
  ],
  seo: {
    title: 'First Post',
    description: 'An introductory post.',
    openGraph: {
      title: 'First Post',
      description: 'An introductory post.',
      imageUrl: null
    }
  },
  ogMedia: null,
  lastModified: '2026-06-15T10:00:00Z',
  slug: 'first-post',
  title: 'First Post',
  excerpt: 'An introductory post.',
  bodyMarkdown: 'Body content here.',
  publishedAt: '2026-06-15T10:00:00Z'
}

export const projectDetailResponse = {
  locale: 'en',
  availableLocales: ['en', 'fa'],
  canonicalPath: '/en/portfolio/sample-project',
  hreflang: [
    { locale: 'en', path: '/en/portfolio/sample-project' },
    { locale: 'fa', path: '/fa/portfolio/sample-project' }
  ],
  seo: {
    title: 'Sample Project',
    description: 'A sample portfolio project.',
    openGraph: {
      title: 'Sample Project',
      description: 'A sample portfolio project.',
      imageUrl: '/api/v1/public/media/550e8400-e29b-41d4-a716-446655440002'
    }
  },
  ogMedia: {
    url: '/api/v1/public/media/550e8400-e29b-41d4-a716-446655440002',
    altText: 'Sample project screenshot'
  },
  lastModified: '2026-05-01T00:00:00Z',
  slug: 'sample-project',
  title: 'Sample Project',
  summary: 'A sample portfolio project.',
  bodyMarkdown: 'Project details'
}

export const publicationDetailResponse = {
  locale: 'en',
  availableLocales: ['en'],
  canonicalPath: '/en/publications/sample-publication',
  hreflang: [
    { locale: 'en', path: '/en/publications/sample-publication' }
  ],
  seo: {
    title: 'Sample Publication',
    description: 'A sample publication entry.',
    openGraph: {
      title: 'Sample Publication',
      description: 'A sample publication entry.',
      imageUrl: null
    }
  },
  ogMedia: null,
  lastModified: '2026-04-01T00:00:00Z',
  slug: 'sample-publication',
  title: 'Sample Publication',
  abstract: 'A sample publication entry.',
  year: 2026,
  publicationStage: 'PUBLISHED'
}

export const contactReceiptResponse = {
  id: '550e8400-e29b-41d4-a716-446655440000',
  status: 'RECEIVED',
  submittedAt: '2026-07-14T10:00:00Z'
}

export const translationUnavailableError = {
  timestamp: '2026-07-14T10:00:00Z',
  status: 404,
  code: 'TRANSLATION_UNAVAILABLE',
  message: 'Translation not available for the requested locale',
  path: '/api/v1/public/en/pages/about',
  availableLocales: ['fa'],
  alternatePaths: ['/fa/pages/about']
}

export const validationErrorResponse = {
  timestamp: '2026-07-14T10:00:00Z',
  status: 400,
  code: 'VALIDATION_ERROR',
  message: 'Request validation failed',
  path: '/api/v1/public/contact',
  fields: [
    { field: 'email', message: 'Email is required' },
    { field: 'message', message: 'Message must not be blank' }
  ]
}

export const csrfErrorResponse = {
  timestamp: '2026-07-14T10:00:00Z',
  status: 403,
  error: 'Forbidden',
  message: 'Access denied',
  path: '/api/v1/public/contact'
}

export const notFoundError = {
  timestamp: '2026-07-14T10:00:00Z',
  status: 404,
  code: 'RESOURCE_NOT_FOUND',
  message: 'Resource not found',
  path: '/api/v1/public/en/posts/nonexistent',
  fields: []
}