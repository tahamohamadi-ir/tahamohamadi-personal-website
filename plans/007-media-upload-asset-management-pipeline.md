# Plan 007: Media upload and asset-management pipeline

> **Executor instructions**: Execute this bounded media vertical slice using RED
> -> GREEN -> REFACTOR. Tests are written first. Treat uploaded input as hostile
> and stop before weakening validation or storage containment.

## Status and dependencies

- **Baseline**: `1932457`; execute after plan 003; reconcile plan 004 if parallel
- **Priority / effort / risk**: P0 / L (6-9 engineer-days) / HIGH
- **Requirements**: FR-MEDIA-001, FR-RESUME-002, ADR-008, NFR-SEC-003
- **Depends on**: 001, 003; plan 002 is required before browser admin use but
  not for the isolated service implementation
- **Parallel**: may run with plan 004 in separate worktrees. Conflict paths:
  `media/asset/*`, `docs/database/data-model.md`, and migration directory.
  Plan 007 must not create a migration; plan 004 owns V5-V7.

## Preflight and current evidence

Run from repository root:

```powershell
git rev-parse --show-toplevel
git branch --show-current
git rev-parse --short HEAD
git merge-base --is-ancestor 1932457 HEAD
git status --porcelain=v1
git diff --stat 1932457..HEAD -- backend/src/main/java/ir/tahamohamadi/media backend/src/test/java/ir/tahamohamadi/media backend/src/main/resources compose.yaml docs
git cat-file -e HEAD:backend/src/main/resources/db/migration/V2__create_media_asset_foundation.sql
```

Require reviewed plan 003 drift and no unrelated changes. ADR-008 binds MVP production to local
filesystem behind an abstraction; `media_asset`/translation persist metadata
only; `compose.yaml` has no media volume yet; no upload controller/storage service
exists. Existing security already protects `/api/v1/admin/**` and CSRF.

## Contract and security policy

- `POST /api/v1/admin/media` multipart: required `file`, optional `faAlt`,
  `faCaption`, `enAlt`, `enCaption`; ADMIN/SUPER_ADMIN and CSRF; `201` safe DTO.
- `GET /api/v1/admin/media` paged/filterable; `GET /{id}`; `PUT /{id}/metadata`
  with `version`; `DELETE /{id}` archives/soft-deletes only after reference check.
- `GET /api/v1/public/media/{id}` streams only ACTIVE referenced/public-eligible
  files with content type, length, ETag/checksum, nosniff, and conservative cache
  headers; never takes a storage path/key from the client.

Initial allow-list, subject to a human decision before implementation: images
`image/jpeg`, `image/png`, `image/webp`; documents `application/pdf`; max image
10 MiB, PDF 20 MiB. Extension, declared MIME, signature/content detection, and
decoded image format must agree. Reject SVG, HTML, scripts, archives, executable
content, polyglots that cannot be confidently classified, zero-byte, oversized,
malformed images/PDFs, path separators, and control characters. Generate a UUID
storage key and canonical extension; preserve sanitized original filename only
as metadata. Never execute uploaded content or serve it inline when attachment is
safer. Compute SHA-256 streaming; checksum deduplication is advisory, not unique,
unless a human explicitly changes the data-model decision.

Storage operations use a configured root resolved to an absolute normalized path;
every target must remain beneath it. Write to an application-owned temporary file,
validate, then atomically move when supported; clean temporary/partial data on
failure. Database/file consistency uses compensating deletion and explicit orphan
reconciliation, not a distributed transaction. Referenced assets cannot be
deleted; orphan report means no FK/junction reference and remains admin-only.
Do not log bytes, filenames supplied by users, paths, checksums tied to private
assets, cookies, CSRF/session IDs, or request bodies.

## Exact files

**Create production files**:

- `backend/src/main/java/ir/tahamohamadi/media/config/MediaStorageProperties.java`
- `backend/src/main/java/ir/tahamohamadi/media/config/MediaConfiguration.java`
- `backend/src/main/java/ir/tahamohamadi/media/storage/MediaStorage.java`
- `backend/src/main/java/ir/tahamohamadi/media/storage/StoredMedia.java`
- `backend/src/main/java/ir/tahamohamadi/media/storage/LocalFileSystemMediaStorage.java`
- `backend/src/main/java/ir/tahamohamadi/media/validation/MediaPolicy.java`
- `backend/src/main/java/ir/tahamohamadi/media/validation/MediaValidationService.java`
- `backend/src/main/java/ir/tahamohamadi/media/service/MediaAssetService.java`
- `backend/src/main/java/ir/tahamohamadi/media/service/MediaReferenceService.java`
- `backend/src/main/java/ir/tahamohamadi/media/service/MediaOrphanReportService.java`
- `backend/src/main/java/ir/tahamohamadi/media/api/admin/AdminMediaController.java`
- `backend/src/main/java/ir/tahamohamadi/media/api/admin/MediaMetadataRequest.java`
- `backend/src/main/java/ir/tahamohamadi/media/api/admin/MediaAssetResponse.java`
- `backend/src/main/java/ir/tahamohamadi/media/api/admin/MediaAssetSummary.java`
- `backend/src/main/java/ir/tahamohamadi/media/api/admin/MediaOrphanResponse.java`
- `backend/src/main/java/ir/tahamohamadi/media/api/publicsite/PublicMediaController.java`
- `backend/src/main/java/ir/tahamohamadi/media/api/MediaUploadException.java`

