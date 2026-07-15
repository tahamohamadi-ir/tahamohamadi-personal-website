# Vue, Quasar, routing, and state

Load for Vue SFCs, layouts, pages, composables, routing, Pinia, or vue-i18n work.

## Conventions

- Use Vue 3 Composition API and `<script setup>` by default. Retain Options API only where an existing file requires it.
- Use repository aliases such as `src/...`, `layouts/...`, and `pages/...`; never assume a nonconfigured alias such as `components/...`.
- Keep composables small. Keep page-local state local; use Pinia only when state is shared across routes/components.
- Prefer semantic HTML when a Quasar convenience component would compromise document semantics.
- Do not hardcode CMS-managed facts or silently convert API errors into invented content.

## Route and landmark ownership

- `frontend/src/router/routes.js` models only `fa` and `en`, carrying locale/direction metadata to child routes. Preserve `/language`, both locale roots, the translation-unavailable route, and localized catch-all behavior.
- `PublicLayout.vue` owns the sole `<main id="main-content" tabindex="-1">` and its router view. A child rendered there must not create `main` or `q-page`.
- `LanguagePage` is outside `PublicLayout` and may own its main landmark.
- Keep `lang`/`dir` correct: `fa`/`rtl`, `en`/`ltr`. Keep route-change focus at the public main content.

## Localization

- Treat Persian and English as first-class. Make unavailable translations explicit instead of falling back.
- Use an API-provided `alternatePath` when switching a localized detail route. Never derive it by replacing locale or slug text.
- Keep identifiers, code, and other technical LTR fragments isolated when needed.

## Review cues

Read `frontend/test/vitest/__tests__/frontend-foundation.spec.js`, `public-routing.spec.js`, and `public-shell.spec.js` for the active route, locale, shell, landmark, token, and alternate-path contracts.