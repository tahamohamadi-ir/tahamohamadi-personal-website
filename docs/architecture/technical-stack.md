# Technical Stack

Source: `docs/master-plan.md` v2.0.

## Stack Decisions

| Layer | MVP Choice | Related Decision | Notes |
|---|---|---|---|
| Backend | Spring Boot | DEC-002, ADR-003 | Secure, structured, maintainable Java backend |
| Security | Spring Security | ADR-007 | Authentication, authorization, common protections |
| Frontend | Vue | DEC-003, ADR-002 | Component-based frontend |
| UI Framework | Quasar | DEC-003, ADR-002 | One stack for public SSR and admin UI |
| State | Pinia | DEC-003 | Vue ecosystem standard |
| Database | PostgreSQL | DEC-004, ADR-004 | Relational data, JSONB where justified, FTS |
| Migration | Flyway | DB-002 | Versioned schema changes |
| ORM | Spring Data JPA | ADR-003 | Suitable for MVP CRUD and modular services |
| Search | PostgreSQL FTS | DEC-004 | Enough for MVP; external search deferred |
| Cache | None | NFR-MAINT-002 | Redis excluded from MVP |
| Editor | Markdown + Preview / TipTap | FR-BLOG-009 | Keep publishing simple |
| Math | KaTeX | FR-BLOG-010 | Should-have after MVP |
| Code Highlighting | Shiki or Prism | FR-BLOG-011 | Should-have after MVP |
| Analytics | Internal lightweight analytics | FR-ANALYTICS-001 | Privacy and simplicity |
| Testing | JUnit, Vitest, Playwright | Testing strategy | Standard backend/frontend/E2E coverage |
| CI/CD | GitHub Actions | Deployment plan | PR checks and release pipeline |
| Deployment | Docker Compose on VPS | DEC-008, ADR-010 | MVP deployment path |

## Key Answers

- Spring Boot is the backend choice because the project needs security, structure, and maintainability.
- Quasar is acceptable for MVP because it supports admin UI speed and SSR for public pages.
- Nuxt may be reconsidered only if public SEO complexity outgrows Quasar SSR.
- Public pages need SSR/hybrid rendering; admin pages do not.
- PostgreSQL is enough for CMS, blog, basic analytics, and first-pass search.
- Redis, external search, Kubernetes, and headless CMS are out of MVP.
- Telegram should be implemented later as an internal backend module that creates drafts only.

## Evidence Notes

Major technical decisions should continue to use official docs or primary implementation evidence where possible:

- Spring Security documentation for auth/security decisions.
- Quasar SSR documentation for rendering decisions.
- PostgreSQL docs for FTS and JSONB decisions.
- Schema.org and search engine guidance for SEO/structured data decisions.
