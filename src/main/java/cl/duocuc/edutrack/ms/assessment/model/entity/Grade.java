package cl.duocuc.edutrack.ms.assessment.model.entity;

import cl.duocuc.edutrack.ms.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Nota de un alumno en una evaluacion. La escala es chilena (1.0–7.0); la validez
 * del rango es invariante de dominio ({@link Score}, {@code 422} via servicio, no
 * Bean Validation). Un alumno tiene a lo sumo una nota por evaluacion (unicidad
 * {@code (evaluation_id, student_id)}).
 *
 * <p>Entidad mutable: la nota puede corregirse, y cada cambio queda en el log
 * inmutable {@link GradeAuditLog} ({@code BE-ASS-003}). Hereda auditoria completa
 * de {@link AuditableEntity}.</p>
 *
 * <p>La relacion con {@link Evaluation} es intra-schema, por lo que se modela como
 * asociacion JPA real ({@code @ManyToOne} LAZY). En cambio {@link #studentId} es
 * un id <b>opaco</b> de otro microservicio (Student): no hay FK; su existencia se
 * verifica via llamada inter-servicio ({@code BE-ASS-004}).</p>
 */
@Entity
@Table(name = "grades", schema = "assessment",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_grade_eval_student",
                columnNames = {"evaluation_id", "student_id"}))
public class Grade extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_id", nullable = false)
    public Evaluation evaluation;

    /** Alumno (id opaco, propiedad del Student Service; sin FK). */
    @Column(name = "student_id", columnDefinition = "uuid", nullable = false)
    public UUID studentId;

    @Column(nullable = false, precision = 2, scale = 1)
    public BigDecimal score;
}
