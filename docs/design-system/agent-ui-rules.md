# Agent UI Rules

These rules apply to Codex, Antigravity, Hermes, OpenRouter models, UI/UX Pro Max and human contributors.

## Required reading order

1. `docs/design.md`
2. `docs/design-system/design-tokens.json`
3. `docs/design-system/quasar-capability-matrix.md`
4. `docs/design-system/component-inventory.md`
5. the active implementation plan
6. the target source files and tests

## Hard constraints

- Keep Vue 3 and Quasar SSR.
- Use Quasar as the framework foundation.
- Preserve Persian and English parity.
- Preserve SSR data loading and hydration.
- Use semantic tokens; do not introduce untracked literal visual values.
- Do not use generic gradients, particles, custom cursors, fake metrics or technology-logo walls.
- Do not invent visible copy, achievements, testimonials or statistics.
- Do not turn every section into a card grid.
- Do not install Quasar App Extensions without a written audit.
- Do not use `$q.screen` for layout that CSS can handle.
- Do not add a public dark-mode toggle in release one.
- Do not commit, push, merge, tag or deploy without explicit operator authorization for that exact action.

## Design behavior

- Public pages are editorial, expressive and image-aware.
- Admin pages are dense, predictable and functional.
- Shared brand identity does not require identical composition.
- Use one focal point per viewport.
- Motion must clarify hierarchy or state.
- Respect reduced motion.
- Prefer original artifacts and photography.
- Every interactive state must be keyboard accessible.

## Quasar behavior

- Prefer `QLayout` families for shells.
- Wrap repeated public visual patterns.
- Direct Quasar usage remains acceptable when tokens and documented patterns govern it.
- Preserve Quasar accessibility behavior; do not replace it with fragile custom scripts.
- Check SSR compatibility before using browser-only APIs.
- Use `QNoSsr` only when server rendering is genuinely impossible and the content is not SEO-critical.

## Required implementation loop

1. Read the active task.
2. Inspect existing patterns.
3. Write or update a failing test.
4. Run the targeted test and confirm failure.
5. Implement the smallest compliant change.
6. Run the targeted test.
7. Run relevant frontend tests.
8. Run SSR build.
9. Verify Persian and English.
10. Verify desktop and mobile.
11. Compare against the accepted visual concept.
12. Report changed files and evidence.
13. Stop before commit unless explicitly authorized.

## Output contract

Every agent handoff must include:

- exact files changed;
- exact tests run;
- test/build results;
- screenshots or visual evidence when UI changed;
- RTL/LTR observations;
- accessibility observations;
- known deviations;
- confirmation that no unapproved commit, push or deployment occurred.
