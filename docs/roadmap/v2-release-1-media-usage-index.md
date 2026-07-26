# V2 Release 1: Normalized media usage index

**Status:** Implemented; focused backend verification passed  
**Date:** 2026-07-26  
**Scope:** R1.4 media usage and lifecycle safety

## Change

Flyway migration `V11__create_media_usage_index.sql` introduces the normalized
`media_usage` index. Each row identifies the media asset, owner type, owner ID,
and reference key. The migration backfills page Open Graph, site identity,
Composer `mediaId`, blog cover/inline/attachment, portfolio cover,
publication cover, and resume document references.

`MediaReferenceService` refreshes the derived index from the authoritative
domain tables before usage, archive eligibility, public exposure, and replace
operations. It therefore includes legacy rows and prevents a Composer JSON
substring from being treated as a media reference unless it is the approved
`mediaId` setting and resolves to an existing asset.

## Safety and compatibility

- Existing Admin endpoints and DTOs remain additive and unchanged.
- Archive remains blocked while an index entry exists.
- Replace updates every supported source reference, then refreshes the index
  before archiving the source asset.
- Public media access still requires an active asset with a published/public
  consumer.
- Rollback is application rollback plus restoration from backup; Flyway
  migrations are forward-only and the index can be reconstructed from source
  tables.

## Evidence

```powershell
cd backend
.\mvnw.cmd '-Dtest=MediaUploadIntegrationTest,MediaOrphanReportServiceUnitTest' test
```

Passed on 2026-07-26: 4 tests, including Composer reference indexing, archive
rejection, and replacement propagation; PostgreSQL 17 Testcontainers applied
all 11 Flyway migrations. `git diff --check` also passed after the change.

## Follow-up

The index is refreshed at the media safety boundary. Release 2 may replace the
full refresh with narrowly scoped synchronization as the Section aggregate
becomes the new authoritative Composer write model.
