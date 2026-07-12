# Plan 006: Backend public content, search, SEO, and contact APIs

> **Executor instructions**: Build this public read/submission slice tests-first
> using RED -> GREEN -> REFACTOR. It is self-contained and must not weaken
> publication, locale, CSRF, or privacy boundaries.

## Status and dependencies

- **Baseline**: `1932457`; execute from reviewed implementations of 002-004 and 007
- **Priority / effort / risk**: P0 / XL (10-15 engineer-days) / HIGH
- **Requirements**: FR-LANDING-001/002, FR-PAGE-001, FR-BLOG-001..005,
  FR-PORTFOLIO-001, FR-PUBLICATION-001, FR-RESUME-001/002, FR-SKILL-001,
  FR-CONTACT-001, FR-SEO-001..003, FR-I18N-001, NFR-PERF-001/002
- **Depends on**: 003, 004, 007; may execute parallel to 005 as described there
- **Merge prerequisite**: if 005 merged first, reuse its common paging/error
  contracts; otherwise do not create competing versions and reconcile before merge.

## Preflight and current evidence

Run from repository root:

```powershell
git rev-parse --show-toplevel
git branch --show-current
git rev-parse --short HEAD
git merge-base --is-ancestor 1932457 HEAD
git status --porcelain=v1
git diff --stat 1932457..HEAD -- backend/src/main backend/src/test docs/api docs/architecture
git cat-file -e HEAD:backend/src/main/resources/db/migration/V7__create_featured_content_and_reviewed_indexes.sql
```

Require reviewed prerequisite drift and no unrelated changes. At baseline there
is no public controller beyond `auth/security/CsrfTokenController.java`.
`docs/api/api-design.md` binds `/api/v1/public/{lang}`; architecture and i18n
rules forbid silent translations; SEO rules require canonical/hreflang/metadata;
data model permits PostgreSQL FTS only for blog; current security configuration
permits public endpoints but retains CSRF for state changes.

## Exact public contracts

All GET responses are DTO-only, cacheable with explicit validators/policy, and
contain requested `locale`, `availableLocales`, canonical path, hreflang links,
SEO title/description, OG media DTO, and last-modified data where applicable.
Only nondeleted PUBLISHED content with `published_at <= now` and the requested
translation is exposed. Missing translation is never silently substituted:
detail returns `404 TRANSLATION_UNAVAILABLE` with safe `availableLocales` and
alternate paths; collections omit the item in that locale. Invalid locale is
400. Lists use bounded size <= 50 and stable ordering.

- `GET /api/v1/public/{lang}/home`: page copy, active time-window featured
  items, latest posts, selected projects/publications, skills summary, socials.
- `GET /api/v1/public/{lang}/pages/{slug}`.
- `GET /api/v1/public/{lang}/posts?page,size&category,tag&q` and
  `GET .../posts/{slug}`; `q` uses parameterized PostgreSQL FTS, English config
  `english`, Persian `simple` plus an approved deterministic normalizer.
- `GET .../categories`, `GET .../tags` with active localized values/counts.
- `GET .../portfolio?page,size&skill` and `GET .../portfolio/{slug}`.
- `GET .../skills`, `GET .../publications?page,size&stage`,
  `GET .../publications/{slug}`, `GET .../resume`, `GET .../featured`.
- `POST /api/v1/public/contact`: JSON `{name,email,message,language}` with
  Bean Validation; valid CSRF is required; `201` returns only
  `{id,status:"RECEIVED",submittedAt}`. Never echo message/email.
- `GET /api/v1/public/sitemap-data`: internal frontend/SSR contract returning
  both locales' canonical published routes; never drafts/admin/private media.
- `GET /robots.txt`: production allows public routes and disallows `/admin` and
  `/api`; nonproduction returns a no-index policy. `/sitemap.xml` remains an SSR/
  delivery integration concern fed by sitemap-data; do not duplicate ownership.

Errors reuse the safe common shape: 400 validation/query/locale, 403 CSRF, 404
not found/translation unavailable, 429 reserved for future infrastructure (not
returned without an actual limiter), 500 generic. Public GETs allow same-origin
and configured exact origins only; never wildcard credentialed CORS.

## Exact files

**Create public API support**:

- `backend/src/main/java/ir/tahamohamadi/common/api/PublicPageResponse.java`
- `backend/src/main/java/ir/tahamohamadi/common/i18n/TranslationUnavailableException.java`
- `backend/src/main/java/ir/tahamohamadi/common/i18n/LocaleNormalizer.java`
- `backend/src/main/java/ir/tahamohamadi/seo/SeoMetadataResponse.java`
- `backend/src/main/java/ir/tahamohamadi/seo/HreflangResponse.java`
- `backend/src/main/java/ir/tahamohamadi/seo/SitemapDataController.java`
- `backend/src/main/java/ir/tahamohamadi/seo/SitemapDataService.java`
- `backend/src/main/java/ir/tahamohamadi/seo/SitemapEntryResponse.java`
- `backend/src/main/java/ir/tahamohamadi/seo/RobotsController.java`

**Create controller/service/DTO files**:

- `backend/src/main/java/ir/tahamohamadi/content/api/publicsite/{PublicHomeController.java,PublicHomeService.java,HomeResponse.java,FeaturedContentResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/content/page/api/publicsite/{PublicPageController.java,PublicPageService.java,PublicPageResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/blog/api/publicsite/{PublicBlogController.java,PublicBlogService.java,BlogPostSummaryResponse.java,BlogPostDetailResponse.java,BlogCategoryResponse.java,TagResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/portfolio/project/api/publicsite/{PublicPortfolioController.java,PublicPortfolioService.java,ProjectSummaryResponse.java,ProjectDetailResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/skill/api/publicsite/{PublicSkillController.java,PublicSkillService.java,SkillCategoryResponse.java,SkillResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/publication/api/publicsite/{PublicPublicationController.java,PublicPublicationService.java,PublicationSummaryResponse.java,PublicationDetailResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/resume/api/publicsite/{PublicResumeController.java,PublicResumeService.java,ResumeResponse.java,ResumeEntryResponse.java,ResumeDocumentResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/content/featured/api/publicsite/{PublicFeaturedController.java,PublicFeaturedService.java}`
- `backend/src/main/java/ir/tahamohamadi/content/contact/api/publicsite/{PublicContactController.java,PublicContactService.java,ContactSubmissionRequest.java,ContactSubmissionResponse.java}`

**Modify** the plan 003-004 repositories only for projection queries;
`common/api/GlobalExceptionHandler.java` if plan 005 exists;
`auth/security/SecurityConfiguration.java` only for explicit public/robots
matchers without disabling CSRF; `application.yml` for documented cache/query
limits only; `docs/api/api-design.md`, `docs/architecture/architecture.md`, and
`docs/architecture/security.md`.

**Create tests**:

- `backend/src/test/java/ir/tahamohamadi/publicapi/PublicContentIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/publicapi/PublicBlogSearchIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/publicapi/PublicContactIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/publicapi/PublicSeoContractIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/publicapi/LocaleNormalizerUnitTest.java`

No migration, POM, upload, or frontend change is expected.

## RED -> GREEN -> REFACTOR

**RED**: add tests first and record endpoint-not-found/missing-bean failures.
Seed test data through repositories/SQL in the PostgreSQL 17 container. Assert
every endpoint/field/status, requested locale, missing-translation response,
draft/archived/future/deleted exclusion, stable pagination/filtering, home
composition/time-window features, media URL/alt contract, CV selection, and
canonical/hreflang/sitemap/robots behavior. Search tests cover title/excerpt/body,
ranking stability, category/tag combination, SQL metacharacters/injection input,
locale isolation, and GIN-backed plan. Contact tests cover CSRF 403, validation,
oversize payload, no echo/log/audit of body/email, `NEW` persistence, duplicate
submissions allowed without invented deduplication, and safe 201.

**GREEN**: implement transactional read-only composition services and a narrow
write transaction for contact. Use projections/mappers and parameterized FTS.
Define cache boundaries: public GETs may use response caching/ETag keyed by path,
locale, query, and content update time; auth/admin/contact/CSRF responses are
`no-store`; no Redis or cross-node cache. Document rate-limit integration point
at contact service/filter boundary, but do not fake a limiter.

**REFACTOR**: query-count and payload review, reuse common contracts after plan
005 merge, update docs, and rerun auth/admin regressions. Do not create a generic
content service or cache abstraction without a second implementation.

## Verification and quality gates

From `backend/`:

```powershell
.\mvnw.cmd -Dtest=LocaleNormalizerUnitTest test
.\mvnw.cmd -Dtest=PublicContentIntegrationTest test
.\mvnw.cmd -Dtest=PublicBlogSearchIntegrationTest test
.\mvnw.cmd -Dtest=PublicContactIntegrationTest test
.\mvnw.cmd -Dtest=PublicSeoContractIntegrationTest test
.\mvnw.cmd -Dtest=SecurityConfigurationUnitTest,SessionSecurityIntegrationTest test
.\mvnw.cmd test
```

Expected BUILD SUCCESS, zero failures/errors/skips, `postgres:17-alpine`, no H2,
no migration past V7, and all existing tests green. Run `git diff --check`,
migration checksum verification, sensitive-log scan, POM/frontend unchanged.
Performance assertions: <=50 page size, stable keys, representative query-count
ceilings, no entity graphs with unbounded to-many, FTS `EXPLAIN`, compact home
payload, cache headers. Accessibility contract: localized text direction remains
a frontend concern, but every meaningful media item supplies localized alt state,
heading content is structured, and errors expose stable message codes.

## Exclusions and STOP conditions

Excluded: admin APIs, frontend SSR implementation, external search/cache,
portfolio/publication FTS, `pg_trgm`, comments, analytics, email sending, CAPTCHA,
rate-limit infrastructure, IP storage, silent translation fallback, preview,
unpublished media, `/llms.txt`, and migrations.

STOP on dirty/material drift; missing prerequisite/common-contract ownership;
need for schema/dependency; unresolved Persian normalization behavior; request to
expose unpublished/missing-locale data; inability to keep contact PII out of
logs/audit; wildcard credentialed CORS; CSRF weakening; or unavailable PG17.

## Definition of Done and handoff

All public contracts return only eligible localized DTOs, FTS/contact/SEO rules
are proven on PostgreSQL 17, cache/privacy/security boundaries are explicit,
docs match behavior, migrations remain immutable, and review has no blocker.
Handoff to plan 008 includes frozen JSON fixtures, error/translation semantics,
cache headers, pagination, canonical/hreflang/media URLs, and SSR sitemap inputs.
