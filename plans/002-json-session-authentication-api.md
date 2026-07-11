# Plan 002: Implement JSON session authentication APIs

> **Executor instructions**: Execute RED -> GREEN -> REFACTOR. This plan is
> self-contained and assumes no prior agent context. Run every command and stop
> on any STOP condition. Do not commit, push, or change files outside Scope.
>
> **Drift check (run first)**: `git diff --stat 30ceccb..HEAD -- <all in-scope implementation paths>`.
> Compare any in-scope change with the current-state facts below. A material
> mismatch is a STOP condition.

## Status

- **Priority**: P1
- **Effort**: L
- **Risk**: HIGH (credential handling, browser session lifecycle, auditability)
- **Depends on**: `plans/001-spring-security-session-foundation.md`
- **Category**: security, tests
- **Planned at**: commit `30ceccb`, 2026-07-11
- **Requirements**: FR-AUTH-001, FR-AUTH-002, NFR-SEC-001; establishes the
  audited login/logout behavior required before later NFR-SEC-004 rate-limit
  and lock-threshold policy work.

## Why this matters

The security foundation already protects admin routes, uses persisted users and
roles, provides CSRF delivery, and configures server-side sessions. It has no
way for the same-origin Quasar client to authenticate, inspect the current
identity, or end a session. This plan adds those three APIs while maintaining
generic credential failures, explicit session persistence, session-ID rotation,
and audit events without sending credential or cookie data across any response,
log, or audit boundary.

## Baseline and preflight gates

Run from repository root before source edits. `plans/` may contain this handoff;
no changed implementation file is allowed.

| Gate | Command | Expected result |
|---|---|---|
| Root | `git rev-parse --show-toplevel` | `D:/Project/Taha/tahamohamadi-ir` |
| Branch | `git branch --show-current` | `plans/auth-session-api` |
| Baseline | `git rev-parse --short HEAD` | `30ceccb`, or reviewed successor with no in-scope drift |
| Tree | `git status --porcelain=v1 -- backend docs` | no output |
| Prior plan | `git cat-file -e HEAD:plans/001-spring-security-session-foundation.md` | exit 0 |
| ADR | `git cat-file -e HEAD:docs/adr/ADR-011-admin-session-authentication-baseline.md` | exit 0 |
| Drift | `git diff --stat 30ceccb..HEAD -- backend/src/main backend/src/test docs/architecture/security.md` | no output, or only reviewed compatible changes |

## Current state to preserve

- `SecurityConfiguration` has one servlet-only `SecurityFilterChain`, CSRF via
  `CookieCsrfTokenRepository` (`XSRF-TOKEN` -> `X-XSRF-TOKEN`),
  `SessionCreationPolicy.IF_REQUIRED`, and `migrateSession()`.
- `PersistedUserDetailsService` uses `AppUserRepository.findByNormalizedEmail`
  and `UserRoleRepository.findActiveRoleCodesByUserId`; it returns disabled and
  locked state through Spring Security `UserDetails`.
- `AppUser` owns `enabled`, `failedLoginCount`, `lockedUntil`, `lastLoginAt`,
  `deletedAt`, optimistic `version`, and deliberately suppresses a JavaBean
  password-hash getter. `AppUserRepository` excludes soft-deleted users.
- `AuditEvent` is immutable after persistence. `audit_event` already has IDs,
  timestamps, nullable actor/target IDs, action, outcome, request ID, IP, and a
  JSON-object `details` column. V1 also has the needed user counter/timestamp
  columns and constraints.
- `SessionSecurityIntegrationTest` is the structural model for web security and
  PostgreSQL 17 Testcontainers. It uses `postgres:17-alpine`,
  `@ServiceConnection`, `@SpringBootTest`, `@AutoConfigureMockMvc`, and a
  test-only controller.
- ADR-011 requires generic failures, login/logout audit events, no sensitive
  logging, CSRF for state changes, no token storage, and session-ID rotation.

## API contract

All responses use `Cache-Control: no-store`. Never return a password, hash,
session ID, cookie value, CSRF token, bearer token, or raw credential input.

