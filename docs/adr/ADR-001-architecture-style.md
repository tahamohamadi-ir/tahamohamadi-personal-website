# ADR-001: Architecture Style

## Status

Accepted for MVP.

## Context

TahaMohamadi.ir is a small-to-medium bilingual personal/resume/blog/portfolio site with a custom lightweight CMS. The system must stay launchable, understandable, secure, and extensible without introducing unnecessary infrastructure.

## Options

| Option | Fit | Notes |
|---|---|---|
| Simple Monolith | Medium | Fast, but can become hard to maintain without boundaries |
| Modular Monolith | High | Keeps deployment simple while preserving module boundaries |
| Microservices | Low | Adds operational complexity not justified for MVP |

## Decision

Use a Modular Monolith.

## Consequences

- Backend code is organized by feature modules such as auth, content, blog, media, seo, admin, audit, and common.
- Public and admin APIs must remain clearly separated.
- Module boundaries require code review discipline.
- Microservices are out of scope for MVP.

## Risks

If module boundaries are ignored, the codebase can still become tangled.

## Follow-Up

Use package-by-feature, DTOs, service boundaries, and focused tests for module behavior.
