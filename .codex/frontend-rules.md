# Frontend Rules

## Stack

- Vue
- Quasar
- Pinia
- Vue Router
- i18n
- SSR/Hybrid rendering for public pages

## General Rules

1. Use Vue Single File Components.
2. Use Quasar components consistently.
3. Use Pinia only for shared state.
4. Keep components small and reusable.
5. Public pages must be SSR-compatible.
6. Admin pages may be CSR.
7. Do not hardcode CMS-managed content.
8. Use semantic HTML for public pages.
9. Add loading, empty, error, and success states.
10. Support both Persian RTL and English LTR.

## Routing Rules

Public routes:

- `/language`
- `/fa`
- `/fa/about`
- `/fa/resume`
- `/fa/research`
- `/fa/publications`
- `/fa/blog`
- `/fa/blog/:slug`
- `/fa/portfolio`
- `/fa/contact`
- `/en`
- `/en/about`
- `/en/resume`
- `/en/research`
- `/en/publications`
- `/en/blog`
- `/en/blog/:slug`
- `/en/portfolio`
- `/en/contact`

Admin routes:

- `/admin`
- `/admin/posts`
- `/admin/pages`
- `/admin/media`
- `/admin/theme`
- `/admin/analytics`
- `/admin/settings`

## UI Rules

1. Mobile-first design.
2. Use accessible labels.
3. Use keyboard-friendly navigation.
4. Use alt text for images.
5. Persian pages must use `dir="rtl"`.
6. English pages must use `dir="ltr"`.
7. Public content must have correct heading hierarchy.
8. Avoid heavy animations in MVP.
9. Use skeleton loading for content pages.
10. Keep admin forms simple.

## SEO Rules

Public pages must support:

- title
- description
- canonical
- Open Graph
- Schema.org JSON-LD where needed
