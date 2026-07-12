# Plan 004: Database and persistence wave B

> **Executor instructions**: Execute tests-first using RED -> GREEN -> REFACTOR.
> This plan is self-contained. Use only forward migrations and stop rather than
> modifying any migration already applied or merged.

## Status, baseline, and dependencies

- **Baseline**: `1932457`; execution requires the merged plan 003 implementation
- **Priority / effort / risk**: P0 / XL (7-10 engineer-days) / HIGH
- **Requirements**: FR-SKILL-001, FR-PORTFOLIO-001, FR-PUBLICATION-001,
  FR-RESUME-001/002, FR-LANDING-002, NFR-I18N-001
- **Depends on**: plan 003; plan 001 remains preserved
- **Parallel**: after plan 003, this may run alongside plan 007 in separate
  worktrees. Potential conflict paths are `media/asset/*`, data-model docs, and
  migration directory; plan 004 owns V5-V7 and plan 007 must create no migration.

## Preflight and evidence

```powershell
git rev-parse --show-toplevel
git branch --show-current
git rev-parse --short HEAD
git status --porcelain=v1
git diff --stat 1932457..HEAD -- backend/src/main backend/src/test backend/src/main/resources/db/migration docs/database
git cat-file -e HEAD:backend/src/main/resources/db/migration/V4__create_blog_content_and_search.sql
```

Require a clean tree and reviewed successor containing only completed prior
plans. `docs/database/data-model.md` assigns V5 to skill/portfolio, V6 to
publication/resume, and V7 to featured content/reviewed indexes. Current source
has only `portfolio/package-info.java`; skill, publication, resume, and featured
models do not exist. Reuse plan 003 UUID, audit, translation, status, soft-delete,
ordering, and repository conventions rather than adding a generic CMS model.

## Scope and migrations

Create exactly:

1. `V5__create_skills_and_portfolio.sql`: `skill_category`,
   `skill_category_translation`, `skill`, `skill_translation`,
   `portfolio_project`, `portfolio_project_translation`,
   `portfolio_project_skill`.
2. `V6__create_publications_and_resume.sql`: `publication`,
   `publication_translation`, `resume_entry`, `resume_entry_translation`,
   `resume_document`.
3. `V7__create_featured_content_and_reviewed_indexes.sql`: `featured_item` and
   only secondary indexes justified by actual repository access paths from
   plans 003-004. Do not add speculative search indexes.

Implement the exact constraints and ordering from the data model: skills are the
single technology taxonomy; content states are DRAFT/PUBLISHED/ARCHIVED;
publication stage is separate; translations are explicit `fa`/`en`; exactly one
featured target is non-null; public features target a published row; media FKs
restrict deletion; dates/ranges/order/version/soft deletion are enforced.

## Exact implementation files

**Migrations**:

- `backend/src/main/resources/db/migration/V5__create_skills_and_portfolio.sql`
- `backend/src/main/resources/db/migration/V6__create_publications_and_resume.sql`
- `backend/src/main/resources/db/migration/V7__create_featured_content_and_reviewed_indexes.sql`

**Skills**:

- `backend/src/main/java/ir/tahamohamadi/skill/SkillCategory.java`
- `backend/src/main/java/ir/tahamohamadi/skill/SkillCategoryRepository.java`
- `backend/src/main/java/ir/tahamohamadi/skill/SkillCategoryTranslation.java`
- `backend/src/main/java/ir/tahamohamadi/skill/SkillCategoryTranslationRepository.java`
- `backend/src/main/java/ir/tahamohamadi/skill/Skill.java`
- `backend/src/main/java/ir/tahamohamadi/skill/SkillRepository.java`
- `backend/src/main/java/ir/tahamohamadi/skill/SkillTranslation.java`
- `backend/src/main/java/ir/tahamohamadi/skill/SkillTranslationRepository.java`

**Portfolio**:

- `backend/src/main/java/ir/tahamohamadi/portfolio/project/PortfolioProject.java`
- `backend/src/main/java/ir/tahamohamadi/portfolio/project/PortfolioProjectRepository.java`
- `backend/src/main/java/ir/tahamohamadi/portfolio/project/PortfolioProjectTranslation.java`
- `backend/src/main/java/ir/tahamohamadi/portfolio/project/PortfolioProjectTranslationRepository.java`
- `backend/src/main/java/ir/tahamohamadi/portfolio/project/PortfolioProjectSkill.java`
- `backend/src/main/java/ir/tahamohamadi/portfolio/project/PortfolioProjectSkillId.java`
- `backend/src/main/java/ir/tahamohamadi/portfolio/project/PortfolioProjectSkillRepository.java`

**Publication and resume**:

