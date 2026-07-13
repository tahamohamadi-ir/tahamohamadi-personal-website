# Backend Completion Execution Plan

> **For agentic workers:** REQUIRED SUB-SKILL: use `superpowers:executing-plans` task-by-task. Mark a checkbox only after the listed verification succeeds.

**Goal:** Deliver a DTO-only, PostgreSQL 17-tested backend gate before any frontend task starts.

**Architecture:** Modular Spring Boot monolith; feature-local controllers/services/mappers/repositories; session/CSRF security; Flyway V1-V7 immutable. Public API contracts are language-prefixed, published-only, bounded, and SEO-shaped.

**Tech stack:** Java 21, Spring Boot, Spring Security, JPA, Flyway, PostgreSQL 17 Testcontainers, Maven.

## Status

| Field | Value |
|---|---|
| Current branch | `feat/backend-completion` |
| Baseline commit | `2741a53 feat(backend): add authenticated admin audit actor` |
| Last completed task | A1: Admin Blog draft CRUD |
| Next task | B1: Blog lifecycle |
| Known blockers | Dirty Blog CRUD foundation is uncompiled; no external blocker |
| Last verification date | 2026-07-13 — AdminBlogCrudIntegrationTest and compile passed |
| Backend readiness | `BACKEND_NOT_READY` |

## Global constraints

- Never edit `backend/src/main/resources/db/migration/V1__*` through `V7__*`; add a forward-only migration only when schema requires it.
- Use `mvn`, never `mvnw.cmd`; focused tests after each task; full suite only gates F, K, L.
- Controllers return records/DTOs only; every growing collection has validated pagination and allow-listed deterministic sort.
- Admin mutations require ADMIN/SUPER_ADMIN and CSRF; audits use `AuthenticatedAuditActor`, never request actor input.
- Do not begin M or N until L reports `BACKEND_READY_FOR_FRONTEND`; do not push, merge, rebase, reset, restore, clean, or automatically commit.

## A. Stabilize existing Blog CRUD

### A1: Compile and complete Blog draft CRUD
**Files:** Modify `backend/src/main/java/ir/tahamohamadi/blog/post/{BlogPost.java,BlogPostTranslation.java,BlogPostRepository.java,BlogPostTranslationRepository.java}`; modify/create `backend/src/main/java/ir/tahamohamadi/blog/post/api/admin/{AdminBlogController.java,AdminBlogService.java,AdminBlogMapper.java,AdminBlogPostRequest.java,AdminBlogPostResponse.java,AdminBlogPostSummary.java}`; create `backend/src/test/java/ir/tahamohamadi/admin/AdminBlogCrudIntegrationTest.java`.
**Interfaces:** Produces `GET/POST/GET{id}/PUT /api/v1/admin/blog/posts`; consumes `PageResponse`, `AuthenticatedAuditActor`, `BlogCategoryRepository`.
- [x] Write Testcontainers MockMvc RED cases for anonymous 401 with CSRF, user 403, admin 201 draft, safe DTO, max size 100, invalid body 400, stale version 409.
- [x] Run `mvn "-Dtest=AdminBlogCrudIntegrationTest" test`; observed and fixed controller import and aggregate-version failures.
- [x] Implement explicit mapper and repository projection/entity-graph query that retrieves each post/category and both translations without per-row lookups; reject inactive/deleted category, duplicate slugs, and missing fa/en translations.
- [x] Run `mvn "-Dtest=AdminBlogCrudIntegrationTest" test`; PostgreSQL 17 BUILD SUCCESS, zero failures/errors/skips in 25.670 seconds.
- [x] Run `git diff --check`; no output. Checkpoint message: `feat(blog): add admin draft CRUD`.
**Acceptance:** Draft create/update is transactional, localized, versioned, paged/sorted, DTO-only. **Rollback-safe:** no migration. **Maximum:** 45 minutes. **Do not change:** public APIs, media/contact/pages.

## B. Blog lifecycle

