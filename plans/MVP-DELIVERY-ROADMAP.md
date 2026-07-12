# TahaMohamadi.ir MVP Delivery Roadmap

> **Roadmap baseline**: commit `1932457`, 2026-07-11. This document coordinates
> plans 001-010; each numbered plan is the executable handoff. It does not
> authorize implementation from a dirty tree or a materially drifted successor.

## 1. Current-state assessment

The repository has sound architectural and operational intent but only the
identity/security foundation is implemented.

| Area | Evidence at `1932457` | Maturity |
|---|---|---|
| Architecture | ADR-001..011, modular Spring Boot/Quasar SSR/PostgreSQL/Compose decisions | Approved |
| Identity/audit DB | immutable V1, entities, repositories, PG17 tests | Implemented |
| Session security | `auth/security/*`: persisted users/roles, IF_REQUIRED sessions, fixation migration, CSRF, JSON 401/403 | Implemented by 001 |
| Auth workflows | plan 002 exists; no login/logout/me controller at baseline | Planned |
| Content persistence | only package placeholders in content/blog/media/portfolio/user | Missing |
| Admin/public APIs | API sketch only; no feature controllers/services/DTOs | Missing |
| Media | ADR-008 and target metadata model only | Missing |
| Public frontend | SSR skeleton, placeholder pages, six foundation tests | Skeleton |
| Admin frontend | static layout/home only | Skeleton |
| Delivery | Compose runs PG17 only; infra/scripts placeholders; no CI/images/runbooks | Missing |

Git history confirms plan 001 implementation preceded `1932457`; plan 002 was
added as a plan at `1932457`. Preserve both source plans as historical handoffs.

## 2. MVP final outcome

A deployable bilingual Persian/English personal site and lightweight custom CMS:
SSR-visible home/profile/research/resume/publications/blog/portfolio/contact;
PostgreSQL-backed localized content/search; accessible responsive Quasar UI;
session-authenticated, CSRF-protected admin CRUD/media/contact workflows;
audited writes; local-volume media; SEO/sitemap/robots/structured data; tested
backup/restore; hardened Docker Compose deployment on one VPS with CI and
operational runbooks.

## 3. Explicit MVP scope

- Language selection, `/fa` RTL and `/en` LTR, explicit missing translations.
- Home/featured, about/profile/research, skills, resume/CV, publications, blog
  list/detail/category/tag/search/attachments, portfolio list/detail, contact.
- ADMIN/SUPER_ADMIN server-session authentication and complete CMS workflows.
- Managed pages, blog, media, skills, projects, publications, resume, featured,
  social links, contact messages, audit records.
- PostgreSQL 17/Flyway V1-V7, PostgreSQL FTS for localized blog only.
- SSR SEO metadata, canonical/hreflang, JSON-LD, sitemap/robots.
- Local filesystem media behind a storage abstraction.
- Docker Compose VPS delivery, CI, security/accessibility/performance gates,
  backup/restore/rollback/smoke operations.

Project/publication detail is included because this roadmap request explicitly
requires it, although older MVP scope labels portfolio detail as Should. Keep
detail lean and do not pull other Should features into MVP.

## 4. Explicit post-MVP scope

Public registration/accounts, password reset, MFA, remember-me, JWT/OAuth/OIDC,
comments/reactions/payments, analytics product, visitor timeline, dark mode,
menu/theme/layout/slider CMS, Telegram drafts, scheduled publishing worker,
revision/editorial workflow, custom roles/permissions, fuzzy/cross-domain search,
slug history/redirects, normalized publication authors, cloud storage/CDN/image
transforms, Redis/Elasticsearch/Kafka, microservices/Kubernetes, multi-node
sessions, and `/llms.txt`.

## 5. Plans 001 through 010

| Plan | Outcome | Effort | Risk | State at baseline |
|---|---|---:|---|---|
| [001](001-spring-security-session-foundation.md) | Session security foundation | L | High | Implemented; retained |
| [002](002-json-session-authentication-api.md) | Login/logout/me, audit/account state | L | High | Approved plan |
| [003](003-database-persistence-wave-a.md) | V2 media, V3 page/contact, V4 blog/FTS persistence | XL, 8-12d | High | Planned |
| [004](004-database-persistence-wave-b.md) | V5 skill/portfolio, V6 publication/resume, V7 featured/indexes | XL, 7-10d | High | Planned |
| [005](005-backend-admin-application-apis.md) | Authorized/audited admin backend | XXL, 15-22d | High | Planned |
| [006](006-backend-public-content-search-seo-contact-apis.md) | Public content/search/contact/SEO backend | XL, 10-15d | High | Planned |
| [007](007-media-upload-asset-management-pipeline.md) | Validated local media upload/delivery | L, 6-9d | High | Planned |
| [008](008-public-frontend-mvp.md) | Bilingual SSR public experience | XXL, 15-22d | High | Planned |
| [009](009-admin-frontend-mvp.md) | Session-aware admin application | XXL, 15-22d | High | Planned |
| [010](010-mvp-integration-hardening-delivery.md) | Acceptance, hardening, CI, release operations | XXL, 12-18d | Critical | Planned |

