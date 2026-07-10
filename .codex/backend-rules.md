# Backend Rules

## Stack

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation

## General Rules

1. Use package-by-feature structure.
2. Do not expose JPA entities directly in API responses.
3. Use DTOs for request and response models.
4. Validate all inputs with Bean Validation.
5. Use service layer for business logic.
6. Use repository layer only for data access.
7. Use transactions for write operations.
8. Use global exception handling.
9. Use consistent API error responses.
10. Add tests for critical logic.

## API Rules

1. Public APIs must be under `/api/v1/public`.
2. Admin APIs must be under `/api/v1/admin`.
3. Auth APIs must be under `/api/v1/auth`.
4. API responses must be predictable and documented.
5. Use pagination for list endpoints.
6. Never return unpublished content from public endpoints.
7. Validate `lang` values. Allowed values: `fa`, `en`.

## Security Rules

1. All admin APIs require authentication.
2. All admin APIs require role/permission checks.
3. Passwords must be hashed securely.
4. Never log passwords, tokens, or secrets.
5. File upload must validate extension, MIME type, and size.
6. Contact form and login must be rate-limited.
7. Sensitive admin actions must be recorded in audit logs.

## Error Handling

Use a standard error response:

- timestamp
- status
- error
- message
- path
- validationErrors, if applicable

Do not leak stack traces to clients.

## Testing

Required test types:

- Unit tests for services
- Integration tests for repositories and APIs
- Security tests for protected endpoints
- Validation tests for request DTOs
