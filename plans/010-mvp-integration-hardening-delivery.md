# Plan 010: MVP integration, hardening, and delivery

> **Executor instructions**: This is the final deployable-product wave. Use RED
> -> GREEN -> REFACTOR for every hardening change, verify rather than assume, and
> stop on failed security/data-recovery gates. Do not add product features.

## Status and dependencies

- **Baseline**: `1932457`; execution requires reviewed plans 001-009
- **Priority / effort / risk**: P0 / XXL (12-18 engineer-days) / CRITICAL
- **Requirements**: NFR-SEC-001..004, NFR-PERF-001..002, NFR-A11Y-001,
  NFR-OPS-001..003, FR-SEO-001..003, ADR-010
- **Depends on**: all prior plans; final sequential gate
- **Parallel**: CI/container/document drafts may begin late alongside 008/009 in
  a dedicated worktree, but production config, contract/E2E, migration, backup,
  performance, SEO, and release acceptance wait for all features. Conflict paths:
  `compose.yaml`, POM/package files, application config, Quasar config, docs, and
  test scripts; rebase and merge feature plans before final validation.

## Preflight and current-state evidence

Run from repository root:

```powershell
git rev-parse --show-toplevel
git branch --show-current
git rev-parse --short HEAD
git merge-base --is-ancestor 1932457 HEAD
git status --porcelain=v1
git diff --stat 1932457..HEAD -- backend frontend compose.yaml infra scripts .github docs
git cat-file -e HEAD:plans/009-admin-frontend-mvp.md
```

Require reviewed implementations 001-009 and no unrelated drift. At baseline,
`compose.yaml` provisions only PostgreSQL 17; `infra/{docker-compose,github-actions,nginx}`
and `scripts/{backup,restore,seed}` contain placeholders; no Dockerfiles, CI
workflow, production profile, reverse proxy, release/rollback/backup runbook,
error monitoring, or acceptance tests exist. Backend Actuator dependency exists;
frontend SSR build exists. ADR-010 binds Docker Compose on one VPS, not Kubernetes.

## Delivery scope and operational contracts

Produce reproducible Java 21 and Node 20 multi-stage containers running as
non-root with read-only root filesystems where practical, writable temp/media
volumes only, health checks, resource limits, pinned major/runtime images, and no
secrets in image/layers. Nginx terminates TLS in the documented production model,
proxies SSR/API/media, sets security headers, preserves real client/proxy trust
policy, limits request/upload sizes, and never logs cookies/query secrets.

Production config uses environment/secret injection, secure session cookie,
SameSite=Lax, exact same-origin/CORS policy, trusted proxy configuration, Flyway
validate/migrate-before-traffic strategy with one migrator, JPA validate, no
credential/user seed, and fail-fast required secrets. Health contract:
`/actuator/health/liveness` excludes DB; readiness includes PostgreSQL/Flyway-
compatible connectivity; details are not public. Structured logs use timestamp,
level, service, request ID, safe route/status/duration and actor ID where allowed;
never passwords/hashes, bodies, email/contact text, upload names/paths, cookies,
CSRF/session IDs, headers, or secrets. Metrics remain JVM/HTTP/DB-pool/health
boundaries; choose an external error-monitoring vendor only by human decision.

Backup is PostgreSQL logical backup plus media-volume snapshot with encryption,
retention, integrity manifest, and restore into isolated environment. A release
must prove restore, migration, smoke, and rollback. Rollback is application image
rollback only when schema is backward compatible; forward-fix is mandatory after
an incompatible forward migration. Never down-migrate or edit Flyway history.

## Exact files

**Create/modify runtime and CI**:

- `backend/Dockerfile`
- `frontend/Dockerfile`
- `.dockerignore`
- `compose.yaml` (modify)
- `infra/docker-compose/compose.production.yaml`
- `infra/nginx/nginx.conf`
- `infra/nginx/conf.d/tahamohamadi.conf`
- `.github/workflows/ci.yml`
- `backend/src/main/resources/application-prod.yml`
- `backend/src/main/resources/logback-spring.xml`
- `backend/src/main/java/ir/tahamohamadi/common/observability/RequestIdFilter.java`
- `backend/src/main/java/ir/tahamohamadi/common/observability/RequestLoggingFilter.java`
- `frontend/quasar.config.js` (production SSR/security settings)
- `.env.example` (nonsecret names/defaults only)

**Create operations scripts/docs**:

- `scripts/backup/backup.ps1`
- `scripts/restore/restore.ps1`
- `scripts/smoke/smoke.ps1`
- `docs/deployment/deployment-runbook.md`
- `docs/deployment/backup-restore-runbook.md`
- `docs/deployment/rollback-runbook.md`
- `docs/deployment/observability.md`
- `docs/testing/mvp-acceptance.md`
- `README.md` (deployment entry links only)

**Create/modify tests**:

- `backend/src/test/java/ir/tahamohamadi/delivery/ProductionConfigurationTest.java`
- `backend/src/test/java/ir/tahamohamadi/delivery/HealthEndpointIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/delivery/SecurityHeadersIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/delivery/StructuredLoggingTest.java`
- `frontend/test/e2e/mvp-public-acceptance.spec.js`
- `frontend/test/e2e/mvp-admin-acceptance.spec.js`
- `frontend/test/e2e/mvp-security-regression.spec.js`
- `frontend/test/e2e/mvp-seo-accessibility.spec.js`

