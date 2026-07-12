# Plan 003: Database and persistence wave A

> **Executor instructions**: This is a self-contained RED -> GREEN -> REFACTOR
> implementation handoff. Start from the stated baseline or a reviewed successor,
> create tests before production code, and stop on every STOP condition. Do not
> modify an applied migration.

## Status and dependencies

- **Baseline**: `1932457` on 2026-07-11
- **Priority / effort / risk**: P0 / XL (8-12 engineer-days) / HIGH
- **Requirements**: DB-001, DB-002, FR-PAGE-001, FR-BLOG-001..005,
  FR-CONTACT-001, FR-MEDIA-001, NFR-I18N-001, NFR-SEC-003
- **Depends on**: plan 001 is already implemented; no dependency on plan 002
- **May run in parallel**: only with frontend contract/shell work in a separate
  worktree. It must not run with plan 004 because migration numbering and shared
  persistence conventions are sequential.

## Preflight and current-state evidence

Run from repository root:

```powershell
git rev-parse --show-toplevel
git branch --show-current
git rev-parse --short HEAD
git status --porcelain=v1
git diff --stat 1932457..HEAD -- backend/src/main backend/src/test backend/src/main/resources/db/migration docs/database
git cat-file -e HEAD:backend/src/main/resources/db/migration/V1__create_identity_and_audit_foundation.sql
```

Expected root is `D:/Project/Taha/tahamohamadi-ir`, baseline is `1932457` or a
reviewed successor, and status/drift are empty. Current evidence:

- `backend/src/main/resources/db/migration/` contains only immutable V1.
- `backend/src/main/java/ir/tahamohamadi/{media,content,blog}/package-info.java`
  are placeholders; no target entity or repository exists.
- `backend/src/test/java/ir/tahamohamadi/FlywayV1IdentityAuditIntegrationTest.java`
  is the PostgreSQL 17/Testcontainers pattern to reuse.
- `docs/database/data-model.md` sections 5 and 26-35 bind table structure,
  locale/status checks, UUIDv4 IDs, soft deletion, optimistic locking, indexes,
  FKs, FTS, and migration order.

## Scope and schema contract

Create forward-only migrations, exactly in this order:

1. `V2__create_media_asset_foundation.sql`: `media_asset`,
   `media_asset_translation`.
2. `V3__create_managed_pages_and_contact.sql`: `content_page`,
   `content_page_translation`, `social_link`, `contact_message`.
3. `V4__create_blog_content_and_search.sql`: `blog_category`,
   `blog_category_translation`, `blog_post`, `blog_post_translation`, `tag`,
   `tag_translation`, `blog_post_tag`, `blog_post_media`.

Implement every column, named PK/FK/unique/check constraint, delete action, and
documented index in `docs/database/data-model.md`. Translation tables accept
only `fa`/`en`; parent+locale is unique; locale+lower(slug) is partially unique
for nondeleted rows. Mutable aggregates/translations have audit columns and
`version`; IDs are application-generated UUIDv4 before persistence. Media bytes
remain outside PostgreSQL. Blog `search_vector` is maintained from localized
title/excerpt/body with `english` for English and `simple` for Persian and has
the only MVP GIN index. Do not add `pg_trgm`.

Persistence behavior must provide active/nondeleted lookup methods, deterministic
ordering, public published+locale queries, and admin pageable queries without
exposing entities. Repositories use Spring Data JPA; no native query unless the
PostgreSQL FTS expression cannot be expressed safely otherwise.

## Exact implementation files

**Create migrations and shared enums**:

- `backend/src/main/resources/db/migration/V2__create_media_asset_foundation.sql`
- `backend/src/main/resources/db/migration/V3__create_managed_pages_and_contact.sql`
- `backend/src/main/resources/db/migration/V4__create_blog_content_and_search.sql`
- `backend/src/main/java/ir/tahamohamadi/common/domain/ContentStatus.java`
- `backend/src/main/java/ir/tahamohamadi/common/domain/LanguageCode.java`

