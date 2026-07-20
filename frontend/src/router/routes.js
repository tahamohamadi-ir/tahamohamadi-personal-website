const PublicLayout = () => import('layouts/PublicLayout.vue')
const AdminLayout = () => import('layouts/AdminLayout.vue')

const PublicHomePage = () =>
  import('pages/public/PublicHomePage.vue')

const AboutPage = () => import('pages/public/AboutPage.vue')
const ResearchPage = () => import('pages/public/ResearchPage.vue')
const SkillsPage = () => import('pages/public/SkillsPage.vue')
const ContactPage = () => import('pages/public/ContactPage.vue')

const PublicRoutePlaceholderPage = () =>
  import('pages/public/PublicRoutePlaceholderPage.vue')

const ResumePage = () => import('pages/public/ResumePage.vue')
const BlogPage = () => import('pages/public/BlogPage.vue')
const PortfolioPage = () => import('pages/public/PortfolioPage.vue')
const PublicationsPage = () => import('pages/public/PublicationsPage.vue')
const BlogPostPage = () => import('pages/public/BlogPostPage.vue')
const PortfolioProjectPage = () => import('pages/public/PortfolioProjectPage.vue')
const PublicationDetailPage = () => import('pages/public/PublicationDetailPage.vue')

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
    component: {
      about: AboutPage,
      research: ResearchPage,
      skills: SkillsPage,
      resume: ResumePage,
      blog: BlogPage,
      portfolio: PortfolioPage,
      publications: PublicationsPage,
      'blog-detail': BlogPostPage,
      'portfolio-detail': PortfolioProjectPage,
      'publication-detail': PublicationDetailPage,
      contact: ContactPage
    }[definition.pageKey] ?? PublicRoutePlaceholderPage,
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
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('pages/admin/AdminLoginPage.vue'),
    meta: {
      requiresGuest: true,
      noindex: true
    }
  },
  {
    path: '/admin',
    name: 'admin-root',
    component: AdminLayout,
    meta: {
      noindex: true
    },
    children: [
      {
        path: '',
        name: 'admin-home',
        component: () =>
          import('pages/admin/AdminHomePage.vue'),
        meta: {
          requiresAdmin: true,
          noindex: true
        }
      },
      {
        path: 'pages',
        name: 'admin-pages',
        component: () => import('pages/admin/AdminPagesPage.vue'),
        meta: {
          requiresAdmin: true,
          noindex: true
        }
      },
      {
        path: ':pathMatch(.*)*',
        name: 'admin-not-found',
        component: () => import('pages/admin/AdminNotFoundPage.vue'),
        meta: {
          requiresAdmin: true,
          noindex: true
        }
      }
    ]
  }
]

export default routes
