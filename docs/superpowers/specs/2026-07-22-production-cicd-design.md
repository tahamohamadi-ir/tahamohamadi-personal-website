# Production CI/CD Design

## Goal

Provide secure, auditable, approval-gated Production deployment without opening SSH access to GitHub-hosted runners.

## Selected approach

Use GitHub-hosted CI, immutable GHCR images, manual approval through the `production` environment, immutable Production tags, and a pull-based systemd deployment agent on the VPS.

## Components

1. GitHub validates Backend, Frontend, Dockerfiles, Bash scripts, and Production Compose.
2. GitHub publishes Backend and Frontend images tagged with the exact commit SHA.
3. Manual approval creates a unique immutable Production tag.
4. The VPS polls Production tags every minute.
5. The VPS backs up Production before deployment.
6. The VPS deploys exact SHA images.
7. Origin and public endpoints are validated.
8. Failure restores the previous repository commit and image pair.
9. Caddy reports the deployed SHA at `/deployment-version`.
10. GitHub verifies the exact SHA before marking deployment successful.

## Constraints

- Do not expose PostgreSQL, Backend, or Frontend ports publicly.
- Do not remove or recreate Production volumes.
- Do not store application secrets in GitHub.
- Do not use a self-hosted runner on Production.
- Do not allow GitHub-hosted runners direct SSH access.
- Do not use mutable image tags for Production deployment.
- Do not force-push branches or deployment tags.
