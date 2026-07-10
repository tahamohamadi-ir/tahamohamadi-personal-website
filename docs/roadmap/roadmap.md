# Roadmap

Source: `docs/master-plan.md` v2.0.

## Phase Breakdown

| Phase | Goal | Deliverables | Definition of Done |
|---|---|---|---|
| Phase 0 | Discovery and setup | Repository, docs, `.codex`, protocol | Skeleton ready |
| Phase 1 | Foundation | Backend, frontend, database, auth base | Build/test pass |
| Phase 2 | Public MVP | Language, home, about, resume, contact | Public flow works |
| Phase 3 | Blog and CMS | Posts, categories, tags, search | Publish post works |
| Phase 4 | Admin | Dashboard, pages, media, settings | Admin usable |
| Phase 5 | SEO, i18n, performance | SSR, metadata, schema, sitemap | Lighthouse 90+ |
| Phase 6 | Telegram and advanced features | Bot draft flow | Draft via bot |
| Phase 7 | Security and QA | Hardening and test coverage | Release candidate |
| Phase 8 | Launch | Deploy, backup, monitoring | Live site |
| Phase 9 | Post-launch | Improvements | Prioritized backlog |

## Critical Path

```text
Repo + .codex
-> DB + Backend Foundation
-> Auth + RBAC
-> Frontend Layout + i18n
-> Public Pages
-> Admin CRUD
-> Blog + Media
-> SEO/SSR
-> Security/QA
-> Deployment
```

## Risk Register

| ID | Category | Risk | Probability | Impact | Mitigation |
|---|---|---|---|---|---|
| RSK-001 | Product | Scope creep | High | High | MVP lock |
| RSK-002 | Technical | SSR complexity | Medium | High | SSR spike |
| RSK-003 | Security | Admin compromise | Low | Critical | RBAC, rate limit, audit |
| RSK-004 | SEO | Poor rendering | Medium | High | SSR tests |
| RSK-005 | UX | Admin too complex | Medium | Medium | Simple CRUD |
| RSK-006 | Timeline | Telegram delays MVP | High | Medium | Move to Phase 6 |
| RSK-007 | Maintenance | CMS over-dynamic | Medium | High | Presets only |
| RSK-008 | AI Dev | Agent changes unrelated files | Medium | Medium | `.codex` rules and PR review |
| RSK-009 | Research | Weak evidence leads to bad decision | Medium | High | Evidence log |
| RSK-010 | Deployment | No backup | Medium | High | Backup script |
| RSK-011 | Content | English incomplete | High | Medium | Translation status |
| RSK-012 | Security | Upload abuse | Medium | High | Upload validation |

## Launch Readiness

- MVP pages complete in Persian and English.
- Admin is protected and publish workflow works.
- SEO metadata, schema, sitemap, and robots are validated.
- Backup and restore path is tested.
- Docker Compose deployment is reproducible.
- Smoke/E2E/security checks pass.
