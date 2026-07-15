---
name: vue-quasar-ssr-engineer
description: Implement, review, test, and debug scoped Vue 3 and Quasar SSR public-frontend work in TahaMohamadi.ir. Use for Vue SFCs, Quasar layouts/components, Vue Router routes, Pinia or vue-i18n changes, SSR/hydration/API-client work, frontend architecture review, Vitest contracts, and Playwright browser verification. Do not use for backend, migrations, deployment, admin frontend, or unscoped visual redesign.
---

# Vue Quasar SSR Engineer

## Overview

Deliver a small, SSR-safe public-frontend change that preserves the accepted backend contract, Persian/English parity, semantic-token design system, and accessibility baseline. Treat hydration warnings as release blockers.

## Read only what the microtask needs

1. Read `.codex/frontend-rules.md` and the relevant section of `plans/008-public-frontend-mvp.md`.
2. Read the route, layout, component, composable, service, and test files being changed.
3. Load a topic reference only when applicable:
   - [SSR and API boundary](references/ssr-api.md) for server data, cookies, hydration, SEO, or mutations.
   - [Vue, Quasar, routing, and state](references/vue-quasar-conventions.md) for SFCs, routes, layouts, Pinia, or i18n.
   - [Locale, design, and accessibility](references/locale-design-accessibility.md) for UI, interaction, responsive, RTL/LTR, or a11y work.
   - [Verification and scope](references/workflow-and-verification.md) for tests, reviews, debugging, or browser checks.

Do not inspect `node_modules`, `dist`, `.quasar`, `target`, `coverage`, or `.git`. Do not load every UI/UX document for a small task.

## Workflow

1. Restate the assigned microtask, relevant requirement IDs, and allowed files. Keep M1 changes in the frontend unless explicit scope says otherwise.
2. Add or confirm a failing behavioral/contract test. Do not weaken a test to fit an implementation.
3. Make the smallest correct change using Vue 3 Composition API and `<script setup>`. Prefer small composables and page-local state; use Pinia only for shared state.
4. Preserve SSR/client DOM parity and request isolation. Treat route metadata, API response contracts, and localization paths as data contracts.
5. Review the changed path against the applicable checklists. Report changed files and any unverified risk concisely.

## Non-negotiable checks

- Keep public output SSR-visible and deterministic. Guard browser globals and browser-only work.
- Create API clients per SSR request; forward only that request's cookie. Keep browser calls same-origin and do not expose build-time backend origins to client bundles.
- Keep exactly one `main#main-content` beneath `PublicLayout`; its children must not create `main` or `q-page`. `LanguagePage` outside that layout may own its landmark.
- Support `fa`/RTL and `en`/LTR without silent translation fallback. Use API-provided `alternatePath` for a translated detail route.
- Use existing semantic tokens, logical CSS properties, 44px minimum interactive targets, visible focus, and reduced-motion support. Do not introduce raw component colours, gradients, glassmorphism, dark mode, or generic SaaS styling.
- Preserve one H1, valid headings, keyboard operation, focus-managed drawers/dialogs, no hover-only controls, and WCAG AA intent.

## Verification routing

Use RED -> GREEN -> REFACTOR. Run only the requested checks; otherwise give the human the focused Vitest command, full `npm run test:unit`, and `npm run build` to run. When Playwright is installed and browser verification is requested, cover the affected locales/routes plus hydration/Vue warnings, landmarks, 375px overflow, keyboard skip link, mobile navigation, Escape/focus restoration, alternate language behavior, and reduced motion. Ignore browser-extension errors only when clearly attributable to an extension.

## Examples

- "Add a localized portfolio-detail state" -> load Vue/Quasar, SSR/API, and locale/a11y references; use the API alternate path rather than rewriting the slug.
- "Fix a hydration mismatch in the header" -> load SSR/API and Vue/Quasar references; identify non-deterministic or browser-only rendering before changing the component.
- "Review the public mobile drawer" -> load locale/a11y and workflow references; check the focus lifecycle, Escape, target size, RTL behavior, and a focused browser test if requested.

## Combine selectively

Use `ui-ux-pro-max` for page or interaction design, `design-system` for token/component governance, `ui-styling` for visual refinement, `frontend-testing-debugging` for runtime bugs, and Playwright for browser verification. Do not invoke all of them by default.