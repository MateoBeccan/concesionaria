# Demo deployment

This project can be deployed as a single Spring Boot application. The production
JAR includes the Vue/Vite frontend and serves the API from the same host.

## Recommended free demo stack

- App: Render Free Web Service, Docker runtime.
- Database: Aiven Free MySQL.

## Aiven MySQL

Create a free MySQL service in Aiven, then copy the host, port, database, user,
and password.

Use this JDBC URL format:

```text
jdbc:mysql://HOST:PORT/DATABASE?sslMode=REQUIRED&useUnicode=true&characterEncoding=utf8&sessionVariables=sql_require_primary_key=0
```

Aiven MySQL can require primary keys for every created table. Liquibase creates
its internal `DATABASECHANGELOG` table without a primary key, so the demo URL
sets `sql_require_primary_key=0` for the application session.

## Render

Create a new Web Service from the GitHub repository. Render can read
`render.yaml`, or you can configure it manually:

- Runtime: Docker.
- Dockerfile path: `./Dockerfile`.
- Plan: Free.
- Health check path: `/management/health`.

Set these environment variables:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_LIQUIBASE_CONTEXTS=prod,bootstrap
DB_URL=jdbc:mysql://HOST:PORT/DATABASE?sslMode=REQUIRED&useUnicode=true&characterEncoding=utf8&sessionVariables=sql_require_primary_key=0
DB_USER=AVNADMIN_OR_USER
DB_PASSWORD=PASSWORD
JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET=GENERATED_64_BYTE_BASE64_SECRET
APP_SECURITY_REGISTRATION_PUBLIC_ENABLED=false
```

`JHIPSTER_MAIL_BASE_URL` can be set to `https://YOUR-RENDER-APP.onrender.com`
for email links, but it is optional for the demo while public registration is
disabled.

Generate a JWT secret locally, then copy it manually into Render as
`JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET`. The decoded secret must be
at least 64 random bytes for HS512.

```powershell
$bytes = New-Object byte[] 64
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$secret = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$secret
```

For demos, keep registration disabled and create/test users intentionally.

## Local production smoke test

With a local MySQL running and `.env.local` values loaded in your shell:

```powershell
.\mvnw.cmd -ntp verify -DskipTests --batch-mode -Pprod
java -jar target/concesionaria-0.0.1-SNAPSHOT.jar
```
