package cl.duocuc.edutrack.ms.assessment.model.dto;

import cl.duocuc.edutrack.ms.assessment.model.entity.Grade;
import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response unico del recurso nota. {@code evaluationId} se aplana desde la
 * asociacion {@code @ManyToOne} para no exponer la entidad evaluacion completa.
 * Los timestamps de auditoria solo viajan en vistas {@code Detailed}/{@code Admin}.
 */
@Schema(description = "Representacion de una nota.")
public record GradeResponse(
        @Schema(description = "UUID de la nota") UUID id,
        @Schema(description = "Evaluacion a la que pertenece") UUID evaluationId,
        @Schema(description = "Alumno") UUID studentId,
        @Schema(description = "Nota (1.0–7.0)") BigDecimal score,

        @JsonView({Views.Detailed.class, Views.Admin.class}) Instant createdAt,
        @JsonView({Views.Detailed.class, Views.Admin.class}) Instant updatedAt
) {

    /** Factory canonico: la instanciacion del DTO vive aqui, no en los call sites. */
    public static GradeResponse fromEntity(Grade g) {
        return new GradeResponse(
                g.id, g.evaluation.id, g.studentId, g.score,
                g.createdAt, g.updatedAt);
    }
}