Estimates are focused engineering days excluding decision and content-entry
latency. Parallel execution changes elapsed time, not total effort.

## 6. Dependency graph and critical path

```text
001 (implemented) --> 002 ------------------------------+
        |                                               |
        +--> 003 --> 004 --------+--> 005 --------+     |
                    \             |                 |     |
                     +--> 007 ----+--> 006 --> 008 +--> 009 --> 010
                                  +-----------------+
```

003 follows the committed foundation; 004 follows 003; 007 follows 003 and can
parallel 004; 005 needs 002/003/004/007; 006 needs 003/004/007 and can parallel
005; 008 needs 006/007; 009 needs 002/005/007/008; 010 needs all. The critical
path is 003 -> max(004,007) -> 006 -> 008 -> 009 -> 010, with 002 and 005 joining
before 009. API contract, asset, or human-decision delays extend it.

## 7. Recommended implementation waves and merge order

| Wave | Work | Merge order / exit |
|---|---|---|
| 0 Security workflow | 002 | Auth security/audit PG17 tests; no migration |
| 1 Persistence A | 003 | Merge V2, V3, V4 together after upgrade gates |
| 2 Parallel foundation | 004 and 007 worktrees | Merge 004, rebase 007, verify V1-V7/media refs |
| 3 Parallel backend | 005 and 006 worktrees | Merge 005 common contracts first, rebase 006 |
| 4 Public product | 008 | Live contract, SSR, a11y, SEO, build gates |
| 5 Admin product | 009 | CRUD/auth/E2E and public regression gates |
| 6 Release | 010 | Clean-room acceptance and human approval |

Recommended branches: `feat/auth-session-api`, `feat/persistence-wave-a`,
`feat/persistence-wave-b`, `feat/media-pipeline`, `feat/admin-api`,
`feat/public-api`, `feat/public-frontend`, `feat/admin-frontend`, and
`release/mvp-hardening`. Start from required merges, not directly from `1932457`.

## 8. Parallelizable and sequential-only work

**Group A**: plan 003 versus the fixture/public-shell portion of 008. Backend and
frontend paths do not conflict, but `docs/api/api-design.md` and root config may.
Do not merge fixture assumptions before plan 006 freezes contracts.

**Group B**: 004 and 007 after 003. Conflicts:
`media/asset/*`, migration directory, `docs/database/data-model.md`, `compose.yaml`.
004 exclusively owns V5-V7; 007 creates no migration. Merge 004, rebase 007, run
the full migration/reference suite.

**Group C**: 005 and 006 after 002/004/007. Conflicts: `common/api/*`,
`SecurityConfiguration.java`, repositories, `application.yml`, and API docs. 005
owns common errors/paging/security; feature packages split into `api/admin` and
`api/publicsite`. Merge 005, rebase 006.

**Sequential**: 008 then 009. Router, boot/API, i18n, CSS, dependencies, and
layouts are conflict-heavy and security-sensitive. Establish shared primitives
in 008 first.

**Late parallel draft**: 010 CI/container/runbook work may begin as 009 finishes,
but production config/acceptance cannot be verified. Conflicts include POM/package,
Quasar/application config, compose, env example, and README. Rebase after features.

## 9. Migration and contract order

Flyway is merge-serialized and immutable:

1. V1 identity/audit, already applied and never edited.
2. V2 media metadata/translations (003).
3. V3 managed pages/social/contact (003).
4. V4 blog/taxonomy/attachments/localized FTS (003).
5. V5 skills/portfolio/relations (004).
6. V6 publications/resume/CV references (004).
7. V7 featured targets and reviewed indexes (004).

Every wave verifies checksums, fresh migrate, prior-version upgrade, repeat zero,
and Flyway validate on PostgreSQL 17. No H2, down migration, or schema auto-create.

Contract order: 002 auth JSON -> 007 media multipart/URL -> 005 common/admin JSON
-> 006 public/SEO JSON -> 008 public live integration -> 009 admin live integration
-> 010 full contract/E2E. Freeze DTO fields, error/status codes, pagination,
translation, CSRF/cookie, conflict, media, and cache semantics at each handoff.

## 10. Required human decisions

| Decision | Before | Why |
|---|---|---|
| CV replacement/history policy | 004 | Determines active-per-locale constraint |
| Manual featured slot semantics | 004/005 | No automatic selection invention |
| Media MIME/size, PDF malware, retention/backup | 007 | Hostile-input boundary |
| Checksum dedupe advisory versus unique | 007 | Data model leaves open |
| Persian search normalization | 006 | Avoid unapproved extension/stemming |
| Contact retention/rate-limit/IP policy | 006/010 | Privacy and abuse boundary |
| Portrait/logo/favicon/OG, fonts, colors | 008 | No production placeholder assets |
| Admin English-only versus bilingual | 009 | Baseline is English/LTR only |
| API sketch references `EDITOR`, while V1 defines only ADMIN/SUPER_ADMIN | 005 | Resolve docs or add a separately approved future schema plan; do not invent the role |
| Markdown sanitizer/editor and Playwright | 008/009 | Dependency and XSS gate |
| VPS/OS/domain/DNS/TLS/origins/secret store | 010 | Production configuration |
| Backup encryption/target/retention/RPO/RTO | 010 | Restore/release gate |
| Monitoring destination, resources, release owner/window | 010 | Operations ownership |

