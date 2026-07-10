# ADR-010: Deployment Strategy

## Status

Accepted for MVP.

## Context

The MVP needs a low-complexity deployment path for a Spring Boot backend, Quasar SSR frontend, PostgreSQL database, media volume, reverse proxy, and backup scripts.

## Options

| Option | Fit | Notes |
|---|---|---|
| VPS + Docker Compose | High | Simple and controllable for MVP |
| Managed PaaS | Medium | Convenient, but adds provider coupling |
| Kubernetes | Low | Overkill for MVP |
| Static frontend + API | Low/Medium | Weakens SSR/SEO strategy |

## Decision

Deploy MVP with Docker Compose on a VPS.

## Consequences

- Compose runs frontend SSR, backend, PostgreSQL, reverse proxy, and volumes.
- Operational docs must include backup and restore.
- High availability is not an MVP goal.
- Kubernetes is explicitly out of scope unless future scale justifies it.

## Risks

Single-VPS deployment has limited high availability and requires careful backups.

## Follow-Up

Before launch, test deploy, migration, backup, restore, health check, and rollback procedures.
