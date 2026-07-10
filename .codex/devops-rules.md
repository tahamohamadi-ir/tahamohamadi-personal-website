# DevOps Rules

## Deployment Strategy

MVP deployment target:

- Docker Compose on VPS

Do not use Kubernetes for MVP unless explicitly required.

## Environment Strategy

Required environments:

- local
- staging
- production

Each environment must have separate configuration.

## Configuration Rules

1. Use environment variables for secrets.
2. Never commit `.env` files.
3. Provide `.env.example`.
4. Keep production secrets outside Git.
5. Use clear variable names.

## Docker Rules

1. Backend must have a Dockerfile.
2. Frontend must have a Dockerfile.
3. Local development must support Docker Compose.
4. PostgreSQL must run through Docker Compose locally.
5. Uploaded files must use a mounted volume.
6. PostgreSQL data must use a mounted volume.

## CI Rules

GitHub Actions should run:

- backend build
- backend tests
- frontend install
- frontend tests
- frontend build
- lint checks when available

## Release Rules

1. Merge to `main` should represent releasable state.
2. Use tags for releases.
3. Run database migrations before starting the new backend version.
4. Run health checks after deployment.
5. Keep rollback instructions documented.

## Backup Rules

Backup must include:

- PostgreSQL database
- uploaded media files
- important environment templates
- deployment configuration

Minimum MVP backup:

- manual backup script
- manual restore script
- documented restore process

## Health Check Rules

Backend must expose:

- application health
- database connectivity health

Frontend/reverse proxy must expose:

- HTTP availability check

## Logging Rules

1. Backend logs must include useful request and error context.
2. Do not log secrets.
3. Production logs must be accessible.
4. Admin actions must be auditable.

## Production Rules

Production must use:

- HTTPS
- secure headers
- restricted admin access
- database backup
- media backup
- monitored health checks
