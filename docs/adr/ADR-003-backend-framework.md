# ADR-003: Backend Framework

## Status

Accepted for MVP.

## Context

The backend must support secure admin APIs, RBAC, CMS workflows, PostgreSQL persistence, Flyway migrations, validation, audit logging, and long-term maintainability.

## Options

| Option | Fit | Notes |
|---|---|---|
| Spring Boot | High | Strong security and structure; aligns with Java preference |
| NestJS | Medium | Productive, but not the preferred backend ecosystem |
| Django | Medium | Productive CMS-like patterns, but not aligned with chosen stack |

## Decision

Use Java Spring Boot.

## Consequences

- Use Spring Security for authentication and authorization.
- Use Spring Data JPA for persistence unless a query requires a more explicit approach.
- Use DTOs, mappers, validation, service boundaries, and global exception handling.
- Use Flyway for all database schema changes.

## Risks

Spring Boot can introduce boilerplate if module boundaries and DTO discipline are weak.

## Follow-Up

Keep controllers thin, services transaction-aware, repositories focused, and tests close to implemented behavior.
