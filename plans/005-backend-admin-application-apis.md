# Plan 005: Backend admin application APIs

> **Executor instructions**: Implement the complete admin API as one cohesive
> vertical backend wave using RED -> GREEN -> REFACTOR. Tests precede production
> code. This plan assumes no conversation context and forbids scope expansion.

## Status and prerequisites

- **Baseline**: `1932457`; execute from a reviewed successor containing plans
  002, 003, 004, and 007 implementations
- **Priority / effort / risk**: P0 / XXL (15-22 engineer-days) / HIGH
- **Requirements**: FR-ADMIN-001..003, FR-PAGE-001, FR-BLOG-001..005,
  FR-SKILL-001, FR-PORTFOLIO-001, FR-PUBLICATION-001, FR-RESUME-001/002,
  FR-LANDING-002, FR-CONTACT-001, API-003/005..010, NFR-SEC-001..004
- **Depends on**: 002, 003, 004, 007
- **Parallel**: may run with plan 006 after all prerequisites merge, in separate
  worktrees with strict ownership. Conflict paths: `SecurityConfiguration.java`,
  `common/api/*`, `docs/api/api-design.md`, and shared repository query methods.
  Plan 005 owns common error/paging and security changes; merge 005 first, then
  rebase 006 and reconcile the API document.

## Preflight and evidence

Run from repository root:

```powershell
git rev-parse --show-toplevel
git branch --show-current
git rev-parse --short HEAD
git merge-base --is-ancestor 1932457 HEAD
git status --porcelain=v1
git diff --stat 1932457..HEAD -- backend/src/main backend/src/test docs/api docs/architecture/security.md
git cat-file -e HEAD:plans/002-json-session-authentication-api.md
git cat-file -e HEAD:backend/src/main/resources/db/migration/V7__create_featured_content_and_reviewed_indexes.sql
```

STOP on unreviewed in-scope drift. Current baseline evidence: auth security
protects `/api/v1/admin/**` for active `ADMIN`/`SUPER_ADMIN`; plans 003-004
provide entities/repositories; `AuditEvent.record` and repository are the
append-only audit boundary; no admin controllers/services/DTOs/error handler
exist; `docs/api/api-design.md` contains only a partial endpoint sketch.

## API contract

All endpoints are JSON under `/api/v1/admin`, require authenticated ADMIN or
SUPER_ADMIN, require CSRF for mutations, return DTOs only, and use bounded
`page` (0-based), `size` (default 20, max 100), allow-listed `sort`, plus
feature filters. Lists return `{items,page,size,totalElements,totalPages,sort}`.
Create returns `201` with safe DTO and `Location`; get/update/action returns
`200`; successful soft delete returns `204`. Errors use the documented shape:
400 validation with field errors and no rejected values, 401/403 existing
security JSON, 404 generic resource not found, 409 `OPTIMISTIC_LOCK_CONFLICT`,
409 `DUPLICATE_RESOURCE`, 422 `PUBLISHING_RULE_VIOLATION`, 415 unsupported
media (plan 007), and 500 safe internal error. Every response carries request ID;
no stack trace/entity/internal SQL is exposed.

Endpoints (CRUD means `GET collection`, `POST`, `GET /{id}`, `PUT /{id}` with
required `version`, and `DELETE /{id}` soft delete/deactivate where defined):

- `GET /analytics/summary`: dashboard counts by content/contact state, missing-translation/media-alt
  warnings, and recent sanitized audit summaries; no visitor analytics invention.
- CRUD `/pages`; `POST /pages/{id}/publish`; `/archive`.
- CRUD `/blog/categories`, `/blog/tags`, `/blog/posts`; post publish/archive;
  post request owns translation, category, tag IDs, ordered media references.
- CRUD `/skill-categories`, `/skills` (deactivate instead of physical delete).
- CRUD `/projects`; publish/archive; ordered skill IDs.
- CRUD `/publications`; publish/archive.
- CRUD `/resume/entries`; publish/archive; CRUD `/resume/documents` using existing
  media IDs and one-current-document policy.
- CRUD `/featured-items`; activation validates exactly one published target and
  time window; CRUD `/social-links`.
- `GET /contact-messages`, `GET /contact-messages/{id}`,
  `POST /contact-messages/{id}/read`, `/archive`; never return a message body in
  collection rows or audit events.
