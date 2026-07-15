# Workflow and verification

Load for implementation, review, tests, debugging, scope decisions, or browser validation.

## Scope boundary

- M1 public frontend is controlled by `plans/008-public-frontend-mvp.md` and the accepted baseline is `BACKEND_READY_FOR_FRONTEND`.
- Do not change backend contracts, backend implementation, migrations, deployment, plans, package files, or admin frontend unless the assigned task explicitly permits it.
- Modify only the requested files. Do not run Git or commit unless the human explicitly asks.
- Do not inspect build/dependency/output directories: `node_modules`, `dist`, `.quasar`, `target`, `coverage`, or `.git`.
- Report changed files, completed checks, and remaining risk concisely. Do not print full files, large diffs, or routine build output.

## RED -> GREEN -> REFACTOR

1. Identify the requirement and freeze the relevant API/UI contract in a focused test or verify an existing failing test.
2. Implement the smallest correct behavior. Do not weaken tests to fit it.
3. Run focused tests, then the full frontend suite and Quasar SSR build when execution is requested.
4. Refactor only repeated/proven patterns. Do not introduce a generic page renderer or abstraction prematurely.

## Human-run default checks

From `frontend/`:

```powershell
npm run test:unit
npm run build
```

When Playwright is installed and browser work is requested, verify `/language`, `/en`, `/fa`, and the changed localized content route. Check one main landmark, no hydration/Vue warnings, no 375px overflow, the skip link, mobile navigation, Escape/trigger-focus restoration, RTL/LTR, alternate-language behavior, and reduced motion. Ignore errors only when they clearly come from a browser extension.

## Review checklist

- Preserve accepted public API response/error and locale-alternate contracts.
- Add loading, empty, recoverable error, offline, and translation-unavailable states where applicable.
- Avoid sensitive logging and raw HTML rendering.
- Keep DOM, metadata, and route state deterministic across SSR and hydration.
- Confirm semantic-token, localization, landmark, heading, keyboard, focus, responsive, and motion requirements for the changed behavior.