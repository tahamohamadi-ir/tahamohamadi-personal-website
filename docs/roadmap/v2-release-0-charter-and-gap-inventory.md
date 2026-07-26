# V2 Release 0 Charter and Gap Inventory

**Status:** Active — R0.1 baseline established  
**Date:** 2026-07-25  
**Owner:** Site engineering  
**Parent documents:** `docs/master-plan.md`, `plans/008-public-frontend-mvp.md`, `docs/roadmap/v2-home-2-delta.md`  
**Input plans:** `docs/tahamohamadi_site_cms_v2_development_plan.md`, `docs/tahamohamadi_site_cms_v2_gap_based_implementation_plan.md`

## Purpose

This charter converts the supplied V2 roadmap into incremental, reversible
releases. It is deliberately not a rewrite authorization. The existing typed,
relational composer, DTO-backed APIs, independent `fa`/`en` content, public
SSR, optimistic locking, and publish-state rules remain the baseline.

## Requirement traceability

| Source | Requirement or decision | R0 treatment |
|---|---|---|
| Master plan | `FR-LANDING-001`, `FR-LANDING-002` | Home 2.0 may improve presentation only through the existing public response and renderer. |
| Master plan | `CMS-001` to `CMS-004`, `CMS-006` | Published-only public content, locale awareness, alt-text discipline, and public/preview parity remain non-negotiable. |
| Master plan | `DEC-003`, `DEC-005`, `DEC-006` | Vue/Quasar, lightweight custom CMS, and SSR/hybrid public rendering remain in force. |
| Plan 008 | M1 public frontend boundaries | No backend contract, implementation, migration, or new Admin work is authorized by this charter. |
| V2 gap plan | R0.1 through R0.5 | Establish governance and evidence, then finish only the bounded Home 2.0 release. |

## Dirty-worktree classification

All paths below were already dirty when this charter began. They are
**pre-existing** for this task and must not be overwritten, staged, or amended
without explicit owner confirmation.

| Path | Classification | Observed relation to V2 | Action in this task |
|---|---|---|---|
| `docs/roadmap/roadmap.md` | pre-existing | Links an incremental Home 2.0 roadmap delta. | Preserve. |
| `docs/roadmap/v2-home-2-delta.md` | pre-existing | Defines the adopted, bounded Home 2.0 scope. | Treat as controlling release scope. |
| `docs/tahamohamadi_site_cms_v2_development_plan.md` | pre-existing | Supplied V2 source plan. | Preserve as input. |
| `docs/tahamohamadi_site_cms_v2_gap_based_implementation_plan.md` | pre-existing | Supplied gap-based execution plan. | Preserve as input. |
| `frontend/src/components/public/PageBlockRenderer.vue` | pre-existing | Implements Home 2.0 renderer behavior. | Review and verify only. |
| `frontend/test/vitest/__tests__/public-pages.spec.js` | pre-existing | Adds Home 2.0 coverage. | Review and verify only. |

This file is the sole task-owned artifact for R0.1.

## Authorized release sequence

1. **Release 0 — governance and Home 2.0:** document baseline, review the
   pre-existing Home changes, and collect focused verification evidence.
2. **Release 1 — media picker/library:** requires a separately approved Admin
   and backend charter before any contract or schema change.
3. **Releases 2–7:** sections/preview, article editing, case studies,
   workflow, operations, and rollout remain deferred until their own scoped
   plans, security review, migration plan, and rollback evidence exist.

## Release 0 technical boundary

Allowed Home 2.0 composition uses only existing public block fields:
`type`, `enabled`, `title`, `eyebrow`, `lead`, `actionLabel`, `actionPath`,
`mediaId`, `mediaUrl`, `alt`, `source`, and `limit`.

The only allowed Home sections are hero, research focus, selected work,
featured publications, latest writing, and contact CTA. Renderer behavior must
keep exactly one H1, omit empty collections, render hero media only with
same-locale approved alt text, allow only `/fa...`, `/en...`, or `https://`
CTAs, and avoid cross-locale fallback.