- Media upload/list/metadata/archive endpoints are owned by plan 007 and merely
  reused, not duplicated here.

Request DTOs use Bean Validation with bounded strings, safe URL schemes, enum
values, locale-keyed translations, UUID references, nonnegative order, coherent
dates, and `long version` on updates. Responses expose IDs, safe fields,
translation completeness, timestamps, state, and version; never lazy entities,
password hashes, internal storage paths, contact data beyond the authorized
detail DTO, or audit details containing content/PII.

## Exact files

**Create common API/audit infrastructure**:

- `backend/src/main/java/ir/tahamohamadi/common/api/ApiErrorResponse.java`
- `backend/src/main/java/ir/tahamohamadi/common/api/FieldValidationError.java`
- `backend/src/main/java/ir/tahamohamadi/common/api/PageResponse.java`
- `backend/src/main/java/ir/tahamohamadi/common/api/GlobalExceptionHandler.java`
- `backend/src/main/java/ir/tahamohamadi/common/api/ResourceNotFoundException.java`
- `backend/src/main/java/ir/tahamohamadi/common/api/PublishingRuleViolationException.java`
- `backend/src/main/java/ir/tahamohamadi/common/audit/AdminAuditService.java`
- `backend/src/main/java/ir/tahamohamadi/analytics/admin/AdminAnalyticsController.java`
- `backend/src/main/java/ir/tahamohamadi/analytics/admin/AdminAnalyticsService.java`
- `backend/src/main/java/ir/tahamohamadi/analytics/admin/AdminAnalyticsSummaryResponse.java`

**Create one controller/service/mapper and request/response/summary DTO set per
aggregate at these exact paths**:

- `backend/src/main/java/ir/tahamohamadi/content/page/api/admin/{AdminPageController.java,AdminPageService.java,AdminPageMapper.java,AdminPageRequest.java,AdminPageResponse.java,AdminPageSummary.java}`
- `backend/src/main/java/ir/tahamohamadi/blog/category/api/admin/{AdminBlogCategoryController.java,AdminBlogCategoryService.java,AdminBlogCategoryMapper.java,AdminBlogCategoryRequest.java,AdminBlogCategoryResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/blog/tag/api/admin/{AdminTagController.java,AdminTagService.java,AdminTagMapper.java,AdminTagRequest.java,AdminTagResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/blog/post/api/admin/{AdminBlogPostController.java,AdminBlogPostService.java,AdminBlogPostMapper.java,AdminBlogPostRequest.java,AdminBlogPostResponse.java,AdminBlogPostSummary.java}`
- `backend/src/main/java/ir/tahamohamadi/skill/api/admin/{AdminSkillCategoryController.java,AdminSkillCategoryService.java,AdminSkillController.java,AdminSkillService.java,AdminSkillMapper.java,AdminSkillCategoryRequest.java,AdminSkillCategoryResponse.java,AdminSkillRequest.java,AdminSkillResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/portfolio/project/api/admin/{AdminProjectController.java,AdminProjectService.java,AdminProjectMapper.java,AdminProjectRequest.java,AdminProjectResponse.java,AdminProjectSummary.java}`
- `backend/src/main/java/ir/tahamohamadi/publication/api/admin/{AdminPublicationController.java,AdminPublicationService.java,AdminPublicationMapper.java,AdminPublicationRequest.java,AdminPublicationResponse.java,AdminPublicationSummary.java}`
- `backend/src/main/java/ir/tahamohamadi/resume/api/admin/{AdminResumeEntryController.java,AdminResumeEntryService.java,AdminResumeEntryMapper.java,AdminResumeEntryRequest.java,AdminResumeEntryResponse.java,AdminResumeDocumentController.java,AdminResumeDocumentService.java,AdminResumeDocumentRequest.java,AdminResumeDocumentResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/content/featured/api/admin/{AdminFeaturedItemController.java,AdminFeaturedItemService.java,AdminFeaturedItemMapper.java,AdminFeaturedItemRequest.java,AdminFeaturedItemResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/content/social/api/admin/{AdminSocialLinkController.java,AdminSocialLinkService.java,AdminSocialLinkRequest.java,AdminSocialLinkResponse.java}`
- `backend/src/main/java/ir/tahamohamadi/content/contact/api/admin/{AdminContactMessageController.java,AdminContactMessageService.java,AdminContactMessageSummary.java,AdminContactMessageResponse.java}`

