# ADR-007: Authentication Strategy

## Status

Accepted for MVP.

## Context

The MVP needs secure admin access, RBAC, audit logging, and protection against common admin/API attacks. Public registration is not part of MVP.

## Options

| Option | Fit | Notes |
|---|---|---|
| Spring Security + secure session/cookie | High | Simple and secure for a monolith/admin MVP |
| JWT-first auth | Medium | Useful for distributed systems, but adds handling complexity |
| Keycloak | Low/Medium | Powerful, but overkill for MVP |

## Decision

Use Spring Security with a secure session/cookie strategy for MVP.

## Consequences

- Admin APIs require authentication and RBAC.
- Login should include brute-force protection.
- Sensitive operations require audit logs.
- Public registration remains deferred.

## Risks

Misconfiguration can weaken the security model.

## Follow-Up

Test guest admin access, role enforcement, login failures, logout, session timeout, and CSRF/SameSite behavior.
