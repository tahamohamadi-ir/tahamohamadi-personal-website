# Security

Source: `docs/master-plan.md` v2.0 and `.codex/seo-rules.md`/architecture rules where relevant.

## Security Principles

| ID | Principle |
|---|---|
| SEC-P-001 | Keep public attack surface minimal |
| SEC-P-002 | Protect admin features first |
| SEC-P-003 | Use defense in depth |
| SEC-P-004 | Fail closed |
| SEC-P-005 | Apply least privilege |
| SEC-P-006 | Validate input everywhere |
| SEC-P-007 | Sanitize rich content |
| SEC-P-008 | Audit sensitive operations |
| SEC-P-009 | Keep secrets out of Git |
| SEC-P-010 | Secure by default |

## Authentication and Authorization

- FR-AUTH-001: Provide secure login, logout, and current-user behavior.
- FR-AUTH-002: Enforce RBAC on backend APIs.
- NFR-SEC-001: 100% of admin endpoints require authentication.
- ADR-007: Use Spring Security with secure session/cookie strategy for MVP.
- Admin and Super Admin actions must be audited when sensitive.

## Verified Pack B Admin Enforcement

The Spring Security chain applies `hasAnyRole("ADMIN", "SUPER_ADMIN")` to
`/api/v1/admin/**`. Anonymous requests receive the JSON `401` contract and
authenticated users without either role receive the JSON `403` contract. CSRF
remains enabled for every state-changing admin request; a missing token is
rejected with `403`.

Admin services obtain an audit actor only through `AuthenticatedAuditActor`,
which resolves the current authenticated principal against the persisted user
repository. Request bodies cannot supply or override an actor. Create, update,
lifecycle, deactivation, and deletion actions write stable `ADMIN_*` audit
actions with sanitized details; passwords, email addresses, markdown bodies,
media storage keys, session values, and CSRF values are excluded.

Validation, missing resources, optimistic-lock conflicts, invalid lifecycle
transitions, and publish validation use the common safe JSON error contract:
`400 VALIDATION_ERROR`, `404 RESOURCE_NOT_FOUND`, `409 OPTIMISTIC_LOCK_CONFLICT`
or `STATE_CONFLICT`, and `422 PUBLISH_VALIDATION_FAILED` where applicable.

## Authentication Foundation Boundary

The session-security foundation establishes the Spring Security filter chain,
persisted identity lookup, persisted role mapping, password encoder, server-side
session policy, CSRF transport convention, and JSON authorization failures. It
also provides JSON session authentication endpoints:

- `POST /api/v1/auth/login` accepts a CSRF-protected JSON email/password
  request and returns only the authenticated user ID, display name, and sorted
  active role codes. Successful login explicitly saves the authenticated
  `SecurityContext` to the HTTP session after rotating the pre-authentication
  session ID.
- `POST /api/v1/auth/logout` requires an authenticated, CSRF-protected session,
  records logout, clears the context, invalidates the server session, and
  expires the `JSESSIONID` cookie.
- `GET /api/v1/auth/me` requires an authenticated session and returns the same
  safe current-user representation.

All three responses use `Cache-Control: no-store`. Login failures have one
generic `401 Invalid credentials` response for unknown, wrong-password,
disabled, locked, and soft-deleted users. Login and logout remain CSRF
protected through the `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` header.

Successful and failed login attempts update the persisted account state: failed
attempts increment `failed_login_count`; successful attempts reset it and set
`last_login_at`. Authentication writes immutable, sanitized `AUTH_LOGIN`,
`AUTH_LOGIN_FAILED`, and `AUTH_LOGOUT` audit events containing only the
authentication method. IP and request identifiers remain null until the
privacy/proxy policy is decided.

Rate limiting, lock thresholds/durations, account-unlock behavior, and
concurrent-session policy remain deferred; this slice does not introduce any
token storage or external session infrastructure.

The authentication workflow uses a dedicated audit writer for `AUTH_LOGIN`,
`AUTH_LOGIN_FAILED`, and `AUTH_LOGOUT`. Any future lock transition must add an
`AUTH_ACCOUNT_LOCKED` event. Audit details must be sanitized and must never
contain passwords, password hashes, session identifiers, cookie values, CSRF
tokens, or raw credential input.

## Risk Table

| ID | Risk | Severity | Mitigation | Test |
|---|---|---|---|---|
| SEC-001 | Broken Access Control | Critical | Backend-enforced RBAC | Guest admin API returns 401/403 |
| SEC-002 | SQL Injection | High | Parameterized queries/JPA | Malicious search/input tests |
| SEC-003 | XSS | High | Markdown/rich text sanitization | Script payload test |
| SEC-004 | CSRF | High | CSRF token or SameSite strategy | Forged request test |
| SEC-005 | File Upload Attack | Critical | MIME/extension/size allowlist and random names | Invalid upload rejected |
| SEC-006 | Brute Force | High | Rate limit and/or lockout | Repeated login blocked |
| SEC-007 | Weak Password | Medium | Password policy | Weak password rejected |
| SEC-008 | Token Theft | High | httpOnly secure cookies | Cookie/security review |
| SEC-009 | Contact Spam | Medium | Honeypot, captcha, or rate limit | Spam burst limited |
| SEC-010 | Secrets Leak | Critical | Environment variables and secret scanning | Secret scan passes |
| SEC-011 | Telegram Abuse | High | Allowed user IDs and secret webhook | Fake webhook rejected |
| SEC-012 | Audit Gap | Medium | Audit create/update/delete/publish/login failures | Audit rows exist |
| SEC-013 | Missing Headers | Medium | CSP, HSTS, X-Frame protections | Header scan |
| SEC-014 | Dependency CVE | High | Dependency scanning | SCA check |
| SEC-015 | Excessive Error Detail | Medium | Generic public errors | Forced exception review |

## File Upload Rules

- Validate file extension, MIME type, and size.
- Store with random server-side names.
- Do not serve private media publicly.
- Images need alt text or admin warning.
- Keep local volume storage in MVP behind a StorageService abstraction.

## Content Security

- Sanitize markdown/rich text before public rendering.
- Do not render unsanitized direct HTML.
- Public APIs return only Published content.
- Draft and Archived content must not appear in public responses or sitemaps.

## Privacy

- NFR-PRIV-001: Collect minimal personal data.
- Contact messages require validation, storage discipline, and spam protection.
- Public registration is outside MVP, reducing early privacy surface.
