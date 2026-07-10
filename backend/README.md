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

The `local` profile uses environment-backed placeholders:

- `TAHA_DB_URL`, defaults to `jdbc:postgresql://localhost:5432/taha_site`
- `TAHA_DB_USERNAME`, defaults to `taha_site`
- `TAHA_DB_PASSWORD`, defaults to `taha_site`
- `TAHA_BACKEND_PORT`, defaults to `8080`

## Commands

```powershell
mvn test
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The current skeleton does not create database tables or Flyway migrations yet.
