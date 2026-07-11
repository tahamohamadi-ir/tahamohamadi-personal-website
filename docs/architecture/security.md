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

## Authentication Foundation Boundary

The session-security foundation establishes the Spring Security filter chain,
persisted identity lookup, persisted role mapping, password encoder, server-side
session policy, CSRF transport convention, and JSON authorization failures. It
does not implement login, logout, a current-user API, login counters, lockout
policy, or authentication audit writes.

A later login/logout workflow must use a dedicated audit writer for
`AUTH_LOGIN`, `AUTH_LOGIN_FAILED`, `AUTH_LOGOUT`, and `AUTH_ACCOUNT_LOCKED`
events. Audit details must be sanitized and must never contain passwords,
password hashes, session identifiers, cookie values, CSRF tokens, or raw
credential input.

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