**Modify**:

- `backend/src/main/java/ir/tahamohamadi/media/asset/MediaAsset.java`
- `backend/src/main/java/ir/tahamohamadi/media/asset/MediaAssetRepository.java`
- `backend/src/main/java/ir/tahamohamadi/media/asset/MediaAssetTranslation.java`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-local.yml`
- `compose.yaml` (named media volume/mount and environment only)
- `.env.example` (nonsecret storage settings)
- `docs/api/api-design.md`
- `docs/architecture/architecture.md`
- `docs/architecture/security.md`

**Create tests**:

- `backend/src/test/java/ir/tahamohamadi/media/MediaValidationUnitTest.java`
- `backend/src/test/java/ir/tahamohamadi/media/LocalFileSystemMediaStorageUnitTest.java`
- `backend/src/test/java/ir/tahamohamadi/media/MediaUploadIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/media/MediaReferenceIntegrationTest.java`

No POM change is expected. Use JDK/ImageIO and minimal signature checks already
available; STOP before adding Apache Tika, PDF parser, antivirus client, or cloud
SDK. No migration is required because V2 contains all metadata.

## RED -> GREEN -> REFACTOR

**RED**: write unit and PG17/MockMvc integration tests first; capture missing
types/endpoints. Unit cases cover traversal (`..`, absolute, UNC, alternate
separators), symlink escape, filename/control chars, size enforcement while
streaming, signature/MIME/extension mismatch, malformed image, temp cleanup,
atomic move fallback, duplicate checksum advisory behavior, and containment.
Integration covers auth/role/CSRF, valid JPEG/PNG/WebP/PDF, 400/413/415 safe
errors, metadata locales/optimistic conflict, response secrecy, public active
stream headers, private/archived 404, referenced delete 409, unreferenced archive,
orphan report, compensating cleanup on DB failure, and no sensitive log output.

**GREEN**: implement configuration-bound limits, validation before activation,
storage abstraction/local adapter, transactional metadata service, compensating
cleanup, reference checks across all V2-V7 FKs/junctions, and thin controllers.
Audit upload/metadata/archive with actor/asset ID/outcome and sanitized media type/
size category only; never filename/path/checksum/content.

**REFACTOR**: keep one storage interface and one local implementation, centralize
policy without a framework, inspect streaming/memory usage, document operational
volume ownership/backup, and rerun regression tests.

## Verification

From `backend/`:

```powershell
.\mvnw.cmd -Dtest=MediaValidationUnitTest test
.\mvnw.cmd -Dtest=LocalFileSystemMediaStorageUnitTest test
.\mvnw.cmd -Dtest=MediaUploadIntegrationTest test
.\mvnw.cmd -Dtest=MediaReferenceIntegrationTest test
.\mvnw.cmd -Dtest=SecurityConfigurationUnitTest,SessionSecurityIntegrationTest test
.\mvnw.cmd test
```

Expected BUILD SUCCESS, zero failures/errors/skips, integration uses
`postgres:17-alpine` with no H2, and temp directories are empty after tests. Then
`git diff --check`; verify V1-V7 unchanged/no migration; inspect compose config
without starting/building in the implementation review; verify frontend/POM
unchanged. Security gate includes traversal/symlink/signature/CSRF/access matrix
and secret-log scan. Performance gate proves streaming (no whole-file byte array),
bounded memory, checksummed single pass where practical. Accessibility contract
requires alt/caption completeness status and explicit decorative usage.

## Exclusions, decisions, STOP conditions

Excluded: S3/cloud, CDN, transforms/thumbnails, SVG, video/audio, antivirus
infrastructure, public arbitrary storage keys, hard purge, automatic dedupe,
frontend implementation, migrations, and upload seeds.

Human decisions: approve MIME/size list; confirm public media authorization model;
confirm backup/retention for volume; decide whether production needs a malware
scanner before accepting PDFs. STOP on dirty/drift, missing V2, need for schema or
dependency, inability to validate content consistently, storage root outside an
approved volume, symlink containment uncertainty, or requirement for cloud/
malware infrastructure. Reject the type rather than claiming unsupported safety.

## Definition of Done and handoff

Authorized uploads are validated/streamed/stored atomically, metadata and locale
text persist, public delivery is ID-based and safe, references/orphans/deletion
are enforced, failures clean up, docs/config/tests are green, migrations remain
immutable, and review has no blocker. Handoff to plans 005/006/009 lists multipart
contract, limits, safe DTO/URL, error codes, alt requirements, and volume runbook.
