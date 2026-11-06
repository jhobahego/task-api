# Feature: Testcontainers-backed test suite (TDD)

## Objective

Build a layered automated test suite (unit, web slice, repository slice, E2E) backed by a real PostgreSQL container, fix the two known defects through TDD, and document the project state.

## Problem

The project only has `TaskApiApplicationTests.contextLoads`. The test points at the local PostgreSQL instance (`localhost:5433`) configured in `src/main/resources/application.properties`, so `./mvnw test` is not self-contained. Service rules, controller contracts, and native repository queries have no coverage.

## Why

- Enable TDD with production parity: real PostgreSQL instead of H2.
- Prevent regressions in business rules (duplicate title, minimum length, completion toggle).
- Make the suite runnable locally and in CI with Docker as the only external prerequisite.
- Fix two defects discovered during exploration.

## Scope

- Testcontainers infrastructure using `postgres:16-alpine`.
- Unit tests for `TareaServicio` and `TareaInDTOaTarea`.
- Web slice tests for `TareaControlador` with `MockMvc`.
- Repository slice tests for native queries and constraints.
- E2E integration test over HTTP.
- Production fixes: `Location` header slash and Bean Validation on `TareaInDTO`.
- JaCoCo coverage reporting.
- README with current project state, committed last.

## Constraints

- Spring Boot 3.5.16, Java 17, Maven wrapper (`./mvnw`, Maven 3.8.7).
- Testcontainers 1.21.4 is managed by the Boot 3.5.16 BOM; do not pin it.
- `@ServiceConnection` is available in Boot 3.5.16, but the existing singleton `@DynamicPropertySource` base class remains valid and is kept.
- Controller slice tests use `@MockitoBean` (`@MockBean` is deprecated since Boot 3.4).
- Tests must never use the local PostgreSQL at `localhost:5433`.
- Strict TDD for behavior changes: observe RED, implement GREEN, refactor while green. Pure infrastructure/configuration changes document the exception and use structural verification.
- The README commit must be the last commit, with the exact message: `Update: Readme con el estado actual del proyecto`.

## Tasks

| ID | Task | Route | Status |
| --- | --- | --- | --- |
| T0 | Upgrade Spring Boot 3.0.8 → 3.5.16 for Testcontainers BOM management | delegated | completed |
| T1 | Add Testcontainers dependencies and `lombok.config` | delegated | completed |
| T2 | Create `PostgresContainerBase` singleton support class | delegated | completed |
| T3 | Adapt `TaskApiApplicationTests` to run against the container | delegated | completed |
| T4 | Service unit tests with Mockito | delegated | completed |
| T5 | Mapper unit test | delegated | completed |
| T6 | DTO Bean Validation (`@NotBlank`, `@Size(min = 4)`, `@Valid`) and null-safe service guard | delegated | completed |
| T7 | Fix `Location` header and add controller slice tests | delegated | completed |
| T8 | Repository slice tests including native queries | delegated | completed |
| T9 | E2E integration test | delegated | completed |
| T10 | JaCoCo coverage report | delegated | completed |
| T11 | README and final commit | delegated | completed |

## Authorized scope

- `pom.xml`, `lombok.config`
- `src/test/java/com/jhobadev/taskapi/**`
- `src/main/java/com/jhobadev/taskapi/dtos/TareaInDTO.java`
- `src/main/java/com/jhobadev/taskapi/controlador/TareaControlador.java`
- `src/main/java/com/jhobadev/taskapi/servicio/TareaServicio.java`
- `README.md`

Production changes are limited to the two authorized fixes. No unrelated refactors.

## Acceptance criteria

- `./mvnw test` passes with Docker running and does not require the local PostgreSQL.
- Unit and web slice tests do not start a container.
- Repository and E2E tests run against the real PostgreSQL container.
- `POST /api/tareas` returns `Location: /api/tareas/{id}`.
- Null, blank, or shorter-than-four-character titles return HTTP 400 without an NPE.
- JaCoCo HTML report is generated under `target/site/jacoco/`.
- `README.md` documents the stack, endpoints, configuration, and test commands.

## Checks

- `./mvnw test`
- `./mvnw test -Dtest=<FocusedTest>`
- `./mvnw verify`
- `docker info` (prerequisite)

## Delivery