| Endpoint | Request | Success | Error |
|---|---|---|---|
| `POST /api/v1/auth/login` | JSON `{"email":"...","password":"..."}`; `email`: `@NotBlank @Email @Size(max=320)`; `password`: `@NotBlank @Size(max=4096)` | `200` JSON `{"id":"UUID","displayName":"string","roles":["ADMIN"]}` plus server session cookie | Missing/invalid CSRF: existing `403` security JSON. Malformed body: `400` validation JSON. Unknown, wrong password, disabled, locked, or soft-deleted: same `401` existing security-error shape with `error="UNAUTHORIZED"`, `message="Invalid credentials"`. |
| `POST /api/v1/auth/logout` | Empty body; valid CSRF; authenticated session | `204`, no body, expired `JSESSIONID` cookie with `Path=/`, `Max-Age=0`, HttpOnly, SameSite=Lax, and profile-correct Secure attribute | Missing/invalid CSRF: existing `403` JSON. Unauthenticated: existing `401` JSON. |
| `GET /api/v1/auth/me` | authenticated session | `200` with the exact safe user JSON above | `401` existing security-error shape |

Use `AuthenticatedUserResponse(UUID id, String displayName, List<String> roles)`
for both login and `/me`; roles are stable active persisted codes without the
`ROLE_` prefix, sorted ascending. Use `LoginRequest(String email, String
password)` only for input. Add scoped validation DTOs so a `400` body includes
`timestamp`, `status=400`, `error="VALIDATION_ERROR"`,
`message="Invalid login request"`, `path`, and `validationErrors` containing
only field/message pairs. Do not include rejected values.

## Audit and account-state contract

Create an auth-owned audit writer that calls the existing `AuditEventRepository`
and `AuditEvent.record`; do not mutate `AuditEvent` after saving.

| Event | Actor / target | Outcome | Sanitized details |
|---|---|---|---|
| `AUTH_LOGIN` | authenticated `app_user` ID for both actor and target | `SUCCESS` | `{ "auth_method": "PASSWORD" }` |
| `AUTH_LOGIN_FAILED` | known nondeleted user ID for actor/target when safely resolved; otherwise null | `FAILURE` | `{ "auth_method": "PASSWORD" }` |
| `AUTH_LOGOUT` | authenticated `app_user` ID for both actor and target | `SUCCESS` | `{ "auth_method": "SESSION" }` |

Set `request_id` and `ip_address` to null until the ADR-011 privacy/proxy policy
is decided. Never place email addresses, passwords, hashes, session IDs,
cookies, CSRF tokens, raw request bodies, exception messages, or credentials in
`details`. Add domain methods to `AppUser` that atomically increment
`failed_login_count` and update `updated_at`, and that reset the count to zero,
set `last_login_at`, and update `updated_at` after success. Do not implement a
threshold, a lock transition, rate limiting, or account unlock behavior.

## Migration decision

**No migration is required.** V1 already provides all required columns and
constraints: `failed_login_count` (nonnegative), `last_login_at`, `locked_until`,
soft deletion, optimistic versioning, and append-only `audit_event` records
with sanitized JSON-object details. Do not modify V1 or create a new migration.

## Commands

Run from `backend/`.

| Purpose | Command | Expected result |
|---|---|---|
| API integration RED/GREEN | `.\mvnw.cmd -Dtest=AuthSessionApiIntegrationTest test` | exit 0 when implemented; PostgreSQL 17 container; zero failures/errors/skips |
| Foundation regression | `.\mvnw.cmd -Dtest=SecurityConfigurationUnitTest,SessionSecurityIntegrationTest test` | exit 0; zero failures/errors/skips |
| V1 regression | `.\mvnw.cmd -Dtest=FlywayV1IdentityAuditIntegrationTest test` | exit 0; V1 validates unchanged |
| Full suite | `.\mvnw.cmd test` | `BUILD SUCCESS`, zero failures/errors/skips |

If the checked-in wrapper fails before Maven starts, record its exact failure and
use the installed Maven equivalent only after reporting the wrapper issue; do
not modify wrapper, POM, or build tooling in this plan.

## Scope

**Modify**

- `backend/src/main/java/ir/tahamohamadi/auth/security/SecurityConfiguration.java`
- `backend/src/main/java/ir/tahamohamadi/identity/user/AppUser.java`
- `docs/architecture/security.md`

