package cl.duocuc.edutrack.ms.assessment.model.entity;

import cl.duocuc.edutrack.ms.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evaluacion (prueba/trabajo) de una asignatura en un periodo: nombre, fecha y
 * <b>ponderacion</b>. Las notas de los alumnos ({@link Grade}) cuelgan de una
 * evaluacion; el promedio ponderado por asignatura y periodo se calcula cruzando
 * cada nota con la ponderacion de su evaluacion ({@code BE-ASS-002}).
 *
 * <p>Entidad mutable: hereda auditoria completa de {@link AuditableEntity}
 * ({@code id} UUID, {@code createdAt}/{@code creatorUser},
 * {@code updatedAt}/{@code updaterUser}). Campos {@code public} por convencion
 * Panache active record.</p>
 */
@Entity
@Table(name = "evaluations", schema = "assessment")
public class Evaluation extends AuditableEntity {

    @Column(nullable = false, length = 150)
    public String name;

    /** Fecha de la evaluacion (puede ser futura: evaluaciones planificadas). */
    @Column(name = "evaluation_date", nullable = false)
    public LocalDate evaluationDate;

    /** Ponderacion de la evaluacion como porcentaje ({@code (0, 100]}). */
    @Column(nullable = false, precision = 5, scale = 2)
    public BigDecimal weight;

    /** Asignatura (id opaco, propiedad del Course Service). */
    @Column(name = "subject_id", columnDefinition = "uuid", nullable = false)
    public UUID subjectId;

    /** Periodo academico al que pertenece (p. ej. {@code "2026-1"}). */
    @Column(nullable = false, length = 20)
    public String period;
}
