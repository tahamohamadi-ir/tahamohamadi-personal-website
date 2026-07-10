# ADR-002: Frontend Framework

## Status

Accepted for MVP.

## Context

The frontend must support bilingual public pages, Persian RTL, English LTR, admin workflows, and SEO-readable public rendering. The project preference is Vue with Quasar and Pinia.

## Options

| Option | Fit | Notes |
|---|---|---|
| Quasar SSR | High | One Vue-based stack for public SSR and admin UI |
| Nuxt | Medium/High | Strong content SEO DX, but creates a second stack if admin remains Quasar |
| React | Medium | Viable, but not the preferred ecosystem for this project |

## Decision

Use Vue + Quasar + Pinia, with SSR/hybrid rendering for public pages and CSR acceptable for admin pages.

## Consequences

- One frontend stack can serve public and admin needs.
- Public pages must be validated for SSR, metadata, hreflang, and semantic HTML.
- Admin can prioritize usability without SEO requirements.

## Risks

Quasar SSR setup and deployment must be tested early.

## Follow-Up

If public SEO/content complexity becomes too high, reassess whether public pages should move to Nuxt in a later ADR.
