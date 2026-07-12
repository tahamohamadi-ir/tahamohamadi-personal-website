# Implementation Plans

| Order | Plan | Status | Depends on |
|---|---|---|---|
| 001 | [Spring Security session foundation](001-spring-security-session-foundation.md) | Implemented before roadmap baseline; plan retained | None |
| 002 | [JSON session authentication APIs](002-json-session-authentication-api.md) | Ready for implementation | 001 |
| 003 | [Database and persistence wave A](003-database-persistence-wave-a.md) | Ready for human review | 001 |
| 004 | [Database and persistence wave B](004-database-persistence-wave-b.md) | Ready for human review | 003 |
| 005 | [Backend admin application APIs](005-backend-admin-application-apis.md) | Ready for human review | 002, 003, 004, 007 |
| 006 | [Backend public content, search, SEO, and contact APIs](006-backend-public-content-search-seo-contact-apis.md) | Ready for human review | 003, 004, 007 |
| 007 | [Media upload and asset-management pipeline](007-media-upload-asset-management-pipeline.md) | Ready for human review | 001, 003 |
| 008 | [Public frontend MVP](008-public-frontend-mvp.md) | Ready for human review | 006, 007 |
| 009 | [Admin frontend MVP](009-admin-frontend-mvp.md) | Ready for human review | 002, 005, 007, 008 |
| 010 | [MVP integration, hardening, and delivery](010-mvp-integration-hardening-delivery.md) | Ready for human review | 001-009 |

## Master roadmap

[MVP-DELIVERY-ROADMAP.md](MVP-DELIVERY-ROADMAP.md) is the authoritative
dependency graph, wave/merge order, parallel-worktree guide, risk register,
human-decision list, verification strategy, and final acceptance gate.

Recommended waves: 002; 003; 004 and 007 in parallel; 005 and 006 in parallel;
008; 009; then 010. Follow each plan's stricter merge prerequisites and STOP
conditions. Plans 001 and 002 retain their original baselines and historical
context; plans 003-010 and the roadmap were planned at `1932457`.

## Plan-index rules

- Treat ADRs and approved repository documentation as binding.
- Start each plan from a clean worktree containing all merged prerequisites;
  compare implementation paths with the plan baseline before editing.
- Use RED -> GREEN -> REFACTOR and run focused, PostgreSQL 17, full-suite, scope,
  migration-integrity, security, and review gates specified by the plan.
- V1 and every merged migration are immutable. Use no H2.
- Preserve server-side session authentication, CSRF, exact-origin policy,
  Quasar SSR/public plus CSR/admin architecture, and Docker Compose VPS delivery.
- Stop and report missing human decisions, material drift, contradictions,
  unapproved dependencies/schema changes, or security/data-loss risk.
