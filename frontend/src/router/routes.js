const PublicLayout = () => import('layouts/PublicLayout.vue')
const AdminLayout = () => import('layouts/AdminLayout.vue')

const PublicHomePage = () =>
  import('pages/public/PublicHomePage.vue')

const PublicRoutePlaceholderPage = () =>
  import('pages/public/PublicRoutePlaceholderPage.vue')

const ResumePage = () => import('pages/public/ResumePage.vue')

const TranslationUnavailablePage = () =>
  import('pages/public/TranslationUnavailablePage.vue')

const NotFoundPage = () =>
  import('pages/public/NotFoundPage.vue')

const normalPublicRoutes = [
  {
    path: 'about',
    pageKey: 'about',
    contentType: 'page'
  },
  {
    path: 'research',
    pageKey: 'research',
    contentType: 'page'
  },
  {
    path: 'skills',
    pageKey: 'skills',
    contentType: 'skills'
  },
  {
    path: 'resume',
    pageKey: 'resume',
    contentType: 'resume'
  },
  {
    path: 'blog',
    pageKey: 'blog',
    contentType: 'post-list'
  },
  {
    path: 'blog/:slug',
    pageKey: 'blog-detail',
    contentType: 'post'
  },
  {
    path: 'portfolio',
    pageKey: 'portfolio',
    contentType: 'project-list'
  },
  {
    path: 'portfolio/:slug',
    pageKey: 'portfolio-detail',
    contentType: 'project'
  },
  {
    path: 'publications',
    pageKey: 'publications',
    contentType: 'publication-list'
  },
  {
    path: 'publications/:slug',
    pageKey: 'publication-detail',
    contentType: 'publication'
  },
  {
    path: 'contact',
    pageKey: 'contact',
    contentType: 'contact'
  }
]

function createRouteName(locale, pageKey) {
  return `${locale}-${pageKey}`
}

function createRouteMeta(
  locale,
  direction,
  pageKey,
  contentType
) {
  return {
    locale,
    direction,
    pageKey,
    contentType
  }
}

function createLocaleRoute(locale, direction) {
  const normalChildren = normalPublicRoutes.map((definition) => ({
    path: definition.path,
    name: createRouteName(locale, definition.pageKey),
    component: definition.pageKey === 'resume'
      ? ResumePage
      : PublicRoutePlaceholderPage,
    meta: createRouteMeta(
      locale,
      direction,
      definition.pageKey,
      definition.contentType
    )
  }))

  return {
    path: `/${locale}`,
    name: `${locale}-root`,
    component: PublicLayout,
    meta: {
      locale,
      direction
    },
    children: [
      {
        path: '',
        name: `${locale}-home`,
        component: PublicHomePage,
        meta: createRouteMeta(
          locale,
          direction,
          'home',
          'home'
        )
      },
      ...normalChildren,
      {
        path: 'translation-unavailable',
        name: `${locale}-translation-unavailable`,
        component: TranslationUnavailablePage,
        meta: createRouteMeta(
          locale,
          direction,
          'translation-unavailable',
          'error'
        )
      },
      {
        path: ':pathMatch(.*)*',
        name: `${locale}-not-found`,
        component: NotFoundPage,
        meta: createRouteMeta(
          locale,
          direction,
          'not-found',
          'error'
        )
      }
    ]
  }
}

const routes = [
  {
    path: '/',
    redirect: '/language'
  },
  {
    path: '/language',
    name: 'language',
    component: () => import('pages/LanguagePage.vue')
  },
  createLocaleRoute('fa', 'rtl'),
  createLocaleRoute('en', 'ltr'),
  {
    path: '/admin',
    name: 'admin-root',
    component: AdminLayout,
    children: [
      {
        path: '',
        name: 'admin-home',
        component: () =>
          import('pages/admin/AdminHomePage.vue')
      }
    ]
  }
]

export default routes
