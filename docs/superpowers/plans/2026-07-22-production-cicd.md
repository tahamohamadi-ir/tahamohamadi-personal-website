# Production CI/CD Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build approval-gated, pull-based Production CI/CD using GitHub Actions, GHCR, systemd, Caddy version reporting, backup-before-deploy, and automatic rollback.

**Architecture:** GitHub builds immutable SHA-tagged images and creates immutable approval tags after manual Environment approval. A root-owned systemd watcher detects the newest approval tag and deploys corresponding images after a successful Production backup.

**Tech Stack:** GitHub Actions, GHCR, Docker Compose, Bash, systemd, Caddy, Spring Boot, PostgreSQL, Vue 3, Quasar SSR.

## Global Constraints

- Java version is 21.
- Frontend Node.js version is 22.23.1.
- Production PostgreSQL and Media volumes must remain unchanged.
- Application secrets must remain only on the VPS.
- GitHub must not connect directly to Production SSH.
- Production images must be tagged by exact 40-character Git SHA.
- Deployment must back up Production before changing application containers.
- Failure must restore the previous repository commit and image pair.

---

### Task 1: Version-aware Production Compose

- Modify `compose.production.yaml`.
- Add immutable image variables to Backend and Frontend.
- Preserve named PostgreSQL and Media volumes.
- Validate Compose with non-secret CI placeholders.

### Task 2: CI and image publication

- Create `.github/workflows/ci-cd.yml`.
- Run Backend Maven verification on Java 21.
- Run Frontend tests and SSR build on Node.js 22.23.1.
- Validate Bash, Compose, and Docker builds.
- Publish exact SHA-tagged Backend and Frontend images.

### Task 3: Approval-gated deployment signal

- Reference the `production` GitHub Environment.
- Create a unique immutable Production tag after approval.
- Wait for `/deployment-version` to report the exact SHA.
- Validate public endpoints.

### Task 4: Production deployment agent

- Create `scripts/deploy/taha-deploy`.
- Create `scripts/deploy/taha-deploy-watch`.
- Create `scripts/deploy/install-server.sh`.
- Validate tags and main-branch ancestry.
- Back up Production.
- Deploy exact images without changing PostgreSQL.
- Roll back repository commit and images after failure.
- Install a one-minute systemd watcher.

### Task 5: Observability and operations

- Expose `/deployment-version`.
- Prevent caching of the version endpoint.
- Record deployment history.
- Document verification and recovery commands.
