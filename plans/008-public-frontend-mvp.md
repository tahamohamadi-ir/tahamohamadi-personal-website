# Plan 008: Public frontend MVP

> **Executor instructions**: Implement the complete public Quasar SSR experience
> as one cohesive wave using RED -> GREEN -> REFACTOR. Tests and contract fixtures
> precede UI code. Do not build marketing-only placeholders; build the usable MVP.

## Status and dependencies

- **Baseline**: `1932457`; execute from a reviewed successor with plans 006/007
- **Priority / effort / risk**: P0 / XXL (15-22 engineer-days) / HIGH
- **Requirements**: all MVP public FRs, FR-I18N-001, FR-SEO-001..003,
  NFR-PERF-001/002, NFR-A11Y-001, ADR-002/006
- **Depends on**: 006 and 007 contracts; plan 003 schema may run concurrently
  with early fixture/shell work, but live integration waits for 006
- **Parallel**: shell/tokens/fixture components can run in a frontend-only
  worktree while backend schema work proceeds. Do not run full plan 008 with 009:
  conflict paths are `routes.js`, router guards, `app.scss`, i18n files, boot
  files, `package.json`, and shared API client. Merge 008 before starting 009.

## Preflight and current evidence

Run from repository root:

```powershell
git rev-parse --show-toplevel
git branch --show-current
git rev-parse --short HEAD
git merge-base --is-ancestor 1932457 HEAD
git status --porcelain=v1
git diff --stat 1932457..HEAD -- frontend docs/frontend docs/api
git cat-file -e HEAD:plans/006-backend-public-content-search-seo-contact-apis.md
```

Require only reviewed prerequisites. Current frontend has only `App.vue`, `PublicLayout.vue`,
`PublicHomePage.vue`, `LanguagePage.vue`, two i18n files, routes for `/fa` and
`/en`, and one six-test foundation spec. `quasar.config.js` uses SSR; no API
client/content store/public components exist. Bind to `docs/frontend/design-system.md`,
`.codex/frontend-rules.md`, i18n/SEO rules, ADR-002/006, and plan 006 fixtures.

## Experience and route contract

Implement SSR/hydration routes: `/` language choice/negotiation; `/{lang}` home;
`/{lang}/about`; `/research`; `/skills`; `/portfolio`; `/portfolio/:slug`;
`/blog`; `/blog/:slug`; `/publications`; `/publications/:slug`; `/resume`;
`/contact`; and a localized not-found/translation-unavailable route. `lang` is
only `fa|en`; Persian sets `lang=fa dir=rtl`, English `lang=en dir=ltr`. Language
switch uses API-provided alternate path; never string-substitute slugs or silently
show another language.

Use Academic Editorial Utility direction and semantic tokens from the design
system: restrained content-first layouts, no decorative card nesting, stable
responsive grids, real approved portrait/project/publication/media assets, and
Quasar controls/icons. Home first viewport identifies Taha Mohamadi and exposes
the next content section; no marketing splash or feature-description text.

Each route has loading skeleton, content, explicit empty, recoverable error,
offline, translation-unavailable, and 404 states. Contact form has labeled fields,
inline errors, CSRF bootstrap, one submission at a time, safe success/reset, and
preserves user input on recoverable failure. Public API requests carry cookies
same-origin, CSRF header only for mutation, abort stale navigation, and never log
contact content/cookies/tokens.

SEO per route: SSR-visible title/description/canonical/hreflang/OG, one H1,
semantic landmarks/headings, Person/WebSite/BlogPosting/ScholarlyArticle/
CreativeWork/ContactPage/BreadcrumbList JSON-LD as applicable, sitemap/robots
delivery integration, no draft/admin/private URLs. JSON-LD is serialized safely,
not raw HTML from content. Markdown rendering must use an approved sanitizer;
STOP if none exists rather than using `v-html` unsafely.

## Exact files

**Modify existing**:

- `frontend/package.json`, `frontend/package-lock.json` only if an approved
  sanitizer/test dependency is required
- `frontend/quasar.config.js`, `frontend/src/App.vue`,
  `frontend/src/layouts/PublicLayout.vue`, `frontend/src/router/index.js`,
  `frontend/src/router/routes.js`, `frontend/src/css/app.scss`,
  `frontend/src/i18n/en.js`, `frontend/src/i18n/fa.js`,
  `frontend/src-ssr/server.js`, `frontend/src-ssr/middlewares/render.js`,
  `frontend/README.md`

**Create foundation files**:

- `frontend/src/boot/api.js`
- `frontend/src/services/httpClient.js`
- `frontend/src/services/publicApi.js`
- `frontend/src/services/csrf.js`
- `frontend/src/composables/useAsyncPage.js`
- `frontend/src/composables/useLocaleRoute.js`
- `frontend/src/composables/useSeoMeta.js`
- `frontend/src/stores/publicContent.js`
- `frontend/src/css/tokens.scss`
- `frontend/src/css/typography.scss`
- `frontend/src/utils/jsonLd.js`

