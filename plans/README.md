# Implementation Plans

| Order | Plan | Status | Depends on |
|---|---|---|
| 001 | [Spring Security session foundation](001-spring-security-session-foundation.md) | Ready for human review | None |

## Execution order

Execute plan 001 as one focused change. It establishes the security foundation
needed before a later, separately scoped JSON login/logout/current-user slice.

## Plan-index rules

- This index and the referenced plan were written against baseline commit
  `2330b7f` on 2026-07-11.
- The plan intentionally does not create credentials, users, new Flyway
  migrations, session tables, or browser UI changes.
- A future executor must run the plan's drift and clean-tree gates before
  touching implementation files.