### B1: Categories, tags, post references, lifecycle, audit
**Files:** Create `backend/src/main/java/ir/tahamohamadi/blog/{category,tag}/api/admin/*`; modify post tag/media repositories and blog services; create `backend/src/test/java/ir/tahamohamadi/admin/{AdminBlogLifecycleIntegrationTest,AdminBlogAuditIntegrationTest}.java`.
**Interfaces:** Produces CRUD `/blog/categories`, `/blog/tags`, `POST /blog/posts/{id}/{publish,archive}`, `DELETE /blog/posts/{id}`.
- [ ] Write RED tests for active localized category/tag CRUD, ordered tags/media, CSRF, ADMIN/SUPER_ADMIN, publish SEO/reference rule 422, stale 409, and actor-attributed sanitized `ADMIN_BLOG_*` audits.
- [ ] Run `mvn "-Dtest=AdminBlogLifecycleIntegrationTest,AdminBlogAuditIntegrationTest" test`; expect RED failures.
- [ ] Implement bounded repository projections, translation batch mapping, lifecycle invariants, soft delete, and same-transaction audit actions `ADMIN_BLOG_CATEGORY_*`, `ADMIN_TAG_*`, `ADMIN_BLOG_POST_{CREATED,UPDATED,PUBLISHED,ARCHIVED,DELETED}`.
- [ ] Run the same command; expect PostgreSQL 17 `BUILD SUCCESS`.
- [ ] Run `git diff --check`; expect no output. Checkpoint message: `feat(blog): complete admin lifecycle`.
**Acceptance:** all Plan 005 blog routes are secured/audited/locked. **Rollback-safe:** no migration. **Maximum:** 75 minutes. **Do not change:** skills, portfolio, public APIs.

## C. Admin Skills

### C1: Categories and skills
**Files:** Create `backend/src/main/java/ir/tahamohamadi/skill/api/admin/*`; modify skill repositories/entities only for bounded projection/update methods; create `backend/src/test/java/ir/tahamohamadi/admin/AdminSkillIntegrationTest.java`.
- [ ] Write RED CRUD/deactivate/order/locale/CSRF/role/version/audit tests.
- [ ] Run `mvn "-Dtest=AdminSkillIntegrationTest" test`; expect RED.
- [ ] Implement DTO mapper/services/controllers with size 1..100, sort allow-list, translation batch query, `ADMIN_SKILL_CATEGORY_*` and `ADMIN_SKILL_*` audits.
- [ ] Run the same command; expect PostgreSQL 17 `BUILD SUCCESS`.
- [ ] Run `git diff --check`; expect no output. Checkpoint message: `feat(skills): add admin CRUD`.
**Acceptance:** DTO-only ordered deactivate semantics. **Rollback-safe:** no migration. **Maximum:** 60 minutes. **Do not change:** portfolio/public APIs.

## D. Admin Portfolio

### D1: Localized projects
**Files:** Create `backend/src/main/java/ir/tahamohamadi/portfolio/project/api/admin/*`; modify project repositories/junction entities; create `backend/src/test/java/ir/tahamohamadi/admin/AdminProjectIntegrationTest.java`.
- [ ] Write RED localized CRUD/media/skill order/publish/archive/CSRF/role/version/audit tests.
- [ ] Run `mvn "-Dtest=AdminProjectIntegrationTest" test`; expect RED.
- [ ] Implement bounded projections, active reference validation, lifecycle, mapper, and `ADMIN_PROJECT_*` audits.
- [ ] Run the same command; expect PostgreSQL 17 `BUILD SUCCESS`.
- [ ] Run `git diff --check`; expect no output. Checkpoint message: `feat(portfolio): add admin projects`.
**Acceptance:** localized DTO-only projects with no N+1. **Rollback-safe:** no migration. **Maximum:** 75 minutes. **Do not change:** public APIs.

## E. Admin Pages corrections

### E1: Page query and audit regression
**Files:** Modify `content/page/{ContentPageRepository.java,ContentPageTranslationRepository.java,api/admin/AdminPageService.java}`; modify `AdminPageApiIntegrationTest`; create `AdminPageAuditAndConcurrencyIntegrationTest.java`.
- [ ] Write RED query-count, persisted actor, stale update, CSRF/role/paging tests.
- [ ] Run `mvn "-Dtest=AdminPageApiIntegrationTest,AdminPageAuditAndConcurrencyIntegrationTest" test`; expect RED.
- [ ] Replace per-page translation lookups with one projection/batch query; preserve response shape and 409 mapping.
- [ ] Run the same command; expect PostgreSQL 17 `BUILD SUCCESS`.
- [ ] Run `git diff --check`; expect no output. Checkpoint message: `fix(pages): remove translation n plus one`.
**Acceptance:** no per-row translation queries; audit actor stored. **Maximum:** 35 minutes.

## F. Pack B acceptance gate

### F1: Verify all admin contracts
**Files:** Modify `docs/api/api-design.md`, `docs/architecture/security.md`; create/modify `backend/src/test/java/ir/tahamohamadi/admin/{AdminAuthorizationIntegrationTest,AdminApiIntegrationTest,AdminAuditIntegrationTest,AdminValidationUnitTest}.java`.
- [ ] Run `mvn "-Dtest=AdminValidationUnitTest,AdminAuthorizationIntegrationTest,AdminApiIntegrationTest,AdminAuditIntegrationTest" test`; expect all PG17 tests green.
- [ ] Run `mvn -DskipTests compile` and `git diff --check`; expect `BUILD SUCCESS` and no whitespace errors.
- [ ] Inspect `git diff --stat`, admin entity exposure, query bounds, V1-V7 checksums; record only verified contract updates.
- [ ] Checkpoint message: `test(admin): verify pack b gate`.
**Acceptance:** Plan 005 complete. **Maximum:** 45 minutes. **Do not change:** frontend.

