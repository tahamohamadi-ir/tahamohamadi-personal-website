# Feature flags and rollout boundary

## Current configuration

Spring Boot binds the following non-secret environment variables to `taha.features.*-enabled`. Their effective values are visible through `GET /api/v1/public/features` and default to `true` when the variable is absent.

| Environment variable | Public key | Default |
|---|---|---|
| `TAHA_FEATURES_HOME_V2_ENABLED` | `homeV2` | `true` |
| `TAHA_FEATURES_COMPOSER_CANVAS_ENABLED` | `composerCanvas` | `true` |
| `TAHA_FEATURES_ARTICLE_BLOCK_EDITOR_ENABLED` | `articleBlockEditor` | `true` |
| `TAHA_FEATURES_PORTFOLIO_CASE_STUDY_ENABLED` | `portfolioCaseStudy` | `true` |
| `TAHA_FEATURES_WORKFLOW_SCHEDULING_ENABLED` | `workflowScheduling` | `true` |

## Important current limitation

At this point the flags are exposed by `FeatureFlagService` and the public features endpoint, but they do not yet gate a controller, public renderer, Admin navigation, or Admin workflow. Therefore changing a variable is **not** a rollback mechanism and must not be presented as one.

For a capability to use a flag safely, its own implementation slice must define all of these together:

1. Backend behavior when the flag is off, without exposing a private route.
2. Public/Admin navigation and SSR behavior when the flag is off.
3. A focused on/off contract test.
4. A rollback owner and deployment runbook.

The outstanding wiring and verification is tracked as `CMS-R0-FLAGS-004` in [the deferred-validation ledger](../status/deferred-validation.md).
