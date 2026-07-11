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

## Lombok Policy

Lombok is approved for this project.

Rules:

1. Lombok may be used for entities, DTO classes, configuration properties, builders, and simple data containers.
2. Prefer targeted annotations such as `@Getter`, `@Setter`, `@Builder`, and constructor annotations.
3. Avoid `@Data` on JPA entities.
4. Do not generate `equals`, `hashCode`, or `toString` for JPA relationships without explicit review.
5. Do not expose sensitive fields such as passwords or tokens through generated `toString`.
6. Use Java records where they are simpler and appropriate for immutable API DTOs.

## Lombok Policy

Lombok is approved for this project and must not be removed unless explicitly requested by the project owner.

Rules:

1. Lombok may be used for entities, DTO classes, configuration properties, builders, and simple data containers.
2. Prefer targeted annotations such as @Getter, @Setter, @Builder, @NoArgsConstructor, and @RequiredArgsConstructor.
3. Avoid @Data on JPA entities.
4. Do not generate equals, hashCode, or 	oString across JPA relationships without explicit review.
5. Exclude sensitive fields such as passwords, tokens, and secrets from generated 	oString methods.
6. Use Java records when they are simpler for immutable request or response DTOs.
7. Lombok must remain optional and excluded from the executable Spring Boot artifact.