**Create media files**:

- `backend/src/main/java/ir/tahamohamadi/media/asset/MediaAsset.java`
- `backend/src/main/java/ir/tahamohamadi/media/asset/MediaAssetStatus.java`
- `backend/src/main/java/ir/tahamohamadi/media/asset/MediaAssetRepository.java`
- `backend/src/main/java/ir/tahamohamadi/media/asset/MediaAssetTranslation.java`
- `backend/src/main/java/ir/tahamohamadi/media/asset/MediaAssetTranslationRepository.java`

**Create page/contact files**:

- `backend/src/main/java/ir/tahamohamadi/content/page/ContentPage.java`
- `backend/src/main/java/ir/tahamohamadi/content/page/ContentPageRepository.java`
- `backend/src/main/java/ir/tahamohamadi/content/page/ContentPageTranslation.java`
- `backend/src/main/java/ir/tahamohamadi/content/page/ContentPageTranslationRepository.java`
- `backend/src/main/java/ir/tahamohamadi/content/social/SocialLink.java`
- `backend/src/main/java/ir/tahamohamadi/content/social/SocialLinkRepository.java`
- `backend/src/main/java/ir/tahamohamadi/content/contact/ContactMessage.java`
- `backend/src/main/java/ir/tahamohamadi/content/contact/ContactMessageStatus.java`
- `backend/src/main/java/ir/tahamohamadi/content/contact/ContactMessageRepository.java`

**Create blog files**:

- `backend/src/main/java/ir/tahamohamadi/blog/category/BlogCategory.java`
- `backend/src/main/java/ir/tahamohamadi/blog/category/BlogCategoryRepository.java`
- `backend/src/main/java/ir/tahamohamadi/blog/category/BlogCategoryTranslation.java`
- `backend/src/main/java/ir/tahamohamadi/blog/category/BlogCategoryTranslationRepository.java`
- `backend/src/main/java/ir/tahamohamadi/blog/post/BlogPost.java`
- `backend/src/main/java/ir/tahamohamadi/blog/post/BlogPostRepository.java`
- `backend/src/main/java/ir/tahamohamadi/blog/post/BlogPostTranslation.java`
- `backend/src/main/java/ir/tahamohamadi/blog/post/BlogPostTranslationRepository.java`
- `backend/src/main/java/ir/tahamohamadi/blog/post/BlogPostMedia.java`
- `backend/src/main/java/ir/tahamohamadi/blog/post/BlogPostMediaId.java`
- `backend/src/main/java/ir/tahamohamadi/blog/post/BlogPostMediaRepository.java`
- `backend/src/main/java/ir/tahamohamadi/blog/post/BlogPostMediaUsage.java`
- `backend/src/main/java/ir/tahamohamadi/blog/tag/Tag.java`
- `backend/src/main/java/ir/tahamohamadi/blog/tag/TagRepository.java`
- `backend/src/main/java/ir/tahamohamadi/blog/tag/TagTranslation.java`
- `backend/src/main/java/ir/tahamohamadi/blog/tag/TagTranslationRepository.java`
- `backend/src/main/java/ir/tahamohamadi/blog/tag/BlogPostTag.java`
- `backend/src/main/java/ir/tahamohamadi/blog/tag/BlogPostTagId.java`
- `backend/src/main/java/ir/tahamohamadi/blog/tag/BlogPostTagRepository.java`

**Create tests; modify documentation only after behavior is green**:

- `backend/src/test/java/ir/tahamohamadi/persistence/FlywayWaveAIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/persistence/WaveARepositoryIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/persistence/FlywayUpgradeIntegrationTest.java`
- `docs/database/database-design.md`
- `docs/database/data-model.md` (implementation status only; no redesign)

Do not modify `backend/pom.xml`; required dependencies already exist. Do not
modify V1 or any plan file.

## RED -> GREEN -> REFACTOR

### RED

