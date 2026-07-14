# Frontend Rules

## Controlling Plan and Scope

- M1 public frontend work is controlled by `plans/008-public-frontend-mvp.md`.
- The accepted backend baseline is `BACKEND_READY_FOR_FRONTEND`.
- Do not change backend contracts, backend implementation, migrations, or admin frontend during M1.
- The human operator runs tests, builds, browser checks, Git commands, and commits.
- Use the installed `ui-ux-pro-max` skill only for tasks that change visual design, interaction, accessibility, responsive behavior, typography, color, or the design system.

## Stack

- Vue
- Quasar
- Pinia
- Vue Router
- i18n
- SSR/hybrid rendering for public pages

## General Rules

1. Use Vue Single File Components.
2. Use Quasar components consistently.
3. Use Pinia only for shared state.
4. Keep components small and reusable.
5. Public pages must be SSR-compatible and hydration-safe.
6. Admin pages may be CSR, but admin frontend implementation is outside M1.
7. Do not hardcode CMS-managed content.
8. Use semantic HTML for public pages.
9. Add loading, empty, error, offline, translation-unavailable, and success states where applicable.
10. Support both Persian RTL and English LTR.
11. Do not silently fall back to content from another locale.
12. Preserve accepted public API contracts.

## Routing Rules

`lang` is limited to `fa|en`.

Public routes and behavior:

- `/`
- `/language`
- `/{lang}`
- `/{lang}/about`
- `/{lang}/research`
- `/{lang}/skills`
- `/{lang}/resume`
- `/{lang}/blog`
- `/{lang}/blog/:slug`
- `/{lang}/portfolio`
- `/{lang}/portfolio/:slug`
- `/{lang}/publications`
- `/{lang}/publications/:slug`
- `/{lang}/contact`
- localized translation-unavailable behavior
- localized not-found behavior

Persian routes set `lang="fa"` and `dir="rtl"`. English routes set `lang="en"` and `dir="ltr"`.

Language switching must use API-provided alternate paths. Never construct translated detail URLs by replacing locale or slug strings.

Admin routes are not implemented in M1:

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
7. Public content must have correct heading hierarchy and one H1 per route.
8. Avoid heavy or decorative-only animations in MVP.
9. Respect `prefers-reduced-motion`.
10. Use skeleton loading for content pages.
11. Keep touch targets at least 44px.
12. Avoid production placeholders for required portrait, logo, OG, project, publication, or media assets.

## SEO Rules

Public pages must support SSR-visible:

- title
- description
- canonical
- hreflang
- Open Graph
- Schema.org JSON-LD where applicable

Do not include draft, admin, private, or untranslated fallback URLs in public SEO output.
