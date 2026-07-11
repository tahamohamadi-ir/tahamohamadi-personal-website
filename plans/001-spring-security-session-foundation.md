# Plan 001: Establish the Spring Security server-session foundation

> **Executor instructions**: Follow this plan using RED -> GREEN -> REFACTOR.
> Run each verification command and confirm its expected result. This is a
> security foundation only: do not implement login, logout, or a frontend flow.
> If a STOP condition occurs, stop and report it. Do not commit or push.
>
> **Drift check (run first)**: `git diff --stat 2330b7f..HEAD -- <in-scope implementation paths>`.
> If an in-scope implementation file changed, compare it to the current-state
> facts below; a material mismatch is a STOP condition.

## Status

- **Priority**: P1
- **Effort**: M
- **Risk**: HIGH (authentication, authorization, cookies, and CSRF)
- **Depends on**: none
- **Category**: security, tests
- **Planned at**: baseline commit `2330b7f`, 2026-07-11
- **Requirements**: FR-AUTH-001, FR-AUTH-002, NFR-SEC-001. This is only the
  foundation for the later NFR-SEC-004 lockout/rate-limit workflow.

## Why this matters

ADR-007 selects Spring Security with a secure session/cookie strategy, while
ADR-011 specifies a same-origin browser admin backed by persisted identities
and roles. The backend has the identity and audit schema plus JPA entities, but
has no `SecurityFilterChain`, user-details service, password encoder, CSRF
delivery convention, or JSON security errors. This plan creates the reusable
server-session boundary needed before a later JSON login/logout/current-user
slice.

## Baseline and preflight gates

Run from repository root before source edits. `plans/` may already contain this
handoff; no changed file below `backend/` or `docs/` is allowed at this gate.

| Gate | Command | Expected result |
|---|---|---|
| Repository root | `git rev-parse --show-toplevel` | `D:/Project/Taha/tahamohamadi-ir` |
| Branch | `git branch --show-current` | `plans/security-foundation` |
| Baseline | `git rev-parse --short HEAD` | `2330b7f`, or a reviewed successor with no in-scope drift |
| Clean implementation tree | `git status --porcelain=v1 -- backend docs` | no output |
| ADR check | `git cat-file -e HEAD:docs/adr/ADR-011-admin-session-authentication-baseline.md` | exit 0, no output |
| Commit drift | `git diff --stat 2330b7f..HEAD -- backend/pom.xml backend/src/main backend/src/test docs/architecture/security.md` | no output, or reviewed compatible changes only |
| Working-tree drift | `git diff --check` | exit 0, no whitespace errors |

Do not use cached test output or a previous agent's conclusion as evidence.

## Current state and conventions

### Architecture decisions

- `docs/adr/ADR-007-authentication-strategy.md:19-28` selects Spring Security
  with a secure session/cookie strategy; admin APIs require authentication and
  RBAC.
- `docs/adr/ADR-011-admin-session-authentication-baseline.md:27-31` selects
  server-side HTTP sessions backed by `AppUserRepository`, Spring Security
  password verification, and persisted active role codes. It rejects JWT,
  refresh tokens, browser token storage, remember-me, public registration,
  password reset, Spring Session JDBC, and Redis session storage.
- ADR-011:37-48 requires an HttpOnly, host-only, `SameSite=Lax` session cookie,
  `Secure=true` outside `local`, and CSRF enabled without a broad `/api/**`
  exemption. ADR-011:50-52 forbids permissive/wildcard/credentialed CORS.
- ADR-011:64-70 requires session-ID rotation after authentication and a
  30-minute idle timeout. Do not use `SessionCreationPolicy.STATELESS`.
- ADR-011:76-98 requires disabled, soft-deleted, and locked users to be
  rejected, active persisted role codes mapped with `ROLE_`, generic failures,
  and no password/hash/session/cookie/CSRF values in logs, errors, or audit
  details.
- `docs/database/data-model.md:66-69` defines `app_user` state and append-only
  `audit_event`. Its line 420 still calls the session/token choice open;
  ADR-011 is newer and controls this work.

### Existing paths to reuse

- `backend/pom.xml` already contains Spring Security, `spring-security-test`,
  Spring Boot Test, and PostgreSQL Testcontainers. Do not add H2 or a new
  security/session dependency.
