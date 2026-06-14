package cl.duocuc.edutrack.ms.assessment.model.dto;

import cl.duocuc.edutrack.ms.assessment.model.entity.GradeAuditLog;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response de una entrada del log de auditoria de notas ({@code BE-ASS-003}).
 * Expone los cuatro datos que exige la spec — usuario, timestamp, valor anterior y
 * valor nuevo — mas los ids denormalizados de la nota/alumno/evaluacion. El
 * "usuario" y el "timestamp" se mapean desde {@code creatorUser}/{@code createdAt}
 * de la entidad inmutable.
 */
@Schema(description = "Entrada del historial de cambios de una nota (inmutable).")
public record GradeAuditLogResponse(
        @Schema(description = "UUID de la entrada de auditoria") UUID id,
        @Schema(description = "Nota afectada") UUID gradeId,
        @Schema(description = "Alumno") UUID studentId,
        @Schema(description = "Evaluacion") UUID evaluationId,
        @Schema(description = "Valor anterior (null en el alta)") BigDecimal oldValue,
        @Schema(description = "Valor nuevo") BigDecimal newValue,
        @Schema(description = "Usuario que realizo el cambio") UUID changedBy,
        @Schema(description = "Instante del cambio") Instant changedAt
) {

    /** Factory canonico: la instanciacion del DTO vive aqui, no en los call sites. */
    public static GradeAuditLogResponse fromEntity(GradeAuditLog log) {
        return new GradeAuditLogResponse(
                log.id, log.gradeId, log.studentId, log.evaluationId,
                log.oldValue, log.newValue, log.creatorUser, log.createdAt);
    }
}
