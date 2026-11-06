# Task API

REST API for a task application. It exposes CRUD-style operations over tasks and is intended as the backend for the React + TypeScript frontend described in the project POM.

## Quick start

1. Install JDK 17+ and start a PostgreSQL instance (the default configuration expects port `5433`).
2. Create the `tareas` database or adjust the datasource properties in `src/main/resources/application.properties`.
3. Run `./mvnw spring-boot:run` and call `http://localhost:8080/api/tareas`.

## Stack

| Area | Technology |
|------|------------|
| Runtime | Java 17 |
| Framework | Spring Boot 3.5.16 (Spring Web, WAR packaging) |
| Persistence | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Database | PostgreSQL |
| Boilerplate | Lombok (`lombok.config` marks generated code for JaCoCo) |
| Tests | JUnit 5, Mockito, MockMvc, Testcontainers 1.21.4 |
| Coverage | JaCoCo Maven plugin 0.8.16 |
| Build | Maven wrapper (`./mvnw`, Maven 3.8.7) |

## Requirements

| Requirement | Why |
|-------------|-----|
| JDK 17+ | Compilation and runtime target |
| Docker | The test suite starts a PostgreSQL container |
| PostgreSQL | Only for running the application locally |

## Local configuration

`src/main/resources/application.properties` holds the local defaults:

| Property | Value |
|----------|-------|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5433/tareas` |
| `spring.datasource.username` | `postgres` |
| `spring.datasource.password` | `1234` |
| `spring.jpa.hibernate.ddl-auto` | `update` |

Create the `tareas` database before starting the application, or change these values to match your environment.

## Run the application

```bash
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080` by default.

## Endpoints

| Method | Path | Success | Notes |
|--------|------|---------|-------|
| `GET` | `/api/tareas` | `200` | Returns the list of tasks |
| `POST` | `/api/tareas` | `201` | Body `{"title":"..."}`; returns `Location: /api/tareas/{id}` and a `RespuestaAPI` body |
| `PATCH` | `/api/tareas/completar/{id}` | `204` | Toggles the completed flag |
| `DELETE` | `/api/tareas/eliminar/{id}` | `204` | Deletes one task |
| `DELETE` | `/api/tareas/eliminar-completadas` | `204` | Deletes all completed tasks |

`POST` response body (`RespuestaAPI`):

```json
{
  "tarea": {
    "id": 1,
    "title": "Buy milk",
    "completed": false
  },
  "mensaje": "Tarea creada correctamente",
  "exito": true
}
```

## Validation and errors

| Rule | Behavior |
|------|----------|
| Title is required (`@NotBlank`) | Blank or missing title returns `400` |
| Minimum length is 4 characters (`@Size(min = 4)`) | Shorter titles return `400` |
| Title is unique | Duplicate title returns `400` |
| Task must exist for completion or deletion | Unknown task id returns `404` |

Validation is enforced with `@Valid` on the controller request body, with a null-safe guard in the service. Errors are returned by the shared exception handler.

## Tests

The suite is layered and runs against a real PostgreSQL container. It never uses the local database at `localhost:5433`.

| Command | Purpose |
|---------|---------|
| `./mvnw test` | Runs the full suite; requires Docker and Testcontainers |
| `./mvnw test -Dtest=TareaServicioTest` | Runs a single focused test class |
| `./mvnw verify` | Runs the tests plus the 80% line coverage gate |

Coverage report: `target/site/jacoco/index.html`. The latest `./mvnw verify` report shows **93% total line coverage** (62 of 66 lines).

### Test layers

| Layer | Class | What it covers |
|-------|-------|----------------|
| Unit | `TareaServicioTest` | Service rules with mocked repository |
| Unit | `TareaInDTOaTareaTest` | DTO-to-entity mapping |
| Web slice | `TareaControladorTest` | HTTP status, headers, validation, and payloads with MockMvc |
| Repository slice | `TareaRepositorioTest` | Native queries and database constraints |
| HTTP E2E | `TaskApiIntegrationTest` | Full HTTP flow against the container |
| Context load | `TaskApiApplicationTests` | Spring context startup against the container |

### Testcontainers

`PostgresContainerBase` starts one `postgres:16-alpine` container per Maven JVM and maps it to a random host port, so parallel test runs do not collide. It points Spring at the container with `spring.datasource.*` dynamic properties and uses `ddl-auto=create-drop` for test isolation.
