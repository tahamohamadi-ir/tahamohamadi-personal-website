# Plan 009: Admin frontend MVP

> **Executor instructions**: Implement the admin Quasar application as one
> cohesive authenticated workflow using RED -> GREEN -> REFACTOR. Begin with
> contract and component tests; preserve plan 008 public behavior.

## Status and dependencies

- **Baseline**: `1932457`; execute from reviewed plans 002, 005, 007, and 008
- **Priority / effort / risk**: P0 / XXL (15-22 engineer-days) / HIGH
- **Requirements**: FR-AUTH-001/002, FR-ADMIN-001..003, all admin CRUD Musts,
  NFR-SEC-001..004, NFR-A11Y-001
- **Depends on**: 002, 005, 007, 008
- **Parallel**: not with plan 008 because shared route/i18n/API/CSS files are
  conflict-heavy. Backend plan 005 and initial admin fixture tests may proceed in
  separate worktrees, but live completion waits for a frozen admin contract.

## Preflight and evidence

Run from repository root:

```powershell
git rev-parse --show-toplevel
git branch --show-current
git rev-parse --short HEAD
git merge-base --is-ancestor 1932457 HEAD
git status --porcelain=v1
git diff --stat 1932457..HEAD -- frontend docs/frontend docs/api
git cat-file -e HEAD:plans/005-backend-admin-application-apis.md
```

Require reviewed prerequisites, a green public suite, and frozen auth/admin/media
fixtures. Baseline has only static
`AdminLayout.vue`/`AdminHomePage.vue`; no auth store, route guard, API modules,
forms, tables, editor, upload UI, or E2E harness. Reuse plan 008 `httpClient`,
CSRF utility, semantic tokens, page states, i18n, and Quasar conventions.

## UX and security contract

Routes: `/admin/login`; protected `/admin` dashboard; `/admin/pages`;
`/admin/blog/{posts,categories,tags}`; `/admin/skills`; `/admin/projects`;
`/admin/publications`; `/admin/resume`; `/admin/featured`; `/admin/media`;
`/admin/contact-messages`; localized not-found. Decide before implementation
whether admin is English/LTR only or bilingual; do not silently hard-code a
choice contrary to the human decision.

On application/route bootstrap call CSRF delivery then `/api/v1/auth/me` with
same-origin credentials. Pinia auth state stores only safe user ID/display name/
roles, never password, cookie, session ID, or CSRF token in persisted storage.
Login uses JSON auth API and generic failure; logout uses CSRF, clears all stores,
and redirects. A 401 clears auth and redirects once with safe return route; 403
shows forbidden without retry loop; CSRF 403 refreshes token and retries at most
once only for idempotently safe client intent. No localStorage auth/session data.

Admin layout is dense, quiet, keyboard-efficient, responsive, and uses Quasar/
Lucide-equivalent icons with tooltips. Every module provides paged/sort/filter
list, create/edit form, translation completeness, validation mapping, loading/
empty/error, delete/archive confirmation, and unsaved-change guard. Stale version
409 opens a conflict dialog with reload/discard options; never auto-overwrite.
Blog/page/project Markdown editor is source+preview with sanitized preview; no
unsafe raw HTML. Media UI uses plan 007 multipart contract, progress, allow-list,
alt/caption per locale, orphan/reference warnings. Contact collection hides body;
detail displays user content as plain text with correct direction.

## Exact files

**Modify**:

- `frontend/package.json`, `frontend/package-lock.json` (Playwright and scripts
  only after approved install), `frontend/quasar.config.js`,
  `frontend/src/layouts/AdminLayout.vue`, `frontend/src/pages/admin/AdminHomePage.vue`,
  `frontend/src/router/index.js`, `frontend/src/router/routes.js`,
  `frontend/src/boot/api.js`, `frontend/src/services/httpClient.js`,
  `frontend/src/services/csrf.js`, `frontend/src/css/app.scss`,
  `frontend/src/i18n/en.js`, `frontend/src/i18n/fa.js`, `frontend/README.md`
- `docs/frontend/design-system.md` (record approved admin-language and implemented
  admin component/state conventions only)

**Create services/stores/guards**:

- `frontend/src/services/{authApi.js,adminApi.js,mediaApi.js}`
- `frontend/src/stores/{auth.js,adminUi.js}`
- `frontend/src/router/adminGuard.js`
- `frontend/src/composables/{useAdminList.js,useAdminForm.js,useUnsavedChanges.js,useValidationErrors.js}`

**Create shared admin components**:

- `frontend/src/components/admin/{AdminNav.vue,AdminHeader.vue,AdminPageHeader.vue,AdminDataTable.vue,AdminFilters.vue,AdminFormActions.vue,TranslationEditor.vue,TranslationStatus.vue,MarkdownEditor.vue,OptimisticConflictDialog.vue,ConfirmActionDialog.vue,ValidationSummary.vue,MediaPicker.vue}`

**Create pages**:

- `frontend/src/pages/admin/{LoginPage.vue,DashboardPage.vue,PagesPage.vue,PageEditPage.vue,BlogPostsPage.vue,BlogPostEditPage.vue,BlogCategoriesPage.vue,BlogTagsPage.vue,SkillsPage.vue,ProjectsPage.vue,ProjectEditPage.vue,PublicationsPage.vue,PublicationEditPage.vue,ResumePage.vue,FeaturedPage.vue,MediaPage.vue,ContactMessagesPage.vue,ContactMessageDetailPage.vue,AdminNotFoundPage.vue}`

**Create tests/E2E**:

- `frontend/playwright.config.js`
- `frontend/test/fixtures/admin-api.js`
- `frontend/test/vitest/__tests__/{auth-store.spec.js,admin-guard.spec.js,admin-layout.spec.js,admin-forms.spec.js,admin-conflict.spec.js,media-admin.spec.js,admin-accessibility.spec.js}`
- `frontend/test/e2e/{admin-auth.spec.js,admin-content-workflow.spec.js,admin-media.spec.js}`

No backend, migration, compose, or public route contract change is allowed.

## RED -> GREEN -> REFACTOR

**RED**: create API fixtures and tests before UI. Unit/component coverage: CSRF
then `/me` order, authenticated/anonymous/forbidden routes, generic login error,
safe auth state, logout clearing, no sensitive storage/logging, 401 handling,
CSRF single retry, all route/module render states, validated forms/field mapping,
translation status, paging/filter/sort URL state, unsaved guard, 409 conflict,
sanitized preview, media constraints/progress, contact plain text, keyboard/focus/
labels/dialog semantics/44px targets/reduced motion. E2E RED covers login ->
dashboard -> create draft bilingual page/post -> publish -> logout; old session
cannot return; media upload/select; stale-version conflict.

**GREEN**: implement auth bootstrap/guard first, then layout/shared table/form
primitives, then modules in business order. Keep stores by workflow, forms local,
server authoritative for authorization/validation, and credentials only in the
immediate login request. Use route-level code splitting and abort stale requests.

**REFACTOR**: consolidate only proven list/form patterns, run accessibility and
mobile table/form review, inspect bundle chunks, and rerun public regression.
Never build a schema-driven generic CMS UI in MVP.

## Verification

From `frontend/`:

```powershell
npm run test:unit
npm run test:e2e
npm run build
```

Expected: zero unit/component/E2E failures and zero skipped tests; Quasar SSR
build succeeds; E2E runs against real backend/PostgreSQL 17 test data using an
ephemeral test credential outside migrations and `postgres:17-alpine`, never H2.
Verify desktop/mobile, keyboard,
focus, contrast AA, no overlap/overflow, no auth/token/session data in browser
storage/logs, CSRF on every mutation, generic credential error, and public route
regressions. Performance: route split, bounded tables, no duplicate bootstrap,
cancel stale requests, large editor/media modules lazy. Run `git diff --check`
and `git diff --exit-code HEAD -- backend/src/main/resources/db/migration`;
expect backend/migrations unchanged. Perform the review gate after all tests.

## Exclusions, decisions, STOP conditions

Excluded: user/role admin, registration/reset/MFA, JWT, remember-me, revisions,
analytics collection, theme/menu/slider CMS, drag-and-drop abstractions beyond
approved ordering, WYSIWYG HTML, frontend-only authorization, and public redesign.

Human decisions: admin English-only versus bilingual; approved Markdown editor/
sanitizer and Playwright dependency; exact destructive confirmation copy; E2E
credential provisioning outside migrations. STOP on dirty/drift, unfrozen API,
need to expose entities/secrets, session/CSRF weakening, missing safe sanitizer,
unapproved test dependency, E2E requiring a committed credential, or public SSR
regression.

## Definition of Done and handoff

An administrator can authenticate, manage every MVP domain/media/contact flow,
handle translation/validation/conflicts safely, and log out through accessible
responsive UI; critical E2E and full frontend build are green; no sensitive data
persists client-side; review has no blocker. Handoff to plan 010 provides test
credentials procedure, route/access matrix, build artifact, environment contract,
known browser support, and measured accessibility/performance baselines.
