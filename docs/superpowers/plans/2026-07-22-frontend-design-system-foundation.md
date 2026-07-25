# Frontend Design System Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Build the governed, Quasar-first design-system foundation used by both public and admin surfaces without redesigning the Home page yet.

**Architecture:** Human-readable rules live in `docs/design.md`; machine-readable values live in `docs/design-system/design-tokens.json`. Sass maps primitive and semantic tokens into Quasar variables and runtime CSS properties. Shared `Tm*` primitives centralize recurring visual and accessibility behavior. Public and admin surfaces share the foundation but apply different density layers.

**Tech Stack:** Vue 3, Quasar 2, Quasar CLI with Vite, SSR, SCSS, Vue Router, Vue i18n, Pinia, Vitest.

## Global Constraints

- Keep Vue 3 and Quasar SSR.
- Release theme is light-first.
- Implement dark infrastructure but do not expose a public toggle.
- Persian and English are first-class.
- CSS handles layout; `$q.screen` handles behavioral differences only.
- No untracked colors, spacing, radius, shadows or motion durations.
- No new App Extension without a separate audit.
- No default gradient, particles, custom cursor, fake metrics or unapproved visible copy.
- Do not commit, push, merge, tag or deploy without explicit operator authorization for that exact action.

---

## File map

### Documentation already prepared

- `docs/design.md`
- `docs/design-system/design-tokens.json`
- `docs/design-system/quasar-capability-matrix.md`
- `docs/design-system/component-inventory.md`
- `docs/design-system/agent-ui-rules.md`
- `docs/design-system/visual-qa-checklist.md`

### Foundation files to create or modify

- Create: `frontend/src/css/quasar.variables.scss`
- Create: `frontend/src/css/tokens/_primitive.scss`
- Create: `frontend/src/css/tokens/_semantic-light.scss`
- Create: `frontend/src/css/tokens/_semantic-dark.scss`
- Create: `frontend/src/css/tokens/_typography.scss`
- Create: `frontend/src/css/tokens/_layout.scss`
- Create: `frontend/src/css/tokens/_motion.scss`
- Create: `frontend/src/css/foundations/_reset.scss`
- Create: `frontend/src/css/foundations/_themes.scss`
- Create: `frontend/src/css/foundations/_accessibility.scss`
- Create: `frontend/src/css/foundations/_rtl.scss`
- Create: `frontend/src/css/foundations/_utilities.scss`
- Modify: `frontend/src/css/app.scss`
- Modify: `frontend/quasar.config.js`
- Create: `frontend/src/boot/theme.js`
- Create: `frontend/src/components/shared/TmButton.vue`
- Create: `frontend/src/components/shared/TmMediaFrame.vue`
- Create: `frontend/src/components/shared/TmStatusPanel.vue`
- Create: `frontend/src/components/shared/TmSectionHeader.vue`
- Create: `frontend/src/pages/admin/AdminDesignSystemPage.vue`
- Modify: `frontend/src/router/routes.js`
- Create: `frontend/test/vitest/__tests__/design-tokens-contract.spec.js`
- Create: `frontend/test/vitest/__tests__/tm-button.spec.js`
- Create: `frontend/test/vitest/__tests__/tm-status-panel.spec.js`
- Create: `frontend/test/vitest/__tests__/design-system-route.spec.js`
- Modify or add: locale message files for design-system state labels only where existing labels cannot be reused.

---

### Task 1: Add contract tests for documentation and token values

**Files:**
- Test: `frontend/test/vitest/__tests__/design-tokens-contract.spec.js`
- Consume: `docs/design-system/design-tokens.json`

**Produces:** a test contract that blocks accidental changes to release theme, Quasar brand colors and prohibited features.

- [ ] **Step 1: Write the failing contract test**

The test must load the JSON from the repository root and assert:

```js
expect(tokens.strategy).toMatchObject({
  releaseTheme: 'light',
  darkInfrastructure: true,
  publicDarkToggle: false,
  quasarFirst: true
})

expect(tokens.quasarBrand).toEqual({
  primary: '#0B6E69',
  secondary: '#2457D6',
  accent: '#D96C4A',
  dark: '#0B1117',
  positive: '#16805B',
  negative: '#B42318',
  info: '#1D70B8',
  warning: '#A15C00'
})

expect(tokens.restrictions).toMatchObject({
  defaultGradient: false,
  permanentParticles: false,
  fakeMetrics: false,
  customCursor: false,
  scrollHijacking: false
})
```

- [ ] **Step 2: Run the targeted test and confirm the expected failure**

Run from `frontend`:

```bash
npm run test:unit -- test/vitest/__tests__/design-tokens-contract.spec.js
```

Expected: FAIL until the repository documentation package is copied into place or the path resolution is implemented.

- [ ] **Step 3: Correct path resolution without duplicating token values**

