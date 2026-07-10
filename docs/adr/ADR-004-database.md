# ADR-004: Database

## Status

Accepted for MVP.

## Context

The system needs durable CMS content, bilingual translations, blog search, admin data, audit logs, contact messages, media metadata, and lightweight analytics.

## Options

| Option | Fit | Notes |
|---|---|---|
| PostgreSQL | High | Relational data, JSONB when useful, built-in full text search |
| MySQL | Medium | Viable relational option, weaker fit for JSONB/FTS needs |
| MongoDB | Low/Medium | Flexible, but less suitable for relational CMS and RBAC data |

## Decision

Use PostgreSQL.

## Consequences

- Use Flyway for schema changes.
- Use translation tables for public multilingual content.
- Use PostgreSQL Full Text Search for MVP search.
- Use JSONB only for flexible settings, not core business data by default.

## Risks

Advanced search ranking may become limited as content grows.

## Follow-Up

Revisit external search only after PostgreSQL FTS is insufficient and the need is proven.
