# Unified Design System Foundation Design

**Date:** 2026-07-22
**Canonical authority:** `docs/design.md`

## Problem

The current website is functionally complete enough to serve public content and administer it, but the visual system is conservative and inconsistent with the interdisciplinary brand. The public experience reads as a functional MVP and exposes generic component-library patterns. The admin is useful but needs a clearer operational system.

## Approved direction

- Keep Vue 3 and Quasar SSR.
- Use a Quasar-first architecture.
- Maintain one shared brand foundation.
- Build separate public and admin surface systems.
- Make public pages expressive, editorial, interactive and image-aware.
- Make admin pages clean, dense, predictable and highly functional.
- Ship light-first.
- Build and test dark-mode infrastructure without a public toggle in release one.
- Support Persian and English equally.
- Base the identity on Engineer, Researcher, Builder and Human-Centered Designer.

## Scope of the foundation implementation

The first implementation plan covers:

1. canonical documentation;
2. machine-readable tokens;
3. Quasar Sass and brand configuration;
4. light and dark semantic token layers;
5. typography and layout foundations;
6. motion and accessibility foundations;
7. shared branded primitives;
8. a design-system development route or component showcase;
9. tests and SSR validation.

It does not redesign the Home page yet. Home redesign begins only after the foundation and visual concepts are approved.

## Architectural boundaries

- `docs/design.md` is the human-readable authority.
- `design-tokens.json` is the machine-readable authority.
- Sass files compile tokens into runtime CSS custom properties.
- `quasar.variables.scss` maps brand variables into Quasar.
- Shared branded components govern repeated patterns.
- Public and admin components consume the same semantic tokens but different density and composition layers.
- Dark mode is token-complete and QA-accessible but not user-visible.

## Visual concept dependency

Before Home implementation, generate and approve complete desktop and mobile concepts for:

- public header and hero;
- selected work;
- research and writing sections;
- About/visual practice;
- footer/contact;
- one inner content page;
- one representative admin page.

## Success criteria

- no ungoverned visual values;
- Quasar components align with the brand;
- SSR succeeds;
- all existing frontend tests pass;
- new foundation tests pass;
- Persian and English layouts work;
- light theme is production ready;
- dark tokens produce readable representative states;
- a component showcase demonstrates every primitive and state;
- no public dark toggle is present.
