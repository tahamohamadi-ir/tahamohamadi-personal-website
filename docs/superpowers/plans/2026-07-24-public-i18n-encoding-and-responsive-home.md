# Public i18n Encoding and Responsive Home Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` or `superpowers:subagent-driven-development` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore correctly encoded public English/Persian copy and make the rendered bilingual home page fit the documented responsive editorial system.

**Architecture:** Keep locale dictionaries as the sole source for localized copy, add a source-file regression test that decodes files as fatal UTF-8, and limit visual changes to the public home/header components and token-based CSS. Preserve SSR, API/CMS states, localization routing, semantics, and drawer behavior.

**Tech Stack:** Vue 3, Quasar SSR, Vitest, existing Manrope/Vazirmatn typography and design tokens.

## Global Constraints

- Work only on `feat/design-system-foundation`; do not change backend, APIs, migrations, SSR contracts, or production infrastructure.
- Use direct UTF-8-safe repository edits; do not use PowerShell source rewrites containing non-ASCII text.
- Preserve one H1, RTL/LTR behavior, CSS logical properties, 44px targets, reduced motion, safe Markdown, and documented tokens.
- Capture screenshots outside the repository; stage exact task-owned paths only.

---

### Task 1: Establish and repair the encoding regression

**Files:**
- Create: `frontend/test/vitest/__tests__/i18n-encoding.spec.js`
- Modify: `frontend/src/i18n/en.js`
- Modify: `frontend/src/i18n/fa.js`
- Modify: `frontend/src/components/public/home/HomeFeatured.vue`

- [ ] **Step 1: Write the failing source-encoding test**

Read both dictionary files as `Buffer`, require `new TextDecoder('utf-8', { fatal: true })` not to throw, reject `\uFFFD` and the mojibake marker character class, and assert the English values `Human-Centered Systems Builder`, `Engineer \u00B7 Researcher \u00B7 HCI Designer`, `Human\u2013AI interaction`, and `Research \u2192 systems design \u2192 production software` plus Persian `\u0637\u0647 \u0645\u062d\u0645\u062f\u06cc`.

- [ ] **Step 2: Run RED**

Run: `npm.cmd run test:unit -- test/vitest/__tests__/i18n-encoding.spec.js`

Expected: FAIL because the current UTF-8 files contain mojibake marker characters.

- [ ] **Step 3: Apply only the source-character repair**

Restore the corrupted new dictionary values and the HomeFeatured arrow to their intended Unicode characters. Do not alter prior, correctly rendered translations.

- [ ] **Step 4: Run GREEN**

Run: `npm.cmd run test:unit -- test/vitest/__tests__/i18n-encoding.spec.js`

Expected: PASS with strict UTF-8 decoding and no marker matches.

### Task 2: Measure and polish responsive home behavior

**Files:**
- Modify only after browser evidence: `frontend/src/components/public/SiteHeader.vue`, `frontend/src/components/public/home/HomeHero.vue`, and any directly evidenced home section component.
- Test: existing public home and public shell Vitest specs.

- [ ] **Step 1: Capture baseline at `/`, `/en`, and `/fa`**

Inspect 1440x900, 1024x768, 936x900, and 390x844. Record title/route, direction, drawer open/close/Escape, language switching, overflow, clipping, header overlap, console errors, and CMS state.

- [ ] **Step 2: Make the smallest token-based CSS changes supported by the screenshots**

Use the existing token scale and logical properties to show desktop navigation where it fits, compact the descriptor at intermediate sizes, reduce tablet hero scale and empty space, tune Persian type separately, preserve scroll padding, and render a text-led Visual Archive fallback when no actual media is available.

- [ ] **Step 3: Re-run the baseline interaction/viewport matrix**

Reload after the edits and collect desktop, Persian desktop, English mobile, Persian mobile, and open-drawer screenshots outside the repository.

### Task 3: Verify and commit narrow slices

**Files:** exact changed test, dictionary, and component paths only.

- [ ] **Step 1: Run focused tests**

Run the encoding test, public-home redesign tests, public-shell tests, public-page/SSR tests, then the full `npm.cmd run test:unit` suite from `frontend/`.

- [ ] **Step 2: Run build and final repository gates**

Run `npm.cmd run build`, `git diff --check`, exact staged-path inspection, and final `git status --short`.

- [ ] **Step 3: Commit only after all gates pass**

Create `fix(frontend): repair public i18n encoding`, then, if visual changes are independently clean, create `fix(frontend): polish responsive public home`; otherwise create the single combined commit requested by the task.
