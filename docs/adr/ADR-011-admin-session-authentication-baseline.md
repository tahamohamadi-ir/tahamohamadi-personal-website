# ADR-011: Admin Session Authentication Baseline

## Status

Accepted for MVP. This ADR refines the high-level session/cookie choice in ADR-007; it does not replace the project scope or introduce an authentication implementation.

## 1. Context

TahaMohamadi.ir has a same-origin browser-based admin application on top of a Spring Boot modular monolith. The MVP needs secure administrator login, logout, role-based access control, brute-force protection, and audit logging, while public registration, social login, password reset, and mobile/API-client authentication are out of scope. V1 already provides `app_user`, `role`, `user_role`, and `audit_event`; it does not create session tables or seed an application user.

## 2. Decision Drivers

- FR-AUTH-001 and FR-AUTH-002 require secure login/logout/current-user behavior and backend-enforced RBAC.
- NFR-SEC-001 and NFR-SEC-004 require protected admin APIs and brute-force mitigation.
- The MVP deploys a single Spring Boot instance through Docker Compose on a VPS, so operational simplicity matters.
- Admin is a same-origin browser client, not a public third-party API platform.
- The design must preserve CSRF protection, avoid browser token storage, and use the existing persisted identity and audit model.

## 3. Considered Alternatives

| Option | Advantages | Disadvantages and security implications | Operational, logout, and scaling behavior | Suitability |
|---|---|---|---|---|
| A. Server-side HTTP session | Browser receives only an opaque cookie; Spring Security provides mature session fixation protection, CSRF integration, and simple role-based access. Logout and revocation are direct server-side invalidation. | Requires CSRF defenses and server-side session state. A restart invalidates in-memory sessions. | Lowest MVP complexity. One backend instance needs no external session store; multiple instances need shared session state or carefully managed affinity. | **Selected.** Fits the same-origin admin and single-instance MVP. |
| B. JWT access and refresh tokens | Can support stateless access-token validation across independently scaled services and API clients. | Requires key management, expiry/refresh rotation, token theft handling, revocation design, refresh-token storage, and stronger XSS exposure if browser tokens are mishandled. Logout is not immediate without deny-listing or refresh revocation. | More endpoints, storage, monitoring, and incident procedures. Stateless access tokens reduce server state but move revocation complexity elsewhere. | Rejected for MVP. The product does not need distributed API clients or independent services. |
| C. OAuth2/OIDC with an external identity provider | Mature federation, centralized identity lifecycle, MFA options, and enterprise integration. | Adds a provider dependency, redirect/callback attack surface, client-secret management, user provisioning/linking, and availability coupling. | Provider configuration and operational ownership exceed the lightweight CMS MVP. Logout may involve both local and provider sessions. | Rejected for MVP. Reconsider only for a concrete institutional or multi-application SSO requirement. |

## 4. Selected Decision

Use Spring Security server-side HTTP session authentication for the MVP admin application. Authentication uses an application user loaded from `AppUserRepository`, password verification through `DaoAuthenticationProvider` or the current Spring Security equivalent, and authorities derived from persisted active role codes. The browser and backend remain same-origin.

No JWT access tokens, refresh tokens, token storage in `localStorage` or `sessionStorage`, remember-me, public registration, social login, password reset, Spring Session JDBC, or Redis session store are part of the first authentication slice.

## 5. Why Session Authentication Fits the MVP

The browser holds only a protected session identifier while authentication state and revocation remain server-side. This matches the single monolith deployment, avoids a refresh-token lifecycle, and makes logout, account disablement, and future administrator session invalidation understandable and auditable. It also works naturally with Spring Security CSRF protections for a same-origin browser client.

## 6. Cookie Policy

- Use a server-managed session cookie with `HttpOnly` enabled.
- Set `Secure=true` outside local development; production traffic is HTTPS-only behind the trusted reverse proxy.
- Use `SameSite=Lax` unless a documented, reviewed flow requires another value.
- Use a host-only cookie by omitting `Domain`; use `Path=/` unless a narrower path is proven safe.
- Do not expose the session identifier to JavaScript or put it in URLs, request bodies, logs, browser storage, or audit details.
- Session cookies are non-persistent by default; remember-me is out of MVP.

## 7. CSRF Strategy

CSRF protection remains enabled. All state-changing browser requests, including logout, require the configured CSRF mechanism. SameSite is defense in depth, not a substitute for CSRF protection. The exact token repository and frontend transport convention must be selected during implementation and verified against JSON login behavior; CSRF must not be disabled globally or bypassed broadly for `/api/**`.

## 8. CORS and Same-Origin Strategy

The production browser client, SSR frontend, and backend are same-origin behind the reverse proxy. Do not enable permissive CORS, wildcard origins, or credentialed cross-origin requests for the MVP. A separate frontend origin, external API client, or cross-site cookie flow requires a new documented security decision.

## 9. Login Request and Response Contract Direction

