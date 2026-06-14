# EduTrack — Assessment Service

Microservicio de **evaluaciones, notas y promedios** del libro de clases digital del
Colegio Bernardo O'Higgins. Forma parte del monorepo EduTrack (ver `../CLAUDE.md`).

- **Path / discovery:** `/assessment` (= app `edutrack-assessment` en Fly.io).
- **Schema BD:** `assessment` (PostgreSQL compartido, credenciales exclusivas).
- **Requisitos:** `BE-ASS-001..004`.

## Que hace

| Capacidad | Detalle |
|---|---|
| Evaluaciones | CRUD de evaluaciones (nombre, fecha, ponderacion) por asignatura y periodo. |
| Notas | Registro/correccion de notas en escala chilena **1.0–7.0** (fuera de rango => `422`). |
| Promedio ponderado | `GET /assessment/averages?studentId&subjectId&period` (`BE-ASS-002`). |
| Auditoria inmutable | Log de cambios de nota (usuario, timestamp, valor anterior/nuevo); lectura ADMIN/SUPERUSER (`BE-ASS-003`). |
| Integridad referencial | Nota para alumno inexistente en Student => `404` (`BE-ASS-004`). |

## Endpoints

```
GET    /assessment/evaluations[?subjectId&period]
POST   /assessment/evaluations
GET    /assessment/evaluations/{id}
PUT    /assessment/evaluations/{id}
DELETE /assessment/evaluations/{id}                 # 409 si tiene notas
POST   /assessment/evaluations/{id}/grades          # registrar nota
GET    /assessment/evaluations/{id}/grades

GET    /assessment/grades[?studentId&subjectId&period]
GET    /assessment/grades/{id}
PUT    /assessment/grades/{id}                       # corregir nota (audita el cambio)
GET    /assessment/grades/{id}/history               # log inmutable (assessment.audit)

GET    /assessment/averages?studentId&subjectId&period
```

OpenAPI/Swagger: `/assessment/q/openapi`, `/assessment/q/swagger-ui`.

## Comandos

```bash
./mvnw quarkus:dev          # dev mode (requiere DB)
./mvnw test                 # tests unitarios (sin Quarkus ni DB)
./mvnw clean package        # build
```

Ver `CLAUDE.md` para la estructura de paquetes y las decisiones de diseno.