- `backend/src/main/resources/application.yml` holds shared settings and
  `application-local.yml` owns local-only overrides. Hibernate remains
  `validate` and Flyway remains enabled.
- `backend/src/main/resources/db/migration/V1__create_identity_and_audit_foundation.sql`
  owns the identity/audit schema, seeds only `ADMIN` and `SUPER_ADMIN`, and has
  no application user. Treat V1 and every existing Flyway migration as
  immutable.
- `backend/src/main/java/ir/tahamohamadi/identity/user/AppUser.java` suppresses
  the password-hash getter and excludes it from `toString()`. Preserve both.
- `backend/src/main/java/ir/tahamohamadi/identity/user/AppUserRepository.java`
  has the nondeleted, case-insensitive, trimmed `findByNormalizedEmail` query;
  reuse it rather than creating another lookup.
- `backend/src/main/java/ir/tahamohamadi/identity/assignment/UserRoleRepository.java`
  lists assignments by user. Add one focused active role-code query instead of
  lazy role loading during authentication.
- `backend/src/main/java/ir/tahamohamadi/audit/event/AuditEvent.java` and its
  repository are the future audit boundary. Do not inject or write them here.
- `backend/src/test/java/ir/tahamohamadi/FlywayV1IdentityAuditIntegrationTest.java`
  is the existing PostgreSQL 17 Testcontainers pattern: `@Testcontainers`,
  `@ServiceConnection`, and `new PostgreSQLContainer<>("postgres:17-alpine")`.

## Commands you will need

Run from `backend/` unless noted otherwise.

| Purpose | Command | Expected on success |
|---|---|---|
| Unit tests | `.\mvnw.cmd -Dtest=SecurityConfigurationUnitTest test` | exit 0; all named unit tests pass |
| PostgreSQL integration | `.\mvnw.cmd -Dtest=SessionSecurityIntegrationTest test` | exit 0; PostgreSQL 17 Testcontainers starts; all tests pass |
| V1 regression | `.\mvnw.cmd -Dtest=FlywayV1IdentityAuditIntegrationTest test` | exit 0; V1 validates and all existing tests pass |
| Full suite | `.\mvnw.cmd test` | exit 0; no failed or skipped tests |
| Final scope, root | `git status --short` | only in-scope files changed |

Testcontainers requires Docker. Do not use H2, production-like local data, or
Maven flags that skip tests.

## Scope

**Modify**

- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-local.yml`
- `backend/src/main/java/ir/tahamohamadi/identity/user/AppUser.java`
- `backend/src/main/java/ir/tahamohamadi/identity/assignment/UserRoleRepository.java`
- `docs/architecture/security.md`

**Create**

- `backend/src/main/java/ir/tahamohamadi/auth/security/AuthSecurityProperties.java`
- `backend/src/main/java/ir/tahamohamadi/auth/security/SecurityConfiguration.java`
- `backend/src/main/java/ir/tahamohamadi/auth/security/PersistedUserDetailsService.java`
- `backend/src/main/java/ir/tahamohamadi/auth/security/SecurityErrorResponse.java`
- `backend/src/main/java/ir/tahamohamadi/auth/security/JsonAuthenticationEntryPoint.java`
- `backend/src/main/java/ir/tahamohamadi/auth/security/JsonAccessDeniedHandler.java`
- `backend/src/main/java/ir/tahamohamadi/auth/security/CsrfTokenController.java`
- `backend/src/test/java/ir/tahamohamadi/auth/security/SecurityConfigurationUnitTest.java`
- `backend/src/test/java/ir/tahamohamadi/auth/security/SessionSecurityIntegrationTest.java`

**Out of scope**

- V1 or any Flyway migration; all migrations are immutable.
- `backend/pom.xml`, unless a declared dependency is unavailable. That is a
  STOP condition, not permission to add H2, Spring Session, Redis, or another
  dependency.
- Login/logout/current-user endpoints, auth request/response DTOs, frontend
  login/CSRF code, and all Quasar files.
- JWT, refresh tokens, OAuth2/OIDC, remember-me, password reset, registration,
  session database tables, administrative users/credentials, or seeds.
- Audit writes, failed-login counters, `last_login_at`, lock thresholds/duration,
  rate limiting, IP/proxy trust policy, concurrent-session controls, security
  headers, and unrelated refactoring.

## Target design

### Security filter chain

Create one `SecurityFilterChain` in `auth.security`, using constructor injection
and no JPA entity responses:

1. Permit only `GET /api/v1/auth/csrf` from the new auth surface.
2. Require `hasAnyRole("ADMIN", "SUPER_ADMIN")` for `/api/v1/admin/**`.
3. Leave all other routes permitted until their controllers exist; do not
   accidentally restrict health checks or invent public API policy.
4. Configure `DaoAuthenticationProvider` with the new
   `PersistedUserDetailsService` and the plan's `PasswordEncoder`. Expose an
   `AuthenticationManager` only if required for the later login slice.
5. Use `SessionCreationPolicy.IF_REQUIRED` and session-fixation
   `migrateSession()` explicitly. No stateless policy or custom session store.
6. Keep CSRF enabled with `CookieCsrfTokenRepository`: cookie `XSRF-TOKEN`,
   request header `X-XSRF-TOKEN`, cookie `HttpOnly=false`, `Path=/`,
   `SameSite=Lax`, and the same profile-dependent Secure setting as the session
   cookie. This is a CSRF value, never an authentication session ID.
7. Do not register `CorsConfigurationSource`, `@CrossOrigin`, wildcard origins,
   or credentialed CORS. The no-CORS-header behavior is the same-origin
   baseline.
8. Use `JsonAuthenticationEntryPoint` and `JsonAccessDeniedHandler`. Both
   return `application/json`, `Cache-Control: no-store`, and only
   `timestamp`, `status`, `error`, `message`, and `path`; fixed messages only.
   Entry point is 401 and access denied is 403.

### Configuration properties and cookie settings

Create validated `@ConfigurationProperties(prefix = "taha.security")` with:

- BCrypt strength, default 12, overridable by `TAHA_AUTH_BCRYPT_STRENGTH`, with
  unsupported values rejected;
- `secureCookies`, used by the CSRF cookie customizer.

In shared `application.yml`, set a 30-minute servlet idle timeout and session
cookie policy: HttpOnly, `SameSite=Lax`, host-only by omitting Domain, `Path=/`,
nonpersistent (no max-age), and Secure resolved from `taha.security` with a
default of true. In `application-local.yml`, override only
`taha.security.secure-cookies=false`. Do not relax CSRF, HttpOnly, or SameSite
in the local profile.

Use `BCryptPasswordEncoder` from the typed strength. BCrypt is the
ADR-011-permitted fallback until Argon2id resources are reviewed. Do not log,
serialize, seed, return, or audit plaintext passwords or hashes.

### Persisted users, roles, CSRF, and audit boundary

Create `PersistedUserDetailsService` with `@Transactional(readOnly = true)`:

1. Load via `AppUserRepository.findByNormalizedEmail`; absence becomes
   `UsernameNotFoundException` without logging submitted identity data.
2. Add `UserRoleRepository.findActiveRoleCodesByUserId(UUID)`: join
   `user_role` to `role`, require `is_active=true` and `deleted_at is null`, and
   return deterministically ordered code strings.
3. Convert codes to `SimpleGrantedAuthority("ROLE_" + code)`. Do not add a role
   enum, permission table, or role administration.
4. Build `UserDetails` using a deliberately named authentication-only
   password-hash accessor, `AppUser.enabled`, and a nonlocked flag false when
   `locked_until` is in the future. The repository already excludes soft-delete.
5. Modify `AppUser` only for that non-JavaBean accessor; preserve the suppressed
   getter and safe `toString()`.

Create `CsrfTokenController` at `GET /api/v1/auth/csrf`. Resolve the configured
`CsrfToken` to force repository delivery, return `204 No Content`, and do not
put the token in the body. A later same-origin Quasar client reads only the
non-HttpOnly CSRF cookie and sends `X-XSRF-TOKEN`; it never reads the HttpOnly
session cookie.

Update `docs/architecture/security.md` with an "Authentication foundation
boundary" subsection. It must state that this slice creates the filter chain,
identity lookup, role mapping, password encoder, sessions, CSRF convention, and
JSON authorization failures only. It must state that a later login/logout
workflow invokes a dedicated audit writer for ADR-011's `AUTH_LOGIN`,
`AUTH_LOGIN_FAILED`, `AUTH_LOGOUT`, and `AUTH_ACCOUNT_LOCKED` events with
sanitized details. This plan performs no `AuditEventRepository` write and never
records passwords, hashes, session IDs, cookies, CSRF tokens, or raw
credentials.

## Steps

### Step 1: RED - create focused unit tests

Create `SecurityConfigurationUnitTest` before implementation. Use Spring mock
servlet requests/responses and a fixed properties instance, with no database.
Add failing tests for:

- BCrypt encode/match behavior and no password/hash in JSON/error responses.
- 401 entry-point and 403 denied-handler JSON content type, `no-store`, exactly
  the safe error fields, path, and no exception/cookie/session/CSRF detail.
- The same migration strategy selected by `SecurityConfiguration` changes a
  pre-authentication mock session ID after authentication.
- Shared properties bind secure cookies and a 30-minute timeout; `local` changes
  only Secure to false.

**Verify**: `.\mvnw.cmd -Dtest=SecurityConfigurationUnitTest test`.
Expected initially: failures directly caused by absent components. Keep them.

### Step 2: RED - create PostgreSQL/Testcontainers integration tests

Create `SessionSecurityIntegrationTest` using the exact PostgreSQL 17 pattern
from `FlywayV1IdentityAuditIntegrationTest`, plus `@SpringBootTest` and
`@AutoConfigureMockMvc`. Import a test-source-only controller under
`/api/v1/admin/security-test`; do not add a production endpoint. Persist users
and assignments through existing repositories; use controlled SQL only for
test-only disabled, future-locked, and inactive-role states. Hash test
credentials with the injected encoder.

Add failing integration assertions for:

- unauthenticated admin GET -> 401 JSON with no usable authenticated session;
- authenticated users without admin roles -> 403 JSON; `ADMIN` and
  `SUPER_ADMIN` -> 200;
- case-insensitive trimmed persisted lookup maps only active nondeleted role
  assignments to `ROLE_` authorities, excluding inactive roles;
- disabled and future-locked users are rejected by user details/provider and
  soft-deleted users are not found; later client failure mapping remains generic;
- CSRF bootstrap -> 204, empty body, JavaScript-readable `XSRF-TOKEN` cookie,
  `Path=/`, `SameSite=Lax`, and profile-correct Secure;
- unsafe test request without/with invalid CSRF -> 403 JSON; valid header ->
  success; no broad `/api/**` CSRF ignore matcher;
- arbitrary `Origin` -> no `Access-Control-Allow-Origin` or credential header;
- pre-authentication session ID differs after session migration.

**Verify**: `.\mvnw.cmd -Dtest=SessionSecurityIntegrationTest test`.
Expected initially: failures due to the missing foundation. Do not use H2.

### Step 3: GREEN - implement the foundation

Implement every class and modification in "Target design" exactly. Keep all
new production code under `ir.tahamohamadi.auth.security`; keep identity/audit
ownership unchanged.

Review these invariants before considering Step 3 green:

- exactly one application `SecurityFilterChain` exists;
- CSRF is enabled with no broad ignore matcher;
- `/api/v1/admin/**` requires backend role authorization;
- no CORS configuration/header, stateless policy, password/session/CSRF leak,
  audit repository injection, migration, seed, H2, JWT, or session store exists.

**Verify**: run both focused Maven commands. Expected: exit 0 and all RED tests
pass unchanged.

### Step 4: GREEN - configure production-safe defaults and document audit boundary

Apply the shared cookie/timeout settings and local Secure-only override. Update
`docs/architecture/security.md` exactly as the boundary above specifies, with no
ADR rewrite and no audit implementation.

**Verify**: from repository root, run:

```powershell
git diff -- backend/src/main/resources/application.yml backend/src/main/resources/application-local.yml docs/architecture/security.md
```

Expected: only cookie/timeout/properties settings, local Secure=false, and the
audit boundary. No credential, CORS allow-list, CSRF disable, login/logout route,
or migration. Then run
`.\mvnw.cmd -Dtest=SecurityConfigurationUnitTest,SessionSecurityIntegrationTest test`
from `backend/`; expected exit 0.

### Step 5: REFACTOR - simplify without behavior change

Refactor only inside in-scope files after green tests:

- centralize repeated safe error serialization without introducing a global
  exception framework or changing public API errors;
- keep constants private to `auth.security`, avoiding a cross-module utility;
- make Testcontainers fixtures deterministic and clear test rows between tests;
- recheck that `AppUser.toString()` remains hash-free and the authentication
  accessor is not a JavaBean/Jackson property.

**Verify**: run, in order:

```powershell
.\mvnw.cmd -Dtest=SecurityConfigurationUnitTest test
.\mvnw.cmd -Dtest=SessionSecurityIntegrationTest test
.\mvnw.cmd -Dtest=FlywayV1IdentityAuditIntegrationTest test
.\mvnw.cmd test
```

Expected: each exits 0; no failed, errored, or skipped tests.

## Test plan and expected result

| Layer | File | Required behavior |
|---|---|---|
| Unit | `SecurityConfigurationUnitTest` | BCrypt, 401/403 safe JSON, session migration, shared/local property behavior |
| PostgreSQL 17 integration | `SessionSecurityIntegrationTest` | Persisted state/roles, admin RBAC, filter chain, CSRF delivery/enforcement, no CORS headers, session-ID rotation |
| Existing regression | `FlywayV1IdentityAuditIntegrationTest` | V1 remains unchanged and validates; seeded roles and hash-safe `toString()` remain valid |

The final full-suite output must include `BUILD SUCCESS` and report zero
failures, errors, and skipped tests. Exact test count may change; it is not a
success criterion.

## Done criteria

- [ ] Preflight and drift gates pass, or compatible drift is reviewed.
- [ ] One explicit filter chain uses `IF_REQUIRED`, `migrateSession()`, enabled
      CSRF, JSON 401/403, no CORS configuration, and admin RBAC.
- [ ] Sessions use 30-minute idle timeout and HttpOnly/host-only/`Path=/`/
      `SameSite=Lax` cookies, Secure outside `local`; only CSRF cookie is
      JavaScript-readable.
- [ ] CSRF bootstrap returns no token body, session ID, or credential.
- [ ] User lookup is nondeleted and case-insensitive; disabled/locked users
      reject; only active persisted roles produce `ROLE_` authorities.
- [ ] No password/hash/session/cookie/CSRF value reaches JSON, logs, exceptions,
      metrics, or audit details; `AppUser.toString()` stays safe.
- [ ] Audit integration is documented but no audit write, login/logout behavior,
      counter, lock transition, or rate limiting is implemented.
- [ ] V1 is byte-for-byte unchanged; no new migration, seed, H2, Spring Session,
      Redis, JWT, or frontend change exists.
- [ ] All four Maven commands pass with zero failures, errors, and skips.
- [ ] `git status --short` shows only in-scope files; do not commit or push.

## STOP conditions

1. Repository/branch/baseline/clean-tree/ADR gates fail, or in-scope drift
   changes a current-state fact.
2. Docker/Testcontainers cannot start PostgreSQL 17, or tests attempt H2,
   production-like local data, or another schema-ownership model.
3. The CSRF delivery mechanism requires disabled CSRF, a broad ignore matcher,
   wildcard/credentialed CORS, or JavaScript access to the session cookie.
4. Completion needs a V1 change, new migration/session table, user/credential
   seed, Spring Session/Redis/H2/JWT/OAuth dependency, or `pom.xml` change.
5. A login/logout/current-user endpoint, audit write, counter, lock policy,
   rate limit, IP/proxy policy, or concurrent-session policy is needed to pass.
6. Current Spring Security cannot provide the chosen CSRF, JSON errors, BCrypt,
   or session migration behavior with declared dependencies and compatible APIs.
7. A response/log/exception/metric/audit payload/test output would include a
   plaintext password, hash, session ID, cookie, CSRF token, bearer-like value,
   or raw authentication request body.

## Maintenance note

The later JSON login/logout/current-user plan must reuse this filter chain,
authentication manager, password encoder, persisted user-details service, and
CSRF convention. It must add generic credential failures, lock/rate-limit policy,
session invalidation, and dedicated audit writes without weakening this baseline.
A separate-origin client or horizontal deployment requires a new ADR before CORS
relaxation or shared session storage.