The intended login endpoint is `POST /api/v1/auth/login` with a JSON request containing email and password over HTTPS. Spring Security establishes the session and sets the cookie; the response returns only a safe current-user representation needed by the admin client, such as ID, display name, and role codes. It never returns `password_hash`, a session identifier, or a bearer token. Responses must use `Cache-Control: no-store`.

The final DTO field names, CSRF bootstrap endpoint, validation shape, and current-user endpoint are implementation details, but all credential failures use one generic response and status policy that does not reveal whether an account exists, is disabled, or is locked.

## 10. Logout Behavior

Logout is a CSRF-protected state-changing request. It invalidates the server-side session, clears the cookie with matching path and attributes, and records a successful logout audit event when a session is authenticated. No token deny list is required because the session is invalidated directly. A missing or expired session must not disclose account state.

## 11. Session Fixation Protection

Successful authentication must rotate the session identifier using Spring Security session-fixation protection. The implementation must not retain a pre-authentication session ID after login. This behavior is an explicit integration-test requirement.

## 12. Session Timeout

Use an explicit idle session timeout, with a 30-minute inactivity timeout as the MVP baseline. There is no remember-me extension. An absolute maximum session lifetime, if needed, remains a later implementation policy and must not be approximated by a long-lived browser cookie.

## 13. Concurrent-Session Policy

The maximum-concurrent-session policy is intentionally unresolved. The implementation must explicitly choose whether an additional login is allowed, rejected, or invalidates the oldest session, and must add tests and audit behavior for that choice. Do not assume a policy from Spring Security defaults.

## 14. Account Lock Behavior

Authentication must reject disabled users, soft-deleted users, and users whose `locked_until` is in the future. Failed attempts update `failed_login_count`; a successful login resets the counter and records `last_login_at`. The lock threshold, lock duration, rate-limiting boundary, and whether an administrator can unlock an account are open implementation decisions. Any lock transition must record an audit event.

## 15. Password Hashing Strategy

Store only adaptive one-way password hashes in `app_user.password_hash`. Use Spring Security's password-encoding support, preferring Argon2id where deployment resources are reviewed and adequate; BCrypt is an acceptable documented fallback. Hashes include their algorithm/parameters and are never returned, logged, included in exceptions, copied to audit JSON, or seeded by migrations. Plaintext, reversible encryption, unsalted digests, and password hints are prohibited.

## 16. Role and Authority Mapping

Load `AppUser` through `AppUserRepository` using the nondeleted, case-insensitive email lookup. Authentication succeeds only for an enabled, nondeleted, unlocked user. Load active persisted role assignments through `user_role` and `role`; map stable codes to Spring Security authorities using the conventional `ROLE_` prefix, for example `ADMIN -> ROLE_ADMIN` and `SUPER_ADMIN -> ROLE_SUPER_ADMIN`. Do not introduce a role-code Java enum, permission table, or custom-role administration in this slice.

## 17. Authentication Failure Responses

Return generic credential-failure messages and avoid account enumeration. Failure responses must not identify whether the email is unknown, the account is disabled, soft-deleted, locked, or the password is incorrect. Validation errors may report malformed request shape without exposing authentication state. Responses and log correlation must not contain submitted passwords, password hashes, session identifiers, or cookie values.

## 18. Audit-Event Requirements

Record successful login, failed login, logout, and account-lock events in `audit_event`. Use stable action names such as `AUTH_LOGIN`, `AUTH_LOGIN_FAILED`, `AUTH_LOGOUT`, and `AUTH_ACCOUNT_LOCKED`, target type `app_user`, and the V1 `SUCCESS`, `FAILURE`, or `DENIED` outcomes. Use `actor_user_id` only when the actor is safely known. Keep `details` a sanitized JSON object with no password, hash, session identifier, token, cookie, or raw credential input. Capture IP data only after the privacy and reverse-proxy trust policy is defined.

## 19. Sensitive Logging Restrictions

Do not log submitted passwords, password hashes, session IDs, cookie headers, CSRF tokens, bearer-like values, reset-like values, or full authentication request bodies. Do not put these values in exception messages, traces, metrics tags, audit details, or support diagnostics. Authentication logs use a correlation/request ID and minimal, reviewed user identifiers only where necessary for operations.

## 20. Local-Development Behavior

The local Spring profile may set `Secure=false` only because normal local development is HTTP. `HttpOnly`, same-origin handling, CSRF protection, generic failures, session rotation, logout invalidation, and audit behavior remain required locally. Local development must not become a route for disabling CSRF, creating default users, or using production credentials.

## 21. Production Behavior

Production uses HTTPS at the reverse proxy and `Secure=true` on the session cookie. Forwarded-header handling must trust only the deployed proxy path. Production configuration must set session timeout, cookie attributes, secret material, password-encoder parameters, and any rate-limiting controls through reviewed configuration rather than source control. Security headers, TLS, monitoring, backup, and incident procedures remain deployment responsibilities.

