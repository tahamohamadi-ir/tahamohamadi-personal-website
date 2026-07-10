# Sprint Plan

Source: `docs/master-plan.md` v2.0.

## Sprint Breakdown

| Sprint | Focus | Deliverables |
|---|---|---|
| Sprint 0 | Setup | Repository, docs, ADRs, `.codex` |
| Sprint 1 | Backend foundation | Spring Boot, PostgreSQL, Flyway, Auth |
| Sprint 2 | Frontend foundation | Quasar, routing, i18n |
| Sprint 3 | Public pages | Language, home, about, resume, contact |
| Sprint 4 | Blog/media | Blog CRUD, media upload |
| Sprint 5 | Admin | Dashboard, pages, posts |
| Sprint 6 | SEO/Launch | SSR, sitemap, tests, deploy |

## Sprint 0 Exit Criteria

- Focused documentation exists outside `docs/master-plan.md`.
- ADR-001 through ADR-010 are recorded.
- `.codex` rules exist and match MVP boundaries.
- No application implementation code is created before operational docs are split.

## Sprint 1 Exit Criteria

- Spring Boot skeleton builds.
- PostgreSQL is connected through Flyway migrations.
- Auth/RBAC foundation is present.
- Basic backend tests pass.

## Sprint 2 Exit Criteria

- Quasar skeleton builds.
- Public routing supports `/fa` and `/en`.
- Persian RTL and English LTR are handled.
- Admin routing remains separate from public SEO pages.

## Sprint 3 Exit Criteria

- Language selection and core public pages render.
- Home, about, resume, contact support Persian and English content.
- Public HTML is SEO-readable.

## Sprint 4 Exit Criteria

- Blog list/detail and admin CRUD exist.
- Media upload validates allowed files.
- Draft/publish/archive workflow works.

## Sprint 5 Exit Criteria

- Admin dashboard is usable.
- Page and post management work.
- SEO metadata can be managed for publishable content.

## Sprint 6 Exit Criteria

- SSR, metadata, schema, sitemap, and robots are validated.
- Security/QA checks pass.
- Docker Compose deployment and backup path are ready.
