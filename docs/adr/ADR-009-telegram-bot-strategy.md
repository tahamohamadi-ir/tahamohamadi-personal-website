# ADR-009: Telegram Bot Strategy

## Status

Deferred to Phase 6.

## Context

Telegram-based publishing could improve content capture, but it adds webhook, identity, abuse, and publishing workflow risks. It is not required for MVP launch.

## Options

| Option | Fit | Notes |
|---|---|---|
| Backend module | Medium/High later | Simple integration with CMS drafts |
| Separate service | Low for MVP | Adds deployment and auth complexity |
| No bot | High for MVP | Keeps launch scope controlled |

## Decision

Do not implement Telegram in MVP. In Phase 6, implement it as a backend module that creates drafts only.

## Consequences

- Telegram cannot directly publish public content.
- Admin review and approval remains required.
- Bot security must validate allowed users and webhook secret.

## Risks

Webhook abuse or accidental direct publishing could compromise content integrity if implemented too early.

## Follow-Up

When Phase 6 starts, define bot-specific security tests and audit logging before implementation.