**Create**

- `backend/src/main/java/ir/tahamohamadi/auth/api/LoginRequest.java`
- `backend/src/main/java/ir/tahamohamadi/auth/api/AuthenticatedUserResponse.java`
- `backend/src/main/java/ir/tahamohamadi/auth/api/ValidationErrorResponse.java`
- `backend/src/main/java/ir/tahamohamadi/auth/api/ValidationFieldError.java`
- `backend/src/main/java/ir/tahamohamadi/auth/api/AuthSessionController.java`
- `backend/src/main/java/ir/tahamohamadi/auth/api/AuthApiExceptionHandler.java`
- `backend/src/main/java/ir/tahamohamadi/auth/application/AuthSessionService.java`
- `backend/src/main/java/ir/tahamohamadi/auth/application/AuthenticationAttemptResult.java`
- `backend/src/main/java/ir/tahamohamadi/auth/audit/AuthAuditService.java`
- `backend/src/test/java/ir/tahamohamadi/auth/api/AuthSessionApiIntegrationTest.java`

**Out of scope**

- V1, any migration, POM, frontend, user/credential seeds, or production
  datasource changes.
- Password reset, registration, remember-me, JWT, refresh tokens, OAuth2/OIDC,
  MFA, Spring Session, Redis, concurrent-session limits, role/permission tables,
  rate-limit infrastructure, lock threshold/duration, and unrelated refactoring.

## Steps

### Step 1: RED - specify API behavior with PostgreSQL integration tests

Create `AuthSessionApiIntegrationTest` following the existing Testcontainers
pattern. Persist test users with the injected `PasswordEncoder`; do not print or
persist raw test passwords. Reuse seeded roles and create transient test users
only inside the container. Add a test-only controller only if needed for session
inspection; never add a production test/login filter.

Write failing tests for all 23 required scenarios: CSRF bootstrap; missing-CSRF
login/logout; successful session login; safe login response; session-ID change;
generic wrong-password and unknown-user failures; disabled/locked/soft-deleted
rejection; failed-count increment; success reset and `last_login_at`; sanitized
login success/failure audits; unauthenticated and authenticated `/me`; logout
invalidation and old-session rejection; sanitized logout audit; full suite and
V1 regression.

For generic-failure equality, assert the same HTTP status, `error`, and
`message` for unknown, wrong-password, disabled, locked, and soft-deleted
requests. Assert audit JSON has only `auth_method`, no raw email or sensitive
keys. Use a pre-authentication `MockHttpSession` plus CSRF token to prove the
post-login ID changes; use the returned session to prove `/me`, then verify it
cannot access `/me` after logout.

**Verify**: `.\mvnw.cmd -Dtest=AuthSessionApiIntegrationTest test` -> fails
only because the APIs, session persistence, account mutation, and audit writes
do not exist.

### Step 2: GREEN - add domain mutations, audit writer, DTOs, and service

Implement `AppUser.recordFailedAuthentication(Instant)` and
`recordSuccessfulAuthentication(Instant)` with invariant checks and `updatedAt`
updates. Add `AuthAuditService` and `AuthSessionService` with transactional
boundaries: authenticate via the existing `AuthenticationManager`, never compare
hashes manually, mutate the resolved nondeleted user, and persist audit events.

Do not throw a raw provider exception after a failed attempt because that would
roll back its counter/audit transaction. Return an internal
`AuthenticationAttemptResult` distinguishing only success from generic failure;
the controller maps every authentication failure to the same safe 401 body.
On success, return authentication plus the safe response assembled from a fresh
persisted `AppUser` and `findActiveRoleCodesByUserId` result.

**Verify**: focused API test compiles and reaches the expected failing HTTP
assertions, with no direct password comparison or sensitive audit detail.

### Step 3: GREEN - wire explicit browser-session login/logout/me endpoints

Update `SecurityConfiguration` to expose and use one
`HttpSessionSecurityContextRepository` and one
`SessionAuthenticationStrategy` based on session-fixation migration. Retain
`IF_REQUIRED`, `migrateSession()`, the existing CSRF repository/handler, and
same-origin no-CORS baseline. Permit only `POST /api/v1/auth/login` and the
existing CSRF GET; require authentication for `POST /api/v1/auth/logout` and
`GET /api/v1/auth/me`.

