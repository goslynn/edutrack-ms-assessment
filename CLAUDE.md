# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Responsabilidad del servicio

Assessment Service gestiona **evaluaciones** y **notas** del colegio: registro de
evaluaciones (nombre, fecha, ponderacion) por asignatura y periodo, notas en escala
chilena **1.0–7.0** por alumno, calculo de **promedio ponderado** por asignatura y
periodo, y un **log de auditoria inmutable** de cambios de notas. Schema BD
`assessment`. Path raiz `/assessment`. Implementa `BE-ASS-001..004`.

Spec fuente: `../openspec/specs/planned-services/spec.md` (+ `data-persistence`,
`authorization`, `api-conventions`, `error-handling`, `request-context`,
`inter-service-communication`).

## Stack

- Quarkus 3.35.3 + Java 21 (`quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`,
  `quarkus-hibernate-validator`, `quarkus-flyway`, `quarkus-jdbc-postgresql`,
  `quarkus-smallrye-openapi`, `quarkus-smallrye-health`, `quarkus-rest-client-jackson`).
- `edutrack-ms-commons` 1.0.0 (artefacto): aporta `infrastructure.*` (security, context,
  exception, jackson, validation, persistence, discovery) y `clients.*`. **No** se duplica
  `infrastructure/` aqui.
- PostgreSQL schema `assessment` (Shared DB). Flyway gestiona el DDL; Hibernate `validate` en dev.

## Comandos

```bash
./mvnw quarkus:dev          # dev mode (requiere DB)
./mvnw test                 # tests unitarios (JUnit5 + Mockito, sin Quarkus ni DB)
./mvnw clean package        # build
./mvnw verify               # tests de integracion (skipITs=true por defecto)
./mvnw test -Dtest=NombreDelTest
```

## Estructura

| Paquete | Contenido |
|---|---|
| `model.entity` | `Evaluation`, `Grade` (heredan `AuditableEntity`), `GradeAuditLog` (hereda `CreatableEntity`, inmutable). Value object `Score` (escala chilena). |
| `model.dto` | `EvaluationRequest/Response`, `GradeRequest/Response`, `GradeAuditLogResponse`, `AverageResponse`, `AssessmentValidations`. Max 2 DTOs por recurso; granularidad con `@JsonView` + validation groups; factory `fromEntity`/`of`. |
| `repository` | `EvaluationRepository`, `GradeRepository` (unicidad, listados, proyeccion `weightedScores`), `GradeAuditLogRepository` (`PanacheRepositoryBase`). |
| `service` | `EvaluationService`, `GradeService`, `GradeAuditService`, `AverageService`, `WeightedAverageCalculator` + `WeightedScore`, `StudentGateway` (+ `RemoteStudentGateway`), `AverageResult`. |
| `client` | `StudentClient` (rest-client declarativo hacia Student). |
| `resource` | `EvaluationResource` (`/evaluations` + notas anidadas), `GradeResource` (`/grades` + `/grades/{id}/history`), `AverageResource` (`/averages`). |
| `security` | `AssessmentResourceId` (resource keys `assessment.evaluations`, `assessment.grades`, `assessment.audit`). |

## Decisiones de diseno

- **Nota fuera de rango => `422`** (no `400`): la presencia del campo es Bean Validation
  (`@NotNull` => `400`), pero la validez del rango `1.0–7.0` es **invariante de dominio**
  (value object `Score`) y la spec exige `422` (`BE-ASS-001`), status que el handler de
  `ConstraintViolation` (400) no produce. Se lanza como `DomainException(422, ...)` en el
  servicio — mismo patron que el RUT en Student.
- **Auditoria inmutable** (`GradeAuditLog` extends `CreatableEntity`): append-only por
  diseno (sin `updated_at`, columnas `updatable=false`, sin endpoint de borrado). El "quien"
  y "cuando" viajan en `creatorUser`/`createdAt`; `GradeService` registra una entrada en
  cada alta (`oldValue=null`) y en cada cambio efectivo de nota (`BE-ASS-003`). La lectura
  del historial requiere `assessment.audit` (READ), grant reservado a ADMIN/SUPERUSER — el
  docente registra/corrige notas pero no puede leer ni manipular el log.
- **Adapter / fail-closed** (`StudentGateway` + `RemoteStudentGateway`): la integridad
  referencial con Student (`BE-ASS-004`) se verifica via REST (`GET /student/students/{id}`):
  `404` => alumno inexistente => `404` en Assessment; cualquier fallo de Student (timeout,
  5xx) => error, nunca se asume que el alumno existe (no se crean notas huerfanas). El gateway
  es una abstraccion CDI para testear `GradeService` con un mock.
- **Calculo puro** (`WeightedAverageCalculator`): promedio ponderado
  `sum(nota*pond)/sum(pond)`, redondeo `HALF_UP` a 1 decimal, con casos de prueba
  documentados (`BE-ASS-002`). El repositorio proyecta los pares `(nota, ponderacion)`
  cruzando cada nota con su evaluacion.
- **Asociacion intra-schema vs id opaco**: `Grade -> Evaluation` es `@ManyToOne` LAZY
  (mismo schema). `studentId`/`subjectId` son ids opacos de otros MS (sin FK): su validez
  vive en el MS dueno.

## Convenciones de plataforma (heredadas del monorepo)

- Identidad del request: inyectar `RequestContext` (commons). Prohibido leer cabeceras `X-...` a mano.
- Autorizacion: `@RequirePermission(resource = AssessmentResourceId.X, value = Permission.READ|WRITE)`.
  El `PermissionEvaluator`/`SuperUserResolver` por defecto (commons) consultan a Auth via `GET /auth/access`.
- Errores: `DomainException` + sugar (`ConflictException`/`NotFoundException`) con `code` estable
  `ASSESSMENT.<ENTIDAD>.<CONDICION>`; el handler global (commons) los serializa al envelope `ErrorResponse`.
- Auditoria: `creator_user`/`updater_user` los rellena `AuditContext` (commons) desde `RequestContext`.
- Validacion: solo Bean Validation (`@Valid` + grupos). La validez del rango de nota es la
  excepcion deliberada (invariante de dominio => `422`, no `400`).

## Pendientes conocidos

- Seed de los grants (`assessment.evaluations`, `assessment.grades`, `assessment.audit`) en Auth —
  en particular `assessment.audit` solo para ADMIN/SUPERUSER. (No se siembra cross-schema desde
  este MS; mismo pendiente que Student/Course.)
- Wiring en `docker-compose.yml` (contenedor `assessment` + credenciales del schema) y `fly.toml`.
- Tests de integracion (`@QuarkusTest` + RestAssured) contra DB y un Student stub.