## G. Publications and Resume admin/public slices
### G1: Implement publication/resume DTO workflows
**Files:** Create `publication/api/{admin,publicsite}/*`, `resume/api/{admin,publicsite}/*`; create `backend/src/test/java/ir/tahamohamadi/{admin,publicapi}/PublicationResumeIntegrationTest.java`.
- [ ] Write RED admin/public published-only locale/version/audit tests.
- [ ] Run `mvn "-Dtest=PublicationResumeIntegrationTest" test`; expect RED.
- [ ] Implement bounded DTO services/controllers, publish/archive, current-document policy, and audit actions.
- [ ] Run the same command; expect PG17 `BUILD SUCCESS`.
- [ ] Run `git diff --check`; expect no output. Checkpoint message: `feat(content): add publications and resume`.
**Acceptance:** Plan 005/006 publication/resume routes. **Maximum:** 90 minutes.

## H. Featured Content and Social Links
### H1: Implement featured/social admin/public contracts
**Files:** Create `content/{featured,social}/api/{admin,publicsite}/*`; create `backend/src/test/java/ir/tahamohamadi/{admin,publicapi}/FeaturedSocialIntegrationTest.java`.
- [ ] Write RED active-window/published-target/order/CSRF/version/audit/public filtering tests.
- [ ] Run `mvn "-Dtest=FeaturedSocialIntegrationTest" test`; expect RED.
- [ ] Implement bounded DTO workflows and `ADMIN_FEATURED_ITEM_*`, `ADMIN_SOCIAL_LINK_*` audits.
- [ ] Run the same command; expect PG17 `BUILD SUCCESS`.
- [ ] Run `git diff --check`; expect no output. Checkpoint message: `feat(content): add featured and social`.
**Acceptance:** no inactive/private target public leak. **Maximum:** 60 minutes.

## I. Public content APIs
### I1: Implement all published localized read endpoints
**Files:** Replace `publicsite/api/*` with exact paths listed in Plan 006; create `content/api/publicsite/*`, `blog/api/publicsite/*`, `skill/api/publicsite/*`, `portfolio/project/api/publicsite/*`; create `backend/src/test/java/ir/tahamohamadi/publicapi/PublicContentIntegrationTest.java`.
- [ ] Write RED fa/en/missing-locale/draft/future/deleted/page-size/filter/sort tests.
- [ ] Run `mvn "-Dtest=PublicContentIntegrationTest" test`; expect RED.
- [ ] Implement DTO-only projections with size <=50, canonical/locale metadata and no fallback.
- [ ] Run the same command; expect PG17 `BUILD SUCCESS`.
- [ ] Run `git diff --check`; expect no output. Checkpoint message: `feat(public): add localized content APIs`.
**Acceptance:** all Plan 006 content endpoints bounded/published-only. **Maximum:** 120 minutes.

## J. Search, localization, SEO, hreflang, sitemap-data
### J1: Implement public discoverability contracts
**Files:** Create `common/i18n/{LocaleNormalizer.java,TranslationUnavailableException.java}`, `seo/{SeoMetadataResponse.java,HreflangResponse.java,SitemapDataController.java,SitemapDataService.java,SitemapEntryResponse.java,RobotsController.java}`; create `PublicBlogSearchIntegrationTest.java`, `PublicSeoContractIntegrationTest.java`, `LocaleNormalizerUnitTest.java`.
- [ ] Write RED bounded FTS/injection/locale/empty query, canonical/hreflang/OG/sitemap/robots tests.
- [ ] Run `mvn "-Dtest=LocaleNormalizerUnitTest,PublicBlogSearchIntegrationTest,PublicSeoContractIntegrationTest" test`; expect RED.
- [ ] Implement parameterized PG FTS, Persian normalizer, DTO SEO metadata, published sitemap entries and no-index nonproduction robots.
- [ ] Run the same command; expect PG17 `BUILD SUCCESS`.
- [ ] Run `git diff --check`; expect no output. Checkpoint message: `feat(seo): add public discovery contracts`.
**Acceptance:** Plan 006 SEO/i18n complete. **Maximum:** 90 minutes.

## K. Pack C acceptance gate
### K1: Verify Pack C
**Files:** Modify `docs/api/api-design.md`, `docs/architecture/{architecture.md,security.md}`; tests from G-I.
- [ ] Run `mvn "-Dtest=PublicContentIntegrationTest,PublicBlogSearchIntegrationTest,PublicContactIntegrationTest,PublicSeoContractIntegrationTest" test`; expect green PG17.
- [ ] Run `mvn -DskipTests compile` and `git diff --check`; expect success.
- [ ] Inspect public entity exposure, locale fallback, page limits, FTS plan, media URLs. Checkpoint message: `test(public): verify pack c gate`.
**Acceptance:** Plan 006 complete. **Maximum:** 45 minutes.