In `AuthSessionController`, after a successful service authentication:

1. call the injected session-authentication strategy to rotate/migrate any
   pre-authentication session;
2. create a new `SecurityContext`, set the authenticated token, and call
   `securityContextRepository.saveContext(context, request, response)`;
3. return the safe user response and `Cache-Control: no-store`.

For logout, obtain the authenticated principal, write `AUTH_LOGOUT`, clear the
holder, explicitly save an empty context, invalidate the HTTP session, and add
an expiring `JSESSIONID` cookie with the documented matching attributes. Return
204 and no body. `/me` resolves the current principal to a nondeleted `AppUser`
and active persisted role codes; it never serializes a Spring `UserDetails` or
JPA entity.

Use the existing `SecurityErrorResponse` for generic 401 failures and existing
403 handling for CSRF. The scoped advice handles only validation errors. Do not
log request bodies or use emails in response/audit details.

**Verify**: `.\mvnw.cmd -Dtest=AuthSessionApiIntegrationTest test` -> all API
tests pass against PostgreSQL 17.

### Step 4: REFACTOR and document

Refactor only within Scope: remove duplicated mapping/error code, preserve
fixed generic messages, deterministic role ordering, and transaction behavior.
Update `docs/architecture/security.md` with the three endpoint contracts,
explicit context persistence, session rotation/invalidation, audit actions, and
the deferment of rate limits and lock policy. Do not revise ADR decisions.

**Verify**:

```powershell
.\mvnw.cmd -Dtest=AuthSessionApiIntegrationTest test
.\mvnw.cmd -Dtest=SecurityConfigurationUnitTest,SessionSecurityIntegrationTest test
.\mvnw.cmd -Dtest=FlywayV1IdentityAuditIntegrationTest test
.\mvnw.cmd test
```

Expected: every command exits 0 with `BUILD SUCCESS`; zero failures, errors,
and skipped tests.

## Done criteria

- [ ] Login, logout, and `/me` implement the exact contract and statuses above.
- [ ] Login/logout require CSRF; logout and `/me` require authentication.
- [ ] Successful login explicitly saves the authenticated context in the
      HTTP session and rotates the pre-authentication session ID.
- [ ] Logout clears context, invalidates the session, expires `JSESSIONID`, and
      the old session receives 401 from `/me`.
- [ ] Credential failures are indistinguishable and do not expose credentials,
      hashes, account state, cookies, session IDs, or CSRF tokens.
- [ ] Counters/timestamps and all three audit events persist correctly with
      sanitized details and no raw email address.
- [ ] No migration is created and V1 is byte-for-byte unchanged.
- [ ] All required integration cases and full regressions pass on PostgreSQL 17.
- [ ] `git diff --check` passes; only in-scope files changed; no commit/push.

## STOP conditions

1. Baseline, branch, clean-tree, ADR, or drift check fails.
2. Existing schema lacks a required column/constraint, or implementation would
   require modifying V1; report the evidence before proposing any migration.
3. CSRF must be disabled/ignored, CORS broadened, a session ID exposed, or a
   browser token introduced to make the APIs work.
4. Explicit context persistence or session migration cannot be achieved without
   a production login filter, Spring Session, or an out-of-scope dependency.
5. Generic failure, audit, or counter behavior requires recording a raw email,
   password, hash, cookie, session ID, CSRF token, IP address, or request body.
6. A failed attempt cannot commit its counter/audit event without changing the
   requested semantics, or lock threshold/rate-limit/concurrent-session policy
   becomes necessary for correctness.
7. Docker/Testcontainers cannot start PostgreSQL 17, H2 is selected, or an
   existing test requires a production seed.

## Maintenance notes

Future rate limiting and lock-transition policy must build on the two explicit
`AppUser` mutation methods and add `AUTH_ACCOUNT_LOCKED` only once thresholds
are decided. Role, enabled-state, and password changes still need a separate
session-invalidation decision. Reviewers should scrutinize transaction rollback
behavior on failed login, the exact session-cookie clearing attributes, and all
audit JSON keys.
