---
name: taha-backend-springboot
description: Use when implementing Spring Boot backend tasks for TahaMohamadi.ir. Scope includes modular monolith, REST APIs, JPA, Flyway, validation, security, and tests.
---

# Taha Backend Spring Boot Skill

## Required Reading

- AGENTS.md
- .codex/backend-rules.md
- .codex/database-rules.md
- .codex/security-rules.md
- .codex/testing-rules.md
- docs/architecture/architecture.md
- docs/database/database-design.md
- docs/api/api-design.md

## Instructions

1. Use package-by-feature.
2. Do not expose JPA entities directly.
3. Use DTOs.
4. Validate inputs.
5. Use transactions for writes.
6. Enforce backend authorization.
7. Add Flyway migrations for DB changes.
8. Add tests for implemented behavior.
9. Do not introduce unrelated infrastructure.
10. Keep changes small and reviewable.

## Output

- Plan
- Files changed
- Tests added
- Commands run
- Risks and follow-ups
