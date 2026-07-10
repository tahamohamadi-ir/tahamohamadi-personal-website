# Architecture

Source: `docs/master-plan.md` v2.0 and `.codex/architecture-rules.md`.

## Decision Summary

| ID | Decision | Rationale |
|---|---|---|
| DEC-001 | Modular Monolith | Simple, fast, and extensible for a small-to-medium project |
| DEC-005 | Custom Lightweight CMS | Project needs are focused and require bilingual SEO control |
| DEC-006 | SSR/Hybrid public pages | Required for SEO and AI visibility |
| DEC-007 | CSR admin is acceptable | Admin pages do not need indexing |
| DEC-008 | Docker Compose on VPS for MVP | Lower operational complexity than Kubernetes |
| DEC-009 | Telegram Bot in Phase 6 | Avoids MVP security and scope risk |

## Final Architecture

```text
Browser / Crawler / AI Search
        |
        v
Quasar SSR Public App + CSR Admin
        |
        v
Spring Boot Modular Monolith REST API
        |
        v
PostgreSQL
        |
        v
Local Media Volume / Future S3-compatible Storage
```

## Architecture Rules

- Use a modular monolith; do not introduce microservices for MVP.
- Package backend code by feature module, not only by technical layer.
- Keep public APIs and admin APIs separate.
- Use DTOs; never expose JPA entities directly.
- Public APIs expose only published content.
- Admin APIs require authentication, authorization, and audit logging for sensitive operations.
- Use PostgreSQL first before adding new infrastructure.
- Public pages must be SSR/hybrid-rendered, semantic, SEO-friendly, and AI-search-friendly.
- Admin pages may be CSR.

## Backend Modules

| Module | Purpose | MVP |
|---|---|---|
| auth | Login, logout, current user, RBAC | Yes |
| user | Admin and future user identities | Yes |
| content | Generic pages and shared content states | Yes |
| blog | Posts, translations, publishing | Yes |
| portfolio | Portfolio items | Yes |
| media | Uploads and file metadata | Yes |
| seo | Metadata, sitemap, robots, schema | Yes |
| analytics | Lightweight stats | Basic/Should |
| admin | Admin workflows and dashboard | Yes |
| audit | Sensitive action logging | Yes |
| telegram | Draft creation from Telegram | Phase 6 |
| common | Shared DTOs, errors, utilities | Yes |

## Frontend Structure

```text
frontend/src/
  pages/public/
  pages/admin/
  components/common/
  components/blog/
  components/admin/
  stores/
  services/
  router/
  i18n/
  css/
```

## Public Routing

- Persian: `/fa/...`, `lang="fa"`, `dir="rtl"`
- English: `/en/...`, `lang="en"`, `dir="ltr"`
- Public URLs must not use query parameters for language selection.
- Missing translations must show a clear message and link to the available version.

## Content Workflow

```text
DRAFT -> PREVIEW -> PUBLISHED -> ARCHIVED
             \-> REJECTED/NEEDS_EDIT
```

All publishable content must support Draft, Published, and Archived states.

## Deployment Shape

```text
VPS
 |-- nginx/caddy reverse proxy
 |-- frontend SSR container
 |-- backend container
 |-- postgres container
 |-- media volume
 `-- backup scripts
```

## Overengineering Guardrails

Do not add Redis, Elasticsearch, Kafka, Kubernetes, external CMS, full multi-tenancy, drag/drop builder, direct Telegram publishing, or public comments in MVP without a new ADR.