Braced paths enumerate literal filenames, not optional alternatives.

**Modify** repositories from plans 003-004 only for required bounded filters;
`backend/src/main/java/ir/tahamohamadi/auth/security/SecurityConfiguration.java`
only if matcher/role behavior needs an explicit regression-preserving update;
`docs/api/api-design.md`; `docs/architecture/security.md` for authorization and
audit boundaries. **Create tests**:

- `backend/src/test/java/ir/tahamohamadi/admin/AdminAuthorizationIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/admin/AdminApiIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/admin/AdminAuditIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/admin/AdminValidationUnitTest.java`

No migration or POM change is expected.

## RED -> GREEN -> REFACTOR

**RED**: write focused unit validation tests and PostgreSQL 17 MockMvc integration
tests first. Capture missing endpoint/service failures. Cover every endpoint's
happy path and invalid body; unauthenticated 401; non-admin 403; ADMIN and
SUPER_ADMIN access; CSRF 403 on every mutation; paging size/sort allow-list;
filter/state/locale/order; entity non-exposure; duplicate/404/409/422 mapping;
stale version leaves data unchanged; publish requires timestamps/required
translations/SEO and active references; soft-deleted rows disappear from normal
lists and cannot be assigned; contact transition rules; sanitized audit event
for every create/update/state/delete with actor/action/target/outcome and no
password, session, CSRF, email, contact body, Markdown body, or raw request.

**GREEN**: implement thin controllers, transactional services that own
authorization-sensitive invariants, explicit mappers, repository projections,
and centralized safe errors. Audit in the same transaction as successful writes;
record failure audits only where policy can do so without rolling them back or
leaking input. Exact action names:
`ADMIN_<PAGE|BLOG_CATEGORY|TAG|BLOG_POST|SKILL_CATEGORY|SKILL|PROJECT|PUBLICATION|RESUME_ENTRY|RESUME_DOCUMENT|FEATURED_ITEM|SOCIAL_LINK|CONTACT_MESSAGE>_<CREATED|UPDATED|PUBLISHED|ARCHIVED|DELETED|READ>`.
Details contain only changed field names and state transition metadata.

**REFACTOR**: remove demonstrated duplication without a generic CRUD framework;
inspect query counts/paging, update approved docs, then rerun all security and
auth regressions.

## Verification

From `backend/`:

```powershell
.\mvnw.cmd -Dtest=AdminValidationUnitTest test
.\mvnw.cmd -Dtest=AdminAuthorizationIntegrationTest test
.\mvnw.cmd -Dtest=AdminApiIntegrationTest test
.\mvnw.cmd -Dtest=AdminAuditIntegrationTest test
.\mvnw.cmd -Dtest=SecurityConfigurationUnitTest,SessionSecurityIntegrationTest test
.\mvnw.cmd test
```

Expected: BUILD SUCCESS, zero failures/errors/skips, PostgreSQL
`postgres:17-alpine` with no H2, all old tests green, no generated security user, no
migration executed beyond V7. Run `git diff --check`, verify V1-V7 checksums and
no new migration, inspect logs for sensitive values, and verify `pom.xml` and
frontend unchanged. Performance: max page size, stable ordering, projections,
query-count assertions for representative lists. Accessibility is API-neutral;
validation field keys and translation-status data must support accessible forms.

## Exclusions and STOP conditions

Excluded: public APIs, upload implementation, frontend, user/role management,
editor role (not present in V1), analytics collection, revisions, scheduling
worker, hard purge, rate-limit infrastructure, OpenAPI dependency if not already
approved, migrations, seeds, and unrelated refactors.

STOP on dirty/material drift, missing prerequisite, schema mismatch, need for a
new migration/dependency, proposal to weaken CSRF/session rules, unresolved role
matrix beyond ADMIN/SUPER_ADMIN, inability to keep audit sanitized/transactional,
or integration tests unavailable on PostgreSQL 17. Do not silently add `EDITOR`.

## Definition of Done and handoff

All documented admin workflows are authorized, validated, paged, audited,
optimistically locked, soft-delete aware, DTO-only, documented, and green in the
full suite; migrations are unchanged; review gate has no blocker. Handoff to
plan 009 includes the exact OpenAPI-equivalent JSON examples, filters/sorts,
field-error keys, conflict semantics, and CSRF/session preconditions.
