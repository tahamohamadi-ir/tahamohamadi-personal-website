# TahaMohamadi.ir

Bilingual personal/resume/blog/portfolio website with a lightweight custom CMS.

## Stack

- Backend: Java Spring Boot
- Frontend: Vue + Quasar + Pinia
- Database: PostgreSQL
- Architecture: Modular Monolith
- Deployment: Docker Compose on VPS
- AI Development: Codex / openCode / Cursor / Claude Code / GitHub Copilot

## Main Goals

- Personal branding
- PhD application profile
- Resume and portfolio
- Blog and publications
- SEO-friendly and AI-search-friendly structure
- Simple custom admin panel
- Avoid overengineering

## Project Structure

backend/
frontend/
docs/
infra/
scripts/
.codex/

## Development Rule

Before implementing any feature, read files inside:

- .codex/
- docs/

## Master Plan

The complete Product + Technical + Architecture + Roadmap + AI Development Setup document is available at:

- docs/master-plan.md

AI coding tools should read this file together with .codex rules before implementing features.

## MVP

The MVP includes:

- Language selection
- Persian and English routes
- Landing page
- About/Profile
- Resume
- Research Interests
- Publications
- Blog
- Portfolio
- Contact
- Admin login
- Basic CMS
- Basic SEO
- Docker Compose deployment

## Development Phases

1. Phase 0: Discovery and Project Setup
2. Phase 1: Architecture and Foundation
3. Phase 2: Public Website MVP
4. Phase 3: Blog and Content Management
5. Phase 4: Admin Panel
6. Phase 5: SEO, i18n and Performance
7. Phase 6: Telegram Bot and Advanced Features
8. Phase 7: Security Hardening and QA
9. Phase 8: Launch Preparation
10. Phase 9: Post-launch Improvements

## Local PostgreSQL Development

PostgreSQL 17 is used for local development because the project documentation selects PostgreSQL but does not specify a major version. `compose.yaml` starts only the database; the backend remains a local Spring Boot process.

```powershell
# Start PostgreSQL from the repository root
docker compose up -d postgres
docker compose ps

# Start the backend from the repository root
$env:SPRING_PROFILES_ACTIVE = 'local'
.\backend\mvnw.cmd spring-boot:run

# Stop PostgreSQL without deleting the named data volume
docker compose down
```

Use `.env.example` only as a local-variable reference. Do not commit `.env` or production credentials.

### Windows Port Troubleshooting

Some Windows, WSL2, or Hyper-V configurations reserve host port `5432`. Check reserved ranges with:

```powershell
netsh interface ipv4 show excludedportrange protocol=tcp
```

If `5432` is reserved, set both local variables to `55432`:

```text
POSTGRES_PORT=55432
DB_PORT=55432
```

This maps host port `55432` to PostgreSQL container port `5432`; the generic project default remains `5432`.