Use URL/path resolution compatible with Vitest ESM. Do not copy the JSON into frontend.

- [ ] **Step 4: Re-run the targeted test**

Expected: PASS.

- [ ] **Step 5: Stop for review**

Report files and test evidence. Do not commit automatically.

---

### Task 2: Map design tokens into Quasar Sass variables

**Files:**
- Create: `frontend/src/css/quasar.variables.scss`
- Create: token Sass files listed in the file map
- Test: extend `design-tokens-contract.spec.js`

**Produces:** Quasar build-time variables and runtime custom properties using the same approved values.

- [ ] **Step 1: Extend the failing test**

Read `quasar.variables.scss` as text and assert that the eight Quasar variables contain the approved values:

```scss
$primary: #0B6E69;
$secondary: #2457D6;
$accent: #D96C4A;
$dark: #0B1117;
$positive: #16805B;
$negative: #B42318;
$info: #1D70B8;
$warning: #A15C00;
```

- [ ] **Step 2: Run the test**

Expected: FAIL because the file does not exist.

- [ ] **Step 3: Create `quasar.variables.scss`**

Include only governed Quasar overrides and any required imports. Add a comment that `docs/design-system/design-tokens.json` is authoritative.

- [ ] **Step 4: Create primitive and semantic Sass maps**

Each file must have one responsibility:

- primitive palette and scales;
- light semantic aliases;
- dark semantic aliases;
- typography;
- layout;
- motion.

- [ ] **Step 5: Generate CSS custom properties from the semantic maps**

Do not duplicate values manually between `_semantic-light.scss` and runtime theme selectors.

- [ ] **Step 6: Run the targeted test**

Expected: PASS.

- [ ] **Step 7: Run an SSR build**

```bash
npm run build
```

Expected: successful Quasar SSR build.

- [ ] **Step 8: Stop for review**

Do not commit automatically.

---

### Task 3: Establish light and dark theme infrastructure

**Files:**
- Create: `frontend/src/css/foundations/_themes.scss`
- Create: `frontend/src/boot/theme.js`
- Modify: `frontend/quasar.config.js`
- Test: `design-tokens-contract.spec.js`

**Produces:** release-one fixed light mode with dark token infrastructure and no public toggle.

- [ ] **Step 1: Write failing source-contract assertions**

Assert:

- the theme boot file calls the Quasar Dark API with light disabled;
- the public toggle component or visible label is not introduced;
- `quasar.config.js` registers the theme boot file and Dark plugin if required by the chosen Quasar API;
- both `body--light` and `body--dark` selectors exist.

- [ ] **Step 2: Run the targeted test**

Expected: FAIL.

- [ ] **Step 3: Implement the boot file**

The boot file must:

- force light mode for release one;
- remain SSR-safe;
- contain a clear comment explaining how dark QA may be enabled locally;
- not read or persist a user preference yet.

- [ ] **Step 4: Implement theme selectors**

Map semantic CSS properties under:

```scss
.body--light { ... }
.body--dark { ... }
```

Do not use pure black in dark mode.

- [ ] **Step 5: Run the test and SSR build**

Expected: PASS.

- [ ] **Step 6: Stop for review**

Do not commit automatically.

---

### Task 4: Replace legacy global foundation wiring

**Files:**
- Create: foundation Sass files
- Modify: `frontend/src/css/app.scss`
- Preserve: existing accessibility and logical-property behavior

**Produces:** one ordered global stylesheet entrypoint.

- [ ] **Step 1: Write a failing style-contract test**

Assert that `app.scss` imports or uses modules in this order:

1. tokens;
2. reset;
3. themes;
4. typography;
5. layout;
6. accessibility;
7. RTL;
8. utilities;
9. legacy component styles still required during migration.

- [ ] **Step 2: Run the test**

Expected: FAIL.

- [ ] **Step 3: Implement foundation modules**

Preserve:

- `box-sizing`;
- document sizing;
- route focus behavior;
- existing reduced-motion handling;
- logical properties;
- current minimum width;
- current SSR-safe behavior.

Do not delete existing public styles until their consumers are migrated.

- [ ] **Step 4: Add transitional aliases**

Existing `--tm-*` variables must continue to resolve so current pages do not visually break before their redesign.

- [ ] **Step 5: Run the full frontend test suite**

```bash
npm run test:unit
```

Expected: all tests pass.

- [ ] **Step 6: Run SSR build**

Expected: PASS.

- [ ] **Step 7: Stop for review**

Do not commit automatically.

---

### Task 5: Build `TmButton`

**Files:**
- Create: `frontend/src/components/shared/TmButton.vue`
- Test: `frontend/test/vitest/__tests__/tm-button.spec.js`

**Interfaces:**

