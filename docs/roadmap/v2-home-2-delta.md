# V2 Home 2.0 Delta

**Status:** Adopted as an incremental roadmap delta  
**Version:** 0.1  
**Decision date:** 2026-07-25  
**Parent documents:** `docs/master-plan.md`, `plans/008-public-frontend-mvp.md`, `docs/design.md`  
**Reference input:** `docs/tahamohamadi_site_cms_v2_development_plan.md`

## Decision

The supplied V2 plan is adopted as a phased roadmap input, not as a rewrite of
the existing CMS or public frontend.

The current typed relational composer remains the source of truth:

- typed page blocks;
- independent Persian and English content;
- SSR public rendering;
- relational persistence and DTO-backed API contracts;
- optimistic locking and existing publish states.

Release 1 is limited to Home 2.0 on top of the existing public Home response and
shared public block renderer.

## Non-adopted V2 proposals for Release 1

The following V2 ideas are not part of Home 2.0 and require separate future
plans before implementation:

- document-wide JSONB page composition;
- new backend page-builder contracts;
- new admin composer endpoints;
- migrations for sections, revisions, scheduling, workflow, media variants, or
  focal points;
- drag/drop, autosave, undo/redo, preview devices, and review workflow;
- article block editor and portfolio case-study builder.

JSON may be considered later only for constrained block settings or revision
snapshots, and only with an explicit backend plan and migration review.

## Release 1 scope: Home 2.0

Home 2.0 may use only the current public API response and existing block fields:

- `type`
- `enabled`
- `title`
- `eyebrow`
- `lead`
- `actionLabel`
- `actionPath`
- `mediaId`
- `mediaUrl`
- `alt`
- `source`
- `limit`

Allowed composition:

- bilingual hero;
- research focus;
- selected work;
- featured publications;
- latest writing;
- contact CTA.

Renderer rules:

- SSR output must stay complete and hydration-safe.
- The route must expose exactly one H1.
- No public page may own `main`, `lang`, or `dir`; `PublicLayout` owns shell
  landmarks and document direction.
- Hero media renders only with approved same-locale alt text.
- Empty collection sections are omitted rather than replaced with placeholder
  content.
- CTA links are limited to `/fa...`, `/en...`, or `https://...`.
- Collection detail links use safe API `canonicalPath` first, otherwise build a
  route from the current locale and same-locale slug.
- No cross-locale content fallback is allowed.
- No fake copy, fake metrics, fake achievements, placeholder visuals, generic
  gradients, decorative animation, or unapproved assets may be introduced.

## Release gates

Home 2.0 is not releasable until all of the following are true:

- approved portrait or hero asset with Persian and English alt text;
- approved project asset/content for selected work;
- approved publication content;
- approved Open Graph asset;
- equivalent Persian and English Home content;
- manual `/fa` and `/en` checks at 375, 768, 1024, and 1440 px;
- RTL/LTR, overflow, focus order, contrast, 44 px targets, reduced motion, one
  H1, hero alt text, and layout-shift checks pass against real CMS content.

If any required content or asset is missing, the section must be omitted or the
release must be held. Placeholder publication is not allowed.

## Verification evidence expected for implementation

Run from `frontend/` unless noted otherwise:

```powershell
npm.cmd run test:unit -- public-pages.spec.js
npm.cmd run test:unit -- page-block-renderer.spec.js public-home-redesign.spec.js
npm.cmd run test:unit
npm.cmd run build
```

Run from the repository root:

```powershell
git diff --check
git diff --exit-code -- backend/src/main/resources/db/migration
```

If Windows blocks the SSR build while removing `frontend/dist/ssr` with `EPERM`,
rerun the same build with the required filesystem permission and do not change
source code or configuration to work around the operating-system lock.

## Follow-on order

After Home 2.0 stabilizes and release gates are satisfied:

1. Scope portfolio case studies with only required metadata and composition
   relations.
2. Replace capped admin media dropdowns with a shared searchable, previewable
   media picker.
3. Extend the existing composer with sections, device preview, undo/redo, and
   autosave.
4. Scope article editing with block editor, inline media, TOC, and reading
   tools.
5. Plan revisions, scheduling, translation freshness, media variants, and focal
   point as separately reviewed backend releases.
