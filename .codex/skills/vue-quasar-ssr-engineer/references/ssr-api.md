# SSR and API boundary

Load for SSR data fetching, hydration, public SEO, HTTP clients, cookies, CSRF, or contact mutations.

## Current repository boundary

- `frontend/quasar.config.js` runs SSR and boots `i18n`, `pinia`, then `api`.
- `frontend/src/boot/api.js` installs a request-scoped API context.
- `frontend/src/services/apiContext.js` validates `TAHA_BACKEND_ORIGIN` only on the server, derives a base URL, and forwards only the current request `Cookie` header.
- `frontend/src/services/httpClient.js` creates an Axios instance per context. Browser requests use relative, same-origin URLs with credentials and the configured XSRF cookie/header names.
- `frontend/src/services/publicApi.js` is the public endpoint boundary. Validate `fa|en` before issuing a request and encode path segments.

## Rules

- Do not access `window`, `document`, storage, media queries, or time/random values while rendering SSR output. Guard browser work and run it in an appropriate client lifecycle path.
- Do not create global mutable request data or reuse an SSR HTTP client between requests.
- Forward no request headers except the request cookie needed by the server-side backend call. Never log cookies, tokens, contact payloads, or backend internals.
- Keep backend-origin configuration server-only; client bundles retain relative API URLs.
- Keep SSR and first client render structurally identical. Resolve hydration warnings rather than suppressing them.
- Require SSR-visible localized title, description, canonical, hreflang, Open Graph, semantic content, and safely serialized JSON-LD where the route needs it. Do not include draft, private, admin, archived, or untranslated-fallback URLs.
- Render Markdown/rich HTML only with an approved sanitizer. Stop and request direction if none exists; do not add unsafe `v-html`.
- Prime CSRF only for accepted mutations. Do not retry contact submission automatically; preserve recoverable user input.

## Review cues

Read `frontend/test/vitest/__tests__/api-boot.spec.js`, `public-pages.spec.js`, and `contact-form.spec.js` when changing these boundaries. They assert trusted-origin validation, request isolation, cookie forwarding, safe error normalization, client defaults, endpoint shape, and no automatic contact retry.