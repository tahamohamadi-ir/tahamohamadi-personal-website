# V2 Release 2: Composition aggregate and draft autosave

**Status:** In progress; Section aggregate and guarded Draft autosave implemented

## Delivered increment

- `V12__add_page_composition_sections.sql` introduces the typed relational
  `content_page_section` aggregate and moves existing blocks to a deterministic
  compatibility section.
- The public query orders enabled sections before their enabled blocks, leaving
  the existing public block response shape unchanged.
- `GET` and `PUT /api/v1/admin/pages/{pageId}/blocks/composition` provide an
  additive versioned composition API. The legacy flat blocks endpoint remains
  supported.
- The Admin composer reads/writes composition data and preserves sections while
  presenting the existing block editor.
- Draft-only autosave is debounced for 1500 ms. It reports pending, saving,
  saved, validation/error, and optimistic-conflict states. It does not run for
  non-Draft pages and never changes lifecycle state.
- Admins can issue a one-time short-lived (10 minute) preview token. Preview
  responses require that token and set `Cache-Control: no-store` and
  `X-Robots-Tag: noindex, nofollow`. The token is invalid for soft-deleted
  pages, and a disabled section makes its returned blocks effectively disabled
  so the shared renderer matches the public output. The public
  published-content API is not changed.

## Focused evidence

```powershell
cd backend
.\mvnw.cmd '-Dtest=AdminPageBlockIntegrationTest' test

cd ..\frontend
npm.cmd run test:unit -- page-block-renderer.spec.js
npm.cmd run build
```

All commands passed on 2026-07-26. Browser/device/keyboard and offline
recovery testing remain Release 2 acceptance work.