- `backend/src/main/java/ir/tahamohamadi/publication/Publication.java`
- `backend/src/main/java/ir/tahamohamadi/publication/PublicationStage.java`
- `backend/src/main/java/ir/tahamohamadi/publication/PublicationRepository.java`
- `backend/src/main/java/ir/tahamohamadi/publication/PublicationTranslation.java`
- `backend/src/main/java/ir/tahamohamadi/publication/PublicationTranslationRepository.java`
- `backend/src/main/java/ir/tahamohamadi/resume/ResumeEntry.java`
- `backend/src/main/java/ir/tahamohamadi/resume/ResumeEntryType.java`
- `backend/src/main/java/ir/tahamohamadi/resume/ResumeEntryRepository.java`
- `backend/src/main/java/ir/tahamohamadi/resume/ResumeEntryTranslation.java`
- `backend/src/main/java/ir/tahamohamadi/resume/ResumeEntryTranslationRepository.java`
- `backend/src/main/java/ir/tahamohamadi/resume/ResumeDocument.java`
- `backend/src/main/java/ir/tahamohamadi/resume/ResumeDocumentRepository.java`

**Featured content**:

- `backend/src/main/java/ir/tahamohamadi/content/featured/FeaturedItem.java`
- `backend/src/main/java/ir/tahamohamadi/content/featured/FeaturedItemRepository.java`

**Tests/docs**:

- `backend/src/test/java/ir/tahamohamadi/persistence/FlywayWaveBIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/persistence/WaveBRepositoryIntegrationTest.java`
- `backend/src/test/java/ir/tahamohamadi/persistence/FlywayUpgradeIntegrationTest.java` (modify)
- `docs/database/database-design.md` (modify)
- `docs/database/data-model.md` (implementation status only)

No POM, V1-V4, frontend, API, auth, or seed change is allowed.

## RED -> GREEN -> REFACTOR and tests

**RED**: add/extend the three integration tests before production code. Record
failures caused by missing V5-V7 relations/entities/repositories. Cover fresh
versions 1-7, V4-to-V7 upgrade, repeat migrate zero, validate, expected tables,
and prior checksums unchanged. Constraint tests cover stable keys, translations,
locale slugs, DOI, stage/status, date/current/order, single active CV per locale,
project-skill duplicates/order, exactly-one featured target, target FK deletion,
and optimistic versions.

Repository scenarios cover inactive/deleted skill exclusion; ordered skill
categories; published localized project/publication/resume queries; missing
translation exclusion; deterministic project/publication/resume ordering;
replacement CV lookup; featured slot/time-window ordering; and no N+1 collection
fan-out in list projections. Demonstrate V7 indexes with representative
`EXPLAIN` plans; do not create an index merely to satisfy a brittle plan string.

**GREEN**: add migrations, then minimal controlled entities and repositories.
Use application UUIDs, explicit mappings, protected constructors, domain
transitions, ID equality, safe `toString`, and no `@Data`. Reuse `ContentStatus`,
`LanguageCode`, media, user audit FKs, and plan 003 patterns.

**REFACTOR**: consolidate only demonstrated repository duplication, document
implemented versions, and re-run every gate. Do not introduce generic entity,
translation, taxonomy, or ordering frameworks.

## Commands and expected results

From `backend/`:

```powershell
.\mvnw.cmd -Dtest=FlywayWaveBIntegrationTest test
.\mvnw.cmd -Dtest=WaveBRepositoryIntegrationTest test
.\mvnw.cmd -Dtest=FlywayUpgradeIntegrationTest test
.\mvnw.cmd -Dtest=FlywayWaveAIntegrationTest,WaveARepositoryIntegrationTest test
.\mvnw.cmd test
```

All commands must be `BUILD SUCCESS` with zero failures/errors/skips using
`postgres:17-alpine`; Flyway applies 1-7 exactly and Hibernate validates. From
root run `git diff --check`, checksum/diff checks for V1-V4, and list migrations;
only V5-V7 are new. Security: no secrets/PII/content bodies in logs, no entity
serialization, media deletion remains restricted, public queries enforce state,
locale, time window, and soft deletion. Performance: bounded paging, deterministic
tie-breakers, justified indexes, no eager to-many loading. Accessibility is a
downstream concern; mappings must retain locale and alt/CV metadata needed by UI.

## Exclusions, decisions, and STOP conditions

Excluded: APIs, DTOs, uploads, frontend, automatic featured selection, subjective
skill proficiency, separate technology taxonomy, normalized publication authors,
resume history beyond the approved tables, new search domains, H2, seeds, and
prior-migration edits.

Before GREEN, obtain human decisions for the exact CV replacement policy and
manual-only featured authoring semantics. Default only if approved: one current
nondeleted resume document per locale and manual ordered features. STOP for dirty
or material drift; missing plan 003 merge; occupied V5-V7 number; changed earlier
checksum; unapproved schema decision; need for dependency/extension; unavailable
PostgreSQL 17; or any need to edit V1-V4.

## Definition of Done and handoff

V5-V7 migrate fresh and as upgrade, all target tables/mappings/query contracts
pass PostgreSQL tests, versions 1-4 remain byte-identical, no speculative index
is added, docs match behavior, and review gate has no blocker. Handoff records
the full checksum/index list and resolved CV/featured decisions. Plans 005 and
006 may start after merge; plan 007 may have proceeded concurrently and must be
rebased/reconciled before either API plan.
