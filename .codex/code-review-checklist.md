# Code Review Checklist

## General

- [ ] The change references a requirement, task, or issue.
- [ ] The scope is limited to the requested task.
- [ ] No unrelated refactor is included.
- [ ] No unnecessary dependency is introduced.
- [ ] No overengineering is introduced.

## Backend

- [ ] DTOs are used for API input/output.
- [ ] JPA entities are not exposed directly.
- [ ] Input validation exists.
- [ ] Business logic is in service layer.
- [ ] Write operations are transactional.
- [ ] Exceptions are handled consistently.
- [ ] Pagination is used for list endpoints.
- [ ] Public APIs return only published content.

## Frontend

- [ ] Components are small and readable.
- [ ] Loading state exists.
- [ ] Empty state exists.
- [ ] Error state exists.
- [ ] Success feedback exists when needed.
- [ ] RTL/LTR behavior is correct.
- [ ] Public pages use semantic HTML.
- [ ] Admin forms are usable and clear.

## Security

- [ ] Admin API requires authentication.
- [ ] Authorization is enforced on backend.
- [ ] Sensitive data is not logged.
- [ ] Secrets are not committed.
- [ ] File upload is validated if changed.
- [ ] Rich text/Markdown output is sanitized.
- [ ] Contact form abuse is considered.
- [ ] Audit log is added for sensitive admin actions.

## Database

- [ ] Schema changes use Flyway migration.
- [ ] Constraints are explicit.
- [ ] Indexes are added where needed.
- [ ] Translation model is respected.
- [ ] Soft delete strategy is respected.

## SEO and i18n

- [ ] Public page metadata is handled.
- [ ] hreflang/canonical impact is considered.
- [ ] Persian RTL is handled.
- [ ] English LTR is handled.
- [ ] Missing translation behavior is handled.

## Testing

- [ ] Unit tests added or updated.
- [ ] Integration tests added when API/DB changed.
- [ ] Security negative tests added for protected endpoints.
- [ ] E2E smoke test updated when user flow changed.
- [ ] Tests pass locally.

## Documentation

- [ ] README or docs updated if behavior changed.
- [ ] ADR added if architecture changed.
- [ ] API docs updated if endpoint changed.
- [ ] Migration notes added if DB changed.