```ts
variant: 'primary' | 'secondary' | 'quiet' | 'text' | 'destructive'
size: 'sm' | 'md' | 'lg'
loading: boolean
disabled: boolean
to?: string | object
href?: string
target?: string
type?: 'button' | 'submit' | 'reset'
```

Emits `click` when actionable.

- [ ] **Step 1: Write failing tests**

Cover:

- default variant and size;
- router destination;
- external link attributes;
- loading state;
- disabled state;
- accessible name from slot;
- no pill class;
- no gradient style;
- click suppression while disabled/loading.

- [ ] **Step 2: Run targeted test**

Expected: FAIL.

- [ ] **Step 3: Implement minimal component**

Use `QBtn` as the behavioral base. Map variants to governed classes and tokens.

- [ ] **Step 4: Run targeted test**

Expected: PASS.

- [ ] **Step 5: Run frontend tests and SSR build**

Expected: PASS.

- [ ] **Step 6: Stop for visual review**

Render all variants in both directions. Do not commit automatically.

---

### Task 6: Build status and content primitives

**Files:**
- Create: `TmMediaFrame.vue`
- Create: `TmStatusPanel.vue`
- Create: `TmSectionHeader.vue`
- Test: `tm-status-panel.spec.js`

**Produces:** shared loading/error/empty treatment and stable media/section anatomy.

- [ ] **Step 1: Write failing tests for `TmStatusPanel`**

Cover the documented variants and ensure retry emits only where applicable.

- [ ] **Step 2: Write failing tests for media semantics**

Cover alt text, decorative images, aspect ratio and failure slot.

- [ ] **Step 3: Implement the primitives**

Use Quasar components where they add behavior. Keep public visuals token-driven.

- [ ] **Step 4: Run targeted tests**

Expected: PASS.

- [ ] **Step 5: Run frontend tests and SSR build**

Expected: PASS.

- [ ] **Step 6: Stop for review**

Do not migrate every page in this task.

---

### Task 7: Add an authenticated design-system showcase

**Files:**
- Create: `frontend/src/pages/admin/AdminDesignSystemPage.vue`
- Modify: `frontend/src/router/routes.js`
- Test: `design-system-route.spec.js`

**Produces:** a private admin route for reviewing tokens, controls, states, typography, themes and RTL/LTR.

Suggested route:

```text
/admin/design-system
```

- [ ] **Step 1: Write failing route tests**

Assert:

- route exists;
- it requires admin;
- it is `noindex`;
- public navigation does not expose it.

- [ ] **Step 2: Run targeted test**

Expected: FAIL.

- [ ] **Step 3: Implement the page**

Show:

- color palette;
- typography;
- spacing;
- buttons;
- status panels;
- media frames;
- forms using current Quasar components;
- light theme;
- a local dark-theme preview container without changing global public mode;
- Persian and English samples;
- reduced-motion note.

Do not build a production theme toggle.

- [ ] **Step 4: Run targeted and full tests**

Expected: PASS.

- [ ] **Step 5: Run SSR build**

Expected: PASS.

- [ ] **Step 6: Perform browser QA**

Verify authenticated desktop and mobile layouts.

- [ ] **Step 7: Stop for review**

Do not commit automatically.

---

### Task 8: Foundation visual and accessibility gate

**Files:**
- Update: `docs/design-system/visual-qa-checklist.md` evidence table for this implementation
- No unrelated source changes

- [ ] **Step 1: Start SSR development mode using the repository’s documented backend-origin setup**

- [ ] **Step 2: Capture desktop and mobile screenshots of the design-system route**

- [ ] **Step 3: Verify Persian and English samples**

- [ ] **Step 4: Verify keyboard navigation and focus**

- [ ] **Step 5: Verify reduced motion**

- [ ] **Step 6: Temporarily inspect dark-preview states**

No public toggle or persisted dark preference is allowed.

- [ ] **Step 7: Run the full verification set**

```bash
npm run test:unit
npm run build
```

- [ ] **Step 8: Produce a fidelity ledger**

Record at least:

- typography;
- colors;
- controls;
- surfaces;
- RTL;
- mobile behavior;
- dark-preview readability.

- [ ] **Step 9: Stop for operator approval**

Do not commit, push, open a pull request or deploy until the operator explicitly authorizes those exact actions.

---

## Plan self-review

### Spec coverage

Covered:

- Quasar-first architecture;
- light-first/dark-ready;
- shared brand foundation;
- public/admin differentiation;
- machine-readable tokens;
- shared primitives;
- RTL/LTR;
- accessibility;
- SSR;
- testing;
- visual QA;
- agent safety.

Not in this plan:

- Home redesign;
- inner-page redesign;
- admin workflow redesign beyond the design-system showcase;
- public dark-mode toggle;
- App Extension installation.

These belong to later, independently reviewable plans.