**Create shared public components**:

- `frontend/src/components/public/{SiteHeader.vue,SiteFooter.vue,LanguageSwitch.vue,SkipLink.vue,Breadcrumbs.vue,SeoHead.vue,PageState.vue,TranslationUnavailable.vue,MediaImage.vue,PaginationControl.vue}`
- `frontend/src/components/content/{MarkdownContent.vue,FeaturedList.vue,PostCard.vue,ProjectCard.vue,PublicationItem.vue,SkillGroups.vue,ResumeTimeline.vue,ContactForm.vue}`

**Create pages**:

- `frontend/src/pages/public/{PublicHomePage.vue,AboutPage.vue,ResearchPage.vue,SkillsPage.vue,PortfolioPage.vue,ProjectDetailPage.vue,BlogPage.vue,BlogPostPage.vue,PublicationsPage.vue,PublicationDetailPage.vue,ResumePage.vue,ContactPage.vue,TranslationUnavailablePage.vue,NotFoundPage.vue}`

**Create test fixtures/tests**:

- `frontend/test/fixtures/public-api.js`
- `frontend/test/vitest/__tests__/public-routing.spec.js`
- `frontend/test/vitest/__tests__/public-pages.spec.js`
- `frontend/test/vitest/__tests__/public-i18n-seo.spec.js`
- `frontend/test/vitest/__tests__/contact-form.spec.js`
- `frontend/test/vitest/__tests__/public-accessibility.spec.js`

No backend, migration, compose, or admin implementation change is allowed.

## RED -> GREEN -> REFACTOR

**RED**: freeze representative plan 006 response/error fixtures; write tests for
all route mappings, SSR metadata/JSON-LD, fa RTL/en LTR, language alternates,
missing translations, loading/empty/error/offline states, pagination/filter URL
sync, contact validation/CSRF/single-submit, no sensitive logging, keyboard nav,
focus restoration, skip link, landmarks, one H1, accessible names, alt handling,
and reduced motion. Capture failures due to missing pages/components/services.

**GREEN**: add semantic tokens and shell; API/SSR data boundary; reusable states;
pages in journey order (home/profile, resume/research/publications, blog/portfolio,
contact); then SEO and sitemap/robots hooks. Use Pinia only for shared public
cache/navigation state; keep page-local state local. Provide fixed aspect ratios
and responsive constraints so media/loading text cannot shift layouts.

**REFACTOR**: deduplicate proven content patterns, inspect CSS palette/token use,
remove dead fixtures, and validate SSR and hydrated parity. Do not make a generic
page renderer or component abstraction before repeated behavior exists.

## Verification

From `frontend/`:

```powershell
npm run test:unit
npm run build
```

Expected: all Vitest tests pass with zero skipped; Quasar SSR build succeeds;
rendered HTML contains route content and SEO without client-only dependence. Live
contract inspection uses the normal backend with `postgres:17-alpine`, never H2. Run
the repository-approved dev command and inspect 375, 768, 1024, 1440 viewports in
both locales: no overlap/overflow, correct direction, visible focus, 44px targets,
keyboard access, contrast AA, reduced motion, meaningful alt, and next-section
hint on home. Validate sitemap/robots/canonical/hreflang/JSON-LD and no admin/
draft URLs. Measure representative production build against LCP <2.5s target,
lazy media, font loading, bundle split, no avoidable large dependency. Then run
`git diff --check` and
`git diff --exit-code HEAD -- backend/src/main/resources/db/migration`; expect
backend/migrations unchanged, then perform the review gate.

## Exclusions, decisions, STOP conditions

Excluded: admin screens, analytics, dark mode, comments, timeline, sliders,
theme/menu CMS, Telegram, UI for uploads, frontend auth, speculative animation,
and source changes outside frontend/docs specified by this plan.

Human decisions required before visual GREEN: approved portrait/logo/favicon/OG
assets; font hosting/licensing/subsetting; final semantic color/state contrast;
public project-detail inclusion is explicitly required by this roadmap despite
the older MVP scope labeling detail as post-MVP. STOP on dirty/drift, unfrozen API
contract, missing assets that would force placeholder production visuals,
unapproved sanitizer/dependency, request for silent fallback, unsafe `v-html`,
SSR/hydration mismatch, or inability to meet core accessibility behavior.

## Definition of Done and handoff

All public routes are functional SSR pages in fa/en with explicit translation
behavior, complete states, accessible responsive UI, live contract compatibility,
SEO/structured data/sitemap/robots integration, passing tests/build, and no
review blocker. Handoff to plan 009 identifies shared shell/API/i18n ownership;
handoff to 010 includes build output, route inventory, performance/a11y/SEO
baselines, required environment values, and unresolved content/asset tasks.
