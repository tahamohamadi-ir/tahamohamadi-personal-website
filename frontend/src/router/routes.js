const routes = [
  {
    path: '/language',
    component: () => import('pages/LanguagePage.vue')
  },
  {
    path: '/fa',
    component: () => import('layouts/PublicLayout.vue'),
    meta: { locale: 'fa', direction: 'rtl' },
    children: [
      {
        path: '',
        component: () => import('pages/public/PublicHomePage.vue')
      }
    ]
  },
  {
    path: '/en',
    component: () => import('layouts/PublicLayout.vue'),
    meta: { locale: 'en', direction: 'ltr' },
    children: [
      {
        path: '',
        component: () => import('pages/public/PublicHomePage.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('layouts/AdminLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('pages/admin/AdminHomePage.vue')
      }
    ]
  }
]

export default routes
