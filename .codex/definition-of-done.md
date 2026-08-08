# Definition of Done

Use this checklist to declare the evidence-backed status of a task. A task may be `implemented` or `operational` with explicitly logged non-critical follow-up; only `release-verified` means all applicable broader validation is complete.

## Status declaration

- `implemented`: scoped implementation and focused verification are complete; any remaining validation is visible in the ledger.
- `operational`: all applicable non-deferrable security, data, publication, contract and deployment gates are complete.
- `release-verified`: operational plus the broader release validation relevant to the change is complete.

Use the status that the evidence supports. A task is not fully `release-verified` merely because its code was merged. For fast-track work, follow `docs/governance/fast-track-delivery.md` and record each deliberate gap in `docs/status/deferred-validation.md`. Never call a task operational or release-verified if an open `P0`/`P1` ledger item applies to that flow.

## Requirement

- [ ] Requirement ID or task ID is referenced.
- [ ] Acceptance criteria are clear.
- [ ] Scope is respected.
- [ ] Out-of-scope items are not implemented.

## Implementation

- [ ] Code is implemented.
- [ ] Code follows project architecture.
- [ ] Code follows related `.codex` rules.
- [ ] No unnecessary dependency is added.
- [ ] No overengineering is introduced.

## Backend

- [ ] DTOs are used.
- [ ] Validation is implemented.
- [ ] Authorization is enforced where needed.
- [ ] Error handling is consistent.
- [ ] Audit logging is added where needed.
- [ ] Database changes use Flyway.

## Frontend

- [ ] UI is responsive.
- [ ] Loading state exists.
- [ ] Empty state exists.
- [ ] Error state exists.
- [ ] RTL/LTR is correct.
- [ ] Public pages are SEO-aware.

## i18n

- [ ] Persian content path works.
- [ ] English content path works.
- [ ] Missing translation behavior is correct.
- [ ] Language-specific metadata is handled where needed.

## Security

- [ ] No secrets are committed.
- [ ] Auth and authorization are tested.
- [ ] Inputs are validated.
- [ ] XSS risk is handled.
- [ ] File upload risk is handled if applicable.
- [ ] The task does not defer auth/RBAC/CSRF, secrets, input/output safety, public publication protection, or data integrity.

## Testing

- [ ] Unit tests pass.
- [ ] Integration tests pass where needed.
- [ ] E2E/smoke tests pass where needed.
- [ ] Regression risks are considered.
- [ ] Any intentionally unrun relevant check is linked to a ledger entry with mitigation, owner and return trigger.

## Documentation

- [ ] Relevant docs are updated.
- [ ] API changes are documented.
- [ ] ADR is added for major decisions.
- [ ] README is updated if setup changes.

## Git

- [ ] Commit message is clear.
- [ ] Working tree is clean after commit.

## Fast-track handoff

- [ ] The reported status is `implemented`, `operational`, or `release-verified` and matches the evidence.
- [ ] Open ledger IDs and their user/release impact are named in the handoff.