Missing decisions are STOP conditions. Record them in an ADR or relevant
approved document before implementation.

## 11. Required environment dependencies

- Clean Git branches/worktrees and PowerShell-compatible commands.
- JDK 21 plus Maven wrapper.
- Docker/Testcontainers and `postgres:17-alpine`.
- Node >=20/npm >=10 and lockfile Quasar SSR toolchain.
- Approved browser/E2E dependency and browsers.
- Test admin credential provisioned outside migrations/source.
- VPS-equivalent Compose host, DNS/TLS, persistent DB/media volumes, secret
  injection, backup target, isolated restore target.
- Approved visual/font assets and production content.

## 12. Verification strategy

Every plan records genuine missing-foundation RED, implements minimum GREEN, and
REFACTORs with the full suite green. Backend progresses from unit to PG17
Testcontainers to `mvnw test`; frontend from contract fixtures to SSR build/live
integration/E2E. Every merge runs `git diff --check`, scope review, migration
checksum/inventory, sensitive-log/secret scan, and review gate.

- **Persistence**: constraints, repository semantics, upgrade/idempotency,
  query/index plans, JPA validate.
- **Security**: access matrix, generic auth, server session fixation/invalidation,
  CSRF, exact origins, safe error/audit/log, hostile upload and XSS tests.
- **Accessibility**: keyboard/focus, landmarks/headings, labels/errors, contrast
  AA, 44px targets, reduced motion, RTL/LTR, 375/768/1024/1440 no overflow.
- **Performance**: bounded paging/query counts, FTS plan, streaming upload,
  SSR/code split, agreed LCP <2.5s mobile target.
- **SEO**: SSR HTML, canonical/hreflang, translated metadata, JSON-LD,
  sitemap/robots exclusions, score target >=90.
- **Delivery**: clean builds, non-root images, health/smoke, dependency/secret
  review, migration-on-deploy, encrypted backup/isolated restore, rollback.

## 13. Security gates

Never disable CSRF, use stateless/JWT auth, expose hashes/entities, log passwords/
cookies/session IDs/CSRF/contact bodies, allow wildcard credentialed CORS, serve
unvalidated paths, or expose draft/deleted/missing-locale content. Admin writes
require server authorization, validation, optimistic locking, sanitized audit,
and safe errors. Release requires no unresolved critical/high exploitable finding
unless a named human accepts it with owner, expiry, and mitigation.

## 14. Deployment gates

Production origins/cookies/TLS/proxy trust are exact; secrets fail fast;
migrations 1-7 validate; DB/media persistence/capacity are defined; health is
correct; logs sanitized; CI artifacts immutable; backup/restore and rollback are
rehearsed; smoke/acceptance passes. No release from document-only evidence.

## 15. Risks and controls

| Risk | Plans | Control |
|---|---|---|
| Migration breadth/ordering | 003/004 | V2-V7 ownership, PG17 upgrade/checksum gates |
| Session/CSRF regression | 002/005/009/010 | Run auth suite every wave |
| Translation/SEO inconsistency | 003/004/006/008 | Explicit unavailable contract and locale tests |
| Unsafe content/upload | 005-008 | Sanitized Markdown and strict containment/signatures |
| API N+1/payload growth | 005/006 | Bounded projections/query-count gates |
| Frontend conflicts | 008/009 | Sequential ownership |
| Missing assets/content | 008/010 | Early decision/content readiness gate |
| Backup/media drift | 007/010 | Coordinated manifest and isolated restore |
| Placeholder operations | 010 | Executable runbooks plus rehearsal |
| Scope creep | all | Exclusions and STOP conditions |

## 16. Final acceptance criteria

1. Plans 001-010 are implemented/reviewed in dependency order with clean scope.
2. V1-V7 apply fresh/upgrade/idempotently on PostgreSQL 17 and prior files and
   checksums remain unchanged.
3. Public fa/en and admin CRUD/media/contact journeys work end to end.
4. Login/me/logout, fixation, invalidation, CSRF, access, safe errors/audit, and
   account-state regressions pass.
5. Only eligible requested-locale content/media is public; missing is explicit.
6. Unit, integration, SSR, E2E, contract, smoke, and restore gates are green.
7. WCAG AA critical behavior, RTL/LTR, SEO/structured data, sitemap/robots, and
   agreed performance baselines pass.
8. Containers/config/CI/logs/health/secrets/dependencies pass hardening review.
9. DB plus media restore and rollback/forward-fix rehearsal succeed in isolation.
10. Decisions are recorded and a named human signs the MVP release checklist.

## 17. Roadmap STOP rule

Stop the active plan, preserve the worktree, and report evidence on baseline/
scope/migration drift, binding-document contradiction, missing decision,
unavailable environment, security/data-loss risk, or need for an unapproved
dependency/schema change. Never weaken a gate or silently expand scope.