- Authored-line forecast: approximately 700-900 lines, mostly tests.
- Delivery strategy: `ask-on-risk`.
- No PR requested. README commit is the final commit.

## Progress

- 2026-11-06: Feature document created. Plan approved by the user. Decisions frozen: `Location` with slash; `@NotBlank` + `@Size(min = 4)` + `@Valid`.
- 2026-11-06: User chose plan option 4 (revisit the plan). Approved target: Spring Boot 3.5.16.
- 2026-11-06: T0-T3 completed. Parent upgraded to 3.5.16; versionless Testcontainers dependencies resolve to 1.21.4; `contextLoads` passes against `postgres:16-alpine`.
- 2026-11-06: Worker flagged the explicit PostgreSQL JDBC 42.6.0 pin as CVE-2024-1597-adjacent. Removed the pin; Boot 3.5.16 now manages PostgreSQL JDBC 42.7.11.
- 2026-11-06: T4-T5 completed. 11 service characterization tests and 1 mapper test added. Full suite: 13 tests green, including the Testcontainers context load.
- 2026-11-06: T6-T7 completed with strict TDD. RED: 2 failures and 3 errors (null-title NPE, invalid payloads reaching the controller, wrong Location). GREEN: validation annotations, `@Valid`, slash fix, and null-safe service guard. Full suite: 23 tests green.
- 2026-11-06: T8-T9 completed. Repository slice (7 tests) and HTTP E2E (4 tests) added against the shared PostgreSQL container. Full suite: 34 tests green.
- 2026-11-06: T10 completed. JaCoCo 0.8.16 added with an 80% line coverage gate; `./mvnw verify` passes at 93.94% line coverage (62/66 lines, 100% branch coverage).
- 2026-11-06: T11 completed. README created in English with the current project state. Work committed on branch `feat/testcontainers-tdd` in five commits: four work units plus this ODD document. The README commit is the final commit, with the exact requested message.
- 2026-11-06: RDD native review could not run on the OpenCode runtime (`unassessable`: immutable receipt review supports claude-code and codex only). No review receipt was produced; verification is the 34-test suite and the JaCoCo gate. RDD mode was not changed.

## Verification evidence

- `./mvnw test`: BUILD SUCCESS, Spring Boot 3.5.16, Hibernate 6.6.53.Final, Testcontainers 1.21.4, PostgreSQL 16.15, Tests run: 1, Failures: 0, Errors: 0, Skipped: 0.
- `./mvnw dependency:tree -Dincludes=org.testcontainers`: `org.testcontainers:postgresql:1.21.4:test` and `org.testcontainers:junit-jupiter:1.21.4:test`.
- `./mvnw test -Dtest=TareaServicioTest,TareaInDTOaTareaTest`: Tests run: 12, Failures: 0, Errors: 0, Skipped: 0.
- `./mvnw dependency:tree -Dincludes=org.postgresql`: `org.postgresql:postgresql:jar:42.7.11:compile`.
- `./mvnw test`: Tests run: 13, Failures: 0, Errors: 0, Skipped: 0.
- `./mvnw test -Dtest=TareaServicioTest,TareaControladorTest` (RED): Tests run: 21, Failures: 2, Errors: 3 — null-title NPE, invalid payloads reaching the controller, and `Location` `/api/tareas5`.
- `./mvnw test -Dtest=TareaServicioTest,TareaControladorTest` (GREEN): Tests run: 21, Failures: 0, Errors: 0, Skipped: 0.
- `./mvnw test`: Tests run: 23, Failures: 0, Errors: 0, Skipped: 0.
- `./mvnw test -Dtest=TareaRepositorioTest`: Tests run: 7, Failures: 0, Errors: 0, Skipped: 0.
- `./mvnw test -Dtest=TaskApiIntegrationTest`: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0.
- `./mvnw test`: Tests run: 34, Failures: 0, Errors: 0, Skipped: 0.
- `./mvnw verify`: BUILD SUCCESS, Tests run: 34, JaCoCo check met, line coverage 93.94% (62/66), branch coverage 100% (12/12).
- `ls -l target/site/jacoco/index.html`: report generated.

## Next step

Feature complete on branch `feat/testcontainers-tdd`. The README commit is the final commit. `.atl/` and `errores-mcps.png` remain untracked and are not part of the feature.
