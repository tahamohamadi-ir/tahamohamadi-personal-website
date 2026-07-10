# Definition of Done

A task is done only when all relevant items below are satisfied.

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

## Testing

- [ ] Unit tests pass.
- [ ] Integration tests pass where needed.
- [ ] E2E/smoke tests pass where needed.
- [ ] Regression risks are considered.

## Documentation

- [ ] Relevant docs are updated.
- [ ] API changes are documented.
- [ ] ADR is added for major decisions.
- [ ] README is updated if setup changes.

## Git

- [ ] Commit message is clear.
- [ ] Working tree is clean after commit.