Write the three integration tests first. Against the unchanged application they
must fail because V2-V4 tables, entities, and repositories are absent. Record
the failing test names and missing relation/bean/compiler reason; failures from
Docker, networking, or environment setup do not count as RED.

Test scenarios: fresh migrate yields versions 1-4 and exactly the documented
tables; V1 checksum is unchanged; migrate called twice executes zero migrations;
Flyway validate succeeds; migration from a V1-only database succeeds without
data loss; all FK/check/unique/partial-unique constraints reject invalid data;
media reference deletion is restricted; translation locale/slug uniqueness and
soft-delete reuse work; optimistic versions increment; public repository queries
exclude draft/archived/deleted/missing-locale rows; ordering is stable; contact
status transitions persist; blog tag/media mappings reject duplicates; English
and Persian FTS return only matching published localized posts and use the GIN
index (verify with `EXPLAIN`, disabling sequential scan only inside the test).

### GREEN

Implement migrations first, then minimal enums/entities/repositories. Follow the
existing `AppUser` conventions: protected JPA constructor, controlled factories
and domain transitions, no `@Data`, ID-based equality, association-free safe
`toString`, explicit column/FK names, and no entity serialization. Keep Markdown
as stored text; sanitization belongs at rendering/service boundaries.

### REFACTOR

Remove query duplication only where a named repository contract is clearer;
retain feature packages and explicit mappings. Run formatter only if the
repository already defines one (none currently); do not add a plugin. Update the
two database docs to state V2-V4 are implemented while preserving decisions.

## Verification gates

Run from `backend/`, in order:

```powershell
.\mvnw.cmd -Dtest=FlywayWaveAIntegrationTest test
.\mvnw.cmd -Dtest=WaveARepositoryIntegrationTest test
.\mvnw.cmd -Dtest=FlywayUpgradeIntegrationTest test
.\mvnw.cmd -Dtest=FlywayV1IdentityAuditIntegrationTest test
.\mvnw.cmd test
```

Expected: every command `BUILD SUCCESS`, zero failures/errors/skips, container
image `postgres:17-alpine`, Hibernate `ddl-auto=validate`, Flyway versions 1-4
successful, and the existing suite green. Then from root:

```powershell
git diff --check
git diff --exit-code 1932457 -- backend/src/main/resources/db/migration/V1__create_identity_and_audit_foundation.sql
git status --short
Get-ChildItem backend/src/main/resources/db/migration | Select-Object -ExpandProperty Name
```

Expected migration list is V1, V2, V3, V4 only. Security checks: no secrets or
contact bodies in logs/toString, no entity exposure, bounded repository paging,
no raw upload bytes, and no password/session changes. Performance checks cover
documented indexes, stable tie-breakers, no eager collection fan-out, and FTS
query plans. Accessibility is not applicable to persistence; preserve localized
alt metadata and explicit missing-translation semantics for later UI work.

## Exclusions and STOP conditions

Excluded: V5-V7, APIs/controllers/DTOs, file upload/storage, UI, seeds,
credentials, H2, `pg_trgm`, revision history, redirects, automatic featured
selection, contact rate limiting, and unrelated refactors.

STOP if the tree is dirty or baseline has in-scope drift; V1 checksum differs;
approved column/constraint behavior is ambiguous; a new dependency appears
necessary; PostgreSQL 17/Docker is unavailable; tests would require H2; a
migration number is already occupied; Persian normalization requires an
unapproved extension; or implementation requires editing prior migrations.

## Definition of Done and handoff

V2-V4 migrate fresh and from V1, validate/idempotently re-run, all mapped
entities validate against PostgreSQL 17, repository/integrity/FTS tests pass,
V1 is byte-for-byte unchanged, docs reflect actual implementation, and the
review gate reports no unresolved blocker. After merge, plan 004 may start;
plan 007 may also branch from this merge. Handoff must state migration checksums,
table/index list, repository query semantics, and any deliberately unresolved
open decision.
