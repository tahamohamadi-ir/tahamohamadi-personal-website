# Project Context: TahaMohamadi.ir

## Project Summary

TahaMohamadi.ir is a bilingual Persian/English personal website, resume website, blog, portfolio, publication profile, and lightweight custom CMS.

The project is designed for:

- Personal branding
- PhD application
- Academic profile
- Resume and employment profile
- Technical blog
- Portfolio and gallery
- Publications and books
- Contact and professional networking

## Core Stack

- Backend: Java Spring Boot
- Frontend: Vue + Quasar + Pinia
- Database: PostgreSQL
- Architecture: Modular Monolith
- Deployment: Docker Compose on VPS for MVP
- Git Hosting: GitHub
- IDE: IntelliJ IDEA and WebStorm
- AI Development: Codex, openCode, Cursor, Claude Code, GitHub Copilot

## Languages

The website must support two languages from the beginning:

- Persian: /fa/...
- English: /en/...

Persian pages must use RTL layout.
English pages must use LTR layout.

## Main User Roles

- Guest
- Registered User, post-MVP
- Content Editor, post-MVP if needed
- Admin
- Super Admin / Site Owner

## MVP Scope

The MVP includes:

- Language selection page
- Persian and English public routes
- Landing page
- About/Profile page
- Resume page
- Research Interests page
- Publications page
- Blog list and blog detail
- Portfolio page
- Contact page
- Admin login
- Basic Admin panel
- Page management
- Blog post management
- Media management
- Social links management
- Basic SEO metadata
- Sitemap and robots.txt
- Docker Compose deployment

## Out of Scope for MVP

Do not implement these in MVP unless explicitly requested:

- Microservices
- Kubernetes
- Redis
- Elasticsearch or Meilisearch
- Public user registration
- Telegram Bot publishing
- Drag-and-drop layout builder
- Advanced analytics
- Multi-tenant SaaS mode
- Payment
- Public comment system

## Architecture Decision

Use Modular Monolith.

The backend must be organized by feature modules, not as one large technical-layer-only structure.

Recommended backend modules:

- auth
- user
- content
- blog
- portfolio
- media
- seo
- analytics
- admin
- audit
- common

## Development Principles

1. Keep the MVP small, clean, and launchable.
2. Avoid overengineering.
3. Prefer simple and maintainable solutions.
4. Public pages must be SEO-friendly.
5. Public pages must be AI-search-friendly.
6. Admin panel must be simple and usable.
7. All admin APIs must enforce authorization.
8. All publishable content must support Draft, Published, and Archived states.
9. All public content must support Persian and English translations.
10. Every feature must include acceptance criteria and tests.

## Primary Project Reference Document

The main full project specification is located at:

- `docs/master-plan.md`

AI coding agents must read this file before planning or implementing major features.

Use this document as the source of truth for:

- product requirements
- technical architecture
- database design
- API design
- security strategy
- SEO strategy
- i18n strategy
- roadmap
- backlog
- AI-assisted development setup

## AI Agent Rule

Before implementing any task, an AI coding agent must read:

- `.codex/project-context.md`
- `docs/master-plan.md`
- related `.codex/*-rules.md` files
- related `docs/*` files

The agent must not implement unrelated features.
The agent must not introduce new infrastructure unless the task explicitly requires it.