Do not create a migration. Do not add Kubernetes, Redis, external CMS/search, or
monitoring SDK before a documented human decision.

## RED -> GREEN -> REFACTOR

**RED**: create production-config/health/header/log tests, full browser acceptance
tests, container smoke script, and backup/restore verification steps before
hardening code/config. Record failures caused by missing images/config/gates, not
environment outages. Acceptance matrix covers all public routes/locales/states,
admin login/CSRF/session rotation/logout/old-session denial, ADMIN/SUPER_ADMIN
access, anonymous/forbidden cases, each CRUD/upload/contact journey, optimistic
conflict, translation missing, sitemap/robots/canonical/hreflang/JSON-LD, keyboard/
contrast/landmarks, error/offline behavior, and sensitive-data absence.

**GREEN**: implement production profile/log filters/health groups; containers and
proxy; CI gates; scripts/runbooks; then run deployment rehearsal on an isolated
Compose environment. CI order: backend unit/integration PG17, frontend unit,
frontend SSR build, container build, Compose smoke/E2E, migration checksum gate,
dependency audit/review, and artifact publication only from protected release.
Use immutable image tags/digests and produce checksums/SBOM if existing tooling
supports it without an unapproved platform.

**REFACTOR**: minimize images/config duplication, remove test-only production
leaks, verify docs against commands, and repeat a clean-room deployment and
restore. No final approval from partial or cached results.

## Exact verification and expected results

From `backend/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

From `frontend/`:

```powershell
npm ci
npm run test:unit
npm run build
npm run test:e2e
```

From root (commands finalized verbatim in the runbook):

```powershell
docker compose -f compose.yaml -f infra/docker-compose/compose.production.yaml config
docker compose -f compose.yaml -f infra/docker-compose/compose.production.yaml build
docker compose -f compose.yaml -f infra/docker-compose/compose.production.yaml up -d
.\scripts\smoke\smoke.ps1
.\scripts\backup\backup.ps1
.\scripts\restore\restore.ps1 -Target isolated-restore
git diff --check
```

Expected: zero test failures/errors/skips; PostgreSQL integration uses
`postgres:17-alpine` and never H2; clean packages and SSR build; non-root containers healthy;
smoke and isolated restore succeed; Flyway validate and versions 1-7 only; V1-V7
checksums unchanged; no frontend console/server errors; no secrets in config,
images, logs, artifacts, or Git. Dependency review has no unresolved critical/
high exploitable issue (document exception owner/expiry if accepted). Performance
baseline: public LCP <2.5s target on agreed mobile profile, bounded API latency and
payload/query counts, upload memory limits, startup/readiness measured. Lighthouse-
equivalent SEO and accessibility >=90, WCAG AA critical checks, no broken links,
valid sitemap/robots/structured data.

## Security and deployment gates

Review the access-control matrix route by route; session cookie attributes,
fixation rotation, invalidation, CSRF bootstrap/mutations, generic login failure,
same-origin/CORS, security headers, upload attacks, Markdown/XSS, SQL/FTS input,
PII/audit/log sanitization, secret injection, actuator exposure, dependency/SBOM,
backup encryption/access, and proxy trust. Deployment gates require approved DNS/
TLS, environment secrets, persistent DB/media volumes, capacity/disk alert plan,
backup schedule/retention, tested restore, operator/on-call owner, maintenance/
rollback window, release tag/digest, and acceptance sign-off.

## Exclusions, decisions, STOP conditions

Excluded: product features, Kubernetes, managed cloud redesign, Redis, CDN,
external search/CMS, active-active, auto-scaling, new schema, analytics product,
and vendor monitoring without approval.

Human decisions: VPS/OS/provider and DNS/TLS ownership; production domains and
exact origins; secret store; backup encryption/retention/off-site target and RPO/
RTO; media malware policy; error-monitoring/metrics destination; resource limits;
release approver/maintenance window. STOP on dirty/drift, unresolved blocker from
plans 001-009, failed security/access/restore/migration gate, changed migration,
unavailable PG17/Compose environment, secret in tracked/config output, critical
dependency without disposition, inaccessible core journey, or performance/SEO
baseline materially below target without accepted exception.

## Definition of Done

Fresh and upgrade deployment, complete acceptance suite, security review,
dependency/config/secret checks, backup and isolated restore, rollback rehearsal,
health/logging/observability, hardened containers/proxy, CI artifacts, smoke,
performance/accessibility/SEO baselines, and operator documentation all pass.
Every migration is immutable, all required human decisions are recorded, no
critical/high review finding remains unaccepted, and a named human approves the
MVP release against `docs/testing/mvp-acceptance.md`.

## Release handoff

Provide the human release owner with immutable image tags/digests and checksums,
CI/acceptance evidence, access-control and environment matrices, migration
checksums, backup/restore artifact and timestamp, rollback/forward-fix decision
tree, accepted risks with owners/expiry, and the signed release checklist.
