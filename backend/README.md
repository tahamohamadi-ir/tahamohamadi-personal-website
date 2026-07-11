# TahaMohamadi.ir Backend

Spring Boot backend skeleton for TahaMohamadi.ir.

## Stack

- Java 21 target
- Maven
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL Driver
- Flyway
- Bean Validation
- Actuator
- Spring Boot Test

## Package Structure

Backend code is organized by feature module under `ir.tahamohamadi`:

- `auth`
- `user`
- `content`
- `blog`
- `portfolio`
- `media`
- `seo`
- `analytics`
- `admin`
- `audit`
- `common`

## Local Configuration

The `local` profile uses environment-backed placeholders. The safe defaults match the root `compose.yaml` development database:

- `DB_HOST`, defaults to `localhost`
- `DB_PORT`, defaults to `5432`
- `DB_NAME`, defaults to `taha_site`
- `DB_USERNAME`, defaults to `taha_site`
- `DB_PASSWORD`, defaults to `taha_local_password`
- `TAHA_BACKEND_PORT`, defaults to `8080`

Flyway remains enabled and Hibernate uses `validate`; this skeleton does not create or update application tables.

## Commands

```powershell
# From the repository root
docker compose up -d postgres
docker inspect --format '{{.State.Health.Status}}' $(docker compose ps -q postgres)

# From backend/
.\mvnw.cmd test
$env:SPRING_PROFILES_ACTIVE = 'local'
.\mvnw.cmd spring-boot:run
```

Stop PostgreSQL from the repository root when local work is complete:

```powershell
docker compose down
```

Do not use `docker compose down -v`; the named local PostgreSQL volume is intentionally persistent. `.env` is ignored, while `.env.example` provides safe local defaults.

### Windows Port Troubleshooting

Some Windows, WSL2, or Hyper-V configurations reserve host port `5432`. Check with:

```powershell
netsh interface ipv4 show excludedportrange protocol=tcp
```

If `5432` is reserved, set both local variables to `55432`:

```text
POSTGRES_PORT=55432
DB_PORT=55432
```

This maps host port `55432` to PostgreSQL container port `5432`; the generic project default remains `5432`.
