package cl.duocuc.edutrack.ms.assessment.model.entity;

import cl.duocuc.edutrack.ms.infrastructure.persistence.CreatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Registro <b>inmutable</b> de un cambio de nota ({@code BE-ASS-003}): quien,
 * cuando, valor anterior y valor nuevo. Es append-only por diseno —
 * extiende {@link CreatableEntity} (solo {@code id}, {@code created_at},
 * {@code creator_user}, todos {@code updatable = false}): nunca se actualiza ni
 * se expone endpoint para borrarlo, de modo que el historial no puede alterarse.
 *
 * <p>Mapeo a los datos que exige la spec:</p>
 * <ul>
 *   <li><b>usuario</b> ⇒ {@code creatorUser} (lo rellena {@code AuditContext}
 *       desde la identidad del request).</li>
 *   <li><b>timestamp</b> ⇒ {@code createdAt}.</li>
 *   <li><b>valor anterior</b> ⇒ {@link #oldValue} ({@code null} en el alta de la
 *       nota: no existia valor previo).</li>
 *   <li><b>valor nuevo</b> ⇒ {@link #newValue}.</li>
 * </ul>
 *
 * <p>Los ids de la nota, el alumno y la evaluacion se denormalizan aqui para que
 * el log sea legible y consultable por si mismo aunque la nota original se elimine
 * (no hay FK hacia {@code grades}): el historial sobrevive a la nota.</p>
 */
@Entity
@Table(name = "grade_audit_log", schema = "assessment")
public class GradeAuditLog extends CreatableEntity {

    @Column(name = "grade_id", columnDefinition = "uuid", nullable = false, updatable = false)
    public UUID gradeId;

    @Column(name = "student_id", columnDefinition = "uuid", nullable = false, updatable = false)
    public UUID studentId;

    @Column(name = "evaluation_id", columnDefinition = "uuid", nullable = false, updatable = false)
    public UUID evaluationId;

    /** Nota anterior; {@code null} cuando el evento es el alta de la nota. */
    @Column(name = "old_value", precision = 2, scale = 1, updatable = false)
    public BigDecimal oldValue;

    @Column(name = "new_value", precision = 2, scale = 1, nullable = false, updatable = false)
    public BigDecimal newValue;
}
