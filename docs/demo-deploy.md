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
jdbc:mysql://HOST:PORT/DATABASE?sslMode=REQUIRED&useUnicode=true&characterEncoding=utf8
```

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
DB_URL=jdbc:mysql://HOST:PORT/DATABASE?sslMode=REQUIRED&useUnicode=true&characterEncoding=utf8
DB_USER=AVNADMIN_OR_USER
DB_PASSWORD=PASSWORD
JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET=GENERATED_BASE64_SECRET
JHIPSTER_MAIL_BASE_URL=https://YOUR-RENDER-APP.onrender.com
APP_SECURITY_REGISTRATION_PUBLIC_ENABLED=false
```

Generate a JWT secret locally with:

```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

For demos, keep registration disabled and create/test users intentionally.

## Local production smoke test

With a local MySQL running and `.env.local` values loaded in your shell:

```powershell
.\mvnw.cmd -ntp verify -DskipTests --batch-mode -Pprod
java -jar target/concesionaria-0.0.1-SNAPSHOT.jar
```
