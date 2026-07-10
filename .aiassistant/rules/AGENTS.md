# AGENTS.md

## Project

This repository is for TahaMohamadi.ir, a bilingual Persian/English personal website, resume website, blog, portfolio, publication profile, and lightweight custom CMS.

## Required Reading Before Any Task

Before planning or implementing, read:

- README.md
- docs/master-plan.md
- .codex/project-context.md
- relevant .codex/*-rules.md files
- relevant docs/* files

## Core Stack

- Backend: Java Spring Boot
- Frontend: Vue + Quasar + Pinia
- Database: PostgreSQL
- Architecture: Modular Monolith
- Deployment: Docker Compose on VPS for MVP

## Critical Rules

1. Do not implement unrelated features.
2. Do not introduce microservices.
3. Do not introduce Kubernetes, Redis, Elasticsearch, Kafka, or external CMS in MVP.
4. Keep the MVP small, clean, and launchable.
5. Public pages must support Persian and English.
6. Persian routes use /fa and RTL.
7. English routes use /en and LTR.
8. Public pages must be SEO-friendly and AI-search-friendly.
9. Admin APIs must enforce authentication and authorization.
10. All publishable content must support Draft, Published, and Archived states.
11. Use PostgreSQL and Flyway for database changes.
12. Use DTOs; do not expose JPA entities directly.
13. Add tests for implemented behavior.
14. Update docs when behavior changes.

## Token Efficiency Rules

1. Read only files needed for the current task.
2. Prefer docs summaries over the full master plan after docs are split.
3. Use Graphify reports for repo navigation after the codebase grows.
4. Do not paste large files into prompts.
5. Do not modify unrelated files.
6. Keep each task small and reviewable.

## Development Workflow

For each task:

1. Restate the task.
2. Identify related requirement IDs.
3. List files to inspect.
4. Propose a short plan.
5. Make minimal focused changes.
6. Run or describe tests.
7. Summarize changed files.
8. Mention risks or follow-up tasks.

## Current Phase

Documentation and project setup.

Do not create application implementation code until the operational docs are split from docs/master-plan.md.
