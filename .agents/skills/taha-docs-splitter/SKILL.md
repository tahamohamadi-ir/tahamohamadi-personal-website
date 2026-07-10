---
name: taha-docs-splitter
description: Use when splitting docs/master-plan.md into focused operational docs for TahaMohamadi.ir. Do not write application code.
---

# Taha Docs Splitter Skill

Use this skill when the task is to extract, summarize, or restructure `docs/master-plan.md`.

## Required Reading

- AGENTS.md
- README.md
- .codex/project-context.md
- docs/master-plan.md

## Instructions

1. Do not implement backend or frontend code.
2. Split content into focused docs.
3. Keep each target file concise and operational.
4. Preserve requirement IDs.
5. Preserve MVP boundaries.
6. Preserve bilingual Persian/English strategy.
7. Preserve ADR decisions.
8. Avoid duplicating the whole master plan.

## Expected Output

- Files changed
- Summary per file
- Ambiguities found
- Recommended next step