## L. Full backend acceptance gate
### L1: Run immutable backend gate
**Files:** Create/modify only acceptance evidence in `docs/testing/mvp-acceptance.md`.
- [ ] Run `mvn -DskipTests compile`; expect `BUILD SUCCESS`.
- [ ] Run `mvn test`; expect zero failures/errors/skips with PostgreSQL 17 Testcontainers.
- [ ] Run `mvn "-Dtest=FlywayUpgradeIntegrationTest,FlywayWaveAIntegrationTest,FlywayWaveBIntegrationTest" test`; expect V1-V7 validation success.
- [ ] Run `git diff --check; git status --short; git diff --stat`; expect no scope violation.
- [ ] Inspect CSRF/RBAC/audit/version/entity exposure/N+1/unbounded repositories; record `BACKEND_READY_FOR_FRONTEND` only if all green. Checkpoint message: `test(backend): pass acceptance gate`.
**Acceptance:** backend ready. **Maximum:** 90 minutes. **Do not change:** frontend.

## M. Public frontend
### M1: Execute Plan 008 after L1 only
**Files:** Exact Plan 008 files; tests `frontend/test/e2e/*public*`.
- [ ] Verify L1 verdict is `BACKEND_READY_FOR_FRONTEND`.
- [ ] Execute Plan 008 RED/GREEN/REFACTOR and `npm run test:unit; npm run build; npm run test:e2e`; expect zero failures.
**Acceptance:** Plan 008. **Maximum:** 180 minutes. **Do not change:** backend contracts without a backend task.

## N. Admin frontend
### N1: Execute Plan 009 after M1 only
**Files:** Exact Plan 009 files; tests `frontend/test/e2e/admin-*`.
- [ ] Verify M1 green and frozen admin contract.
- [ ] Execute Plan 009 and `npm run test:unit; npm run build; npm run test:e2e`; expect zero failures.
**Acceptance:** Plan 009. **Maximum:** 180 minutes.

## O. Integration, hardening, delivery
### O1: Execute Plan 010 release gate
**Files:** Exact runtime, CI, runbook, smoke, backup and test paths in Plan 010.
- [ ] Write RED delivery tests and run them; expect missing-config failures.
- [ ] Implement containers, production profile, proxy, CI, runbooks, smoke/backup/restore scripts.
- [ ] Run exact Plan 010 backend/frontend/Compose/restore commands; expect green deployment rehearsal.
- [ ] Inspect secrets, headers, logs, restore evidence and release checklist. Checkpoint message: `feat(delivery): complete mvp release gate`.
**Acceptance:** Plan 010 release sign-off. **Maximum:** 240 minutes.

## Self-review

- [x] Plans 005-007 are represented by A-L; Plan 008 starts only after L; Plan 009 starts only after M; Plan 010 is O.
- [x] Current dirty Blog foundation is explicitly stabilized first.
- [x] No task edits applied migrations, returns entities, or permits unbounded collections.
- [x] Full Maven suite appears only at L; every implementation task has focused Maven verification.

## Progress log

| Task | Status | Commit | Verification | Duration | Notes |
|---|---|---|---|---|---|
| Pack A media/contact | [x] | `2741a53` ancestry | Focused PG17 media tests | recorded | Preserve |
| Admin Pages foundation | [x] | working tree | `AdminPageApiIntegrationTest` | recorded | Needs E1 correction |
| A1 Blog draft CRUD | [x] | pending local checkpoint | AdminBlogCrudIntegrationTest; compile; diff check | 25.670s test + 1.682s compile | Category entity graph and aggregate version verified |
| B1 Blog lifecycle | [ ] | — | — | — | — |
| C1 Skills | [ ] | — | — | — | — |
| D1 Portfolio | [ ] | — | — | — | — |
| E1 Pages correction | [ ] | — | — | — | — |
| F1 Pack B gate | [ ] | — | — | — | — |
| G1 Publication/resume | [ ] | — | — | — | — |
| H1 Featured/social | [ ] | — | — | — | — |
| I1 Public content | [ ] | — | — | — | — |
| J1 SEO/search | [ ] | — | — | — | — |
| K1 Pack C gate | [ ] | — | — | — | — |
| L1 Backend gate | [ ] | — | — | — | — |
| M1 Public frontend | [ ] | — | — | — | blocked by L1 |
| N1 Admin frontend | [ ] | — | — | — | blocked by M1 |
| O1 Delivery | [ ] | — | — | — | blocked by N1 |