## 22. Horizontal-Scaling Implications

The first deployment is a single backend instance, so container-local session state is acceptable. With more than one backend instance, in-memory sessions can be lost or routed inconsistently. Sticky sessions are not the long-term security or resilience strategy; horizontal scaling requires a reviewed shared session solution and deployment tests before it is enabled.

## 23. Deferred Spring Session Strategy

Do not add Spring Session JDBC or Redis, and do not create session database tables, in this task or the first MVP deployment. If horizontal scaling becomes a real requirement, assess Spring Session JDBC using PostgreSQL first against load, cleanup, migration, failure, backup, and revocation requirements. Redis requires a separate ADR because it adds new infrastructure currently excluded from MVP.

## 24. Rejected JWT Design

JWT access/refresh token authentication is rejected for this MVP. It would require access-token expiry design, refresh-token rotation and storage, revocation semantics, token replay handling, signing-key lifecycle management, and client storage rules without delivering a product need. A future external/mobile API requirement may reopen the decision through a new ADR.

## 25. Rejected LocalStorage Token Storage

Do not store authentication credentials, access tokens, refresh tokens, or session identifiers in browser `localStorage` or `sessionStorage`. JavaScript-readable storage expands XSS theft impact and is unnecessary for a secure server-side session cookie.

## 26. Testing Requirements

The authentication implementation must add focused tests using the existing PostgreSQL/Testcontainers foundation where persistence behavior matters and Spring Security test support for HTTP behavior. At minimum verify:

- unauthenticated admin access is rejected and role-restricted access returns the correct denial;
- valid login creates a protected session and rotates the session ID;
- logout invalidates the session and clears the cookie;
- CSRF rejects missing or invalid state-changing requests and accepts valid tokens;
- disabled, locked, and soft-deleted users cannot authenticate and receive generic failures;
- case-insensitive nondeleted email lookup and persisted role-to-authority mapping work;
- failed-login counter, lock behavior, and successful-login reset behavior match the chosen policy;
- login, failed login, logout, and lock events are persisted without sensitive details;
- cookie attributes differ only where documented between local and production profiles;
- concurrent-session behavior is tested once its policy is selected;
- no test uses H2 for PostgreSQL authentication/audit constraints.

## 27. Security Review Checklist

- [ ] CSRF remains enabled and has no broad ignore rule.
- [ ] Session cookies are HttpOnly, host-only, `SameSite=Lax`, and Secure outside local development.
- [ ] HTTPS and trusted forwarded headers are configured in production.
- [ ] Session ID rotates after successful login and invalidates on logout.
- [ ] No password, hash, session ID, CSRF token, or cookie is returned or logged.
- [ ] Generic credential failures prevent account enumeration.
- [ ] Disabled, locked, and soft-deleted users cannot authenticate.
- [ ] Only active persisted roles produce authorities.
- [ ] Audit events cover login success/failure, logout, and lock transitions without sensitive JSON details.
- [ ] No JWT, refresh token, remember-me, browser token storage, or session table is introduced incidentally.
- [ ] Session timeout and concurrent-session behavior are explicit and tested.

## 28. Consequences

- The admin browser receives an opaque session cookie rather than a bearer token.
- Spring Security configuration and tests must own CSRF, cookie, session-fixation, logout, and authorization details.
- Session invalidation is simple for the initial single backend instance.
- Backend restart ends active sessions in the first deployment.
- External or horizontally scaled session storage is deferred, not ruled out permanently.

## 29. Risks

- A missing CSRF token flow or incorrect proxy/cookie configuration can undermine the design.
- A single-instance restart logs administrators out; this is acceptable for MVP but must be communicated operationally.
- Lock threshold, duration, rate limiting, and concurrent-session rules need deliberate implementation to avoid either weak brute-force protection or unnecessary admin lockouts.
- Existing sessions may retain prior authorities until invalidated; role/disable changes require an explicit session-invalidation strategy before multi-admin operation grows.

## 30. Open Implementation Decisions

1. Exact CSRF token repository, bootstrap endpoint, and JSON-client transport convention.
2. Exact rate-limit boundary, failed-attempt threshold, lock duration, and administrative unlock workflow.
3. Maximum concurrent-session policy and the audit behavior for displaced sessions.
4. Absolute session lifetime and whether it differs from the 30-minute idle timeout.
5. Argon2id parameters versus BCrypt fallback after deployment-resource review.
6. Safe IP-address collection, retention, and trusted reverse-proxy header policy.
7. Session invalidation behavior after role, enabled-state, or password changes.
8. The trigger and design for externalized sessions when horizontal scaling becomes necessary.

## Documentation Alignment

`docs/database/data-model.md` currently lists the session-versus-token mechanism as open. This ADR resolves that choice for the MVP: server-side HTTP sessions are selected, while future session persistence remains deferred. ADR-007 and `docs/architecture/security.md` already select a secure session/cookie approach; this ADR provides the missing operational and security detail.