No feature flag is introduced in R0 because the existing implementation has no
approved flag mechanism and adding one would broaden the current public
frontend scope. Rollback for this release is a focused revert of the
task-owned Home change after review; no data migration or API rollback is
involved.

## Repository inventory and V2 gap matrix

| V2 need | Current evidence | Status | Next action |
|---|---|---|---|
| Typed relational page composition | Flyway `V9__create_site_composer_foundation.sql`; public `PageBlockRenderer.vue`. | exists | Keep as source of truth. |
| Localized footer/content foundation | Flyway `V10__add_localized_footer_content.sql`. | exists | Preserve independent locales. |
| Home hero, collection, CTA, and localized alt safeguards | Pre-existing renderer and `public-pages.spec.js` changes. | partial | Run focused test and manual visual/a11y gate; do not modify until ownership is confirmed. |
| Public SSR page shell and SEO | Plan 008 and public route/SEO test suite exist. | exists | Verify Home regression only in R0. |
| Admin page, block, and media APIs | `AdminPageController`, `AdminPageBlockController`, and `AdminMediaController` exist. | partial | Inventory exact DTO/pagination/lifecycle behavior before Release 1; no API changes now. |
| Searchable, shared media picker | Admin media and settings/pages screens exist, but this charter records no shared picker proof. | missing | Separate Release 1 UX/API plan. |
| Sections, device preview, undo/redo, autosave | Not part of the Home delta. | deferred | Separate Release 2 plan with keyboard, conflict, preview-token, and migration design. |
| Block article editor | Not part of the Home delta. | deferred | Time-boxed ADR/spike before dependency or storage changes. |
| Revisions, scheduling, translation freshness | Not part of the Home delta. | deferred | Separate lifecycle/revision release with authorization and audit tests. |
| Variants, focal points, bulk media actions | Not proven by current inventory. | deferred | Storage and migration spike before commitment. |

## R0.3 review findings

| Finding | Evidence | Release impact | Required resolution |
|---|---|---|---|
| A provided `canonicalPath` is accepted when it is any internal localized path, not specifically a path for `props.locale`. | `collectionPath()` calls `isInternalPath(item.canonicalPath)` before it falls back to the current-locale slug route. | The renderer does not independently guarantee the Home delta's same-locale collection-link rule for malformed or cross-locale API data. | After explicit ownership confirmation, add a locale-bound guard and a focused regression for a cross-locale canonical path. |
| Renderer-only data cannot establish that non-empty hero `alt` text belongs to the page locale. | `heroMediaUrl()` checks for non-empty `alt`; the block shape has no locale marker for that field. | Same-locale alt text remains a content/API and manual-release gate, not a renderer-proven property. | Verify the public response fixture and real CMS content in both locales before release. |

## Verification and acceptance evidence

Before Release 0 can be closed, record the result of:

```powershell
cd frontend
npm.cmd run test:unit -- public-pages.spec.js
npm.cmd run test:unit -- page-block-renderer.spec.js public-home-redesign.spec.js
npm.cmd run test:unit
npm.cmd run build

cd ..
git diff --check
git diff --exit-code -- backend/src/main/resources/db/migration
```

Manual release gates remain required at `/fa` and `/en` for 375, 768, 1024,
and 1440 px: real approved content only; RTL/LTR; exactly one H1; visible focus;
44 px targets; contrast; reduced motion; no overflow or layout shift; and no
cross-locale fallback. Required portrait, project, publication, and Open Graph
assets must be approved in both locales; otherwise the relevant section is
omitted or the release is held.

## Entry criteria for later releases

Before any Admin or backend V2 change, the owner must approve a release-specific
charter that names the DTO/API delta, authorization and CSRF behavior, audit
events, optimistic-lock handling, Flyway migration and rollback path, tests,
and feature-flag/rollout plan. This is the explicit gate required by R0.1.
