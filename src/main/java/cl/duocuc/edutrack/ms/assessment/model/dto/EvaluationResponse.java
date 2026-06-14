package cl.duocuc.edutrack.ms.assessment.model.dto;

import cl.duocuc.edutrack.ms.assessment.model.entity.Evaluation;
import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response unico del recurso evaluacion. Los timestamps de auditoria solo viajan
 * en vistas {@code Detailed}/{@code Admin}; los campos base estan en todas las
 * vistas (default {@code Views.Base}).
 */
@Schema(description = "Representacion de una evaluacion.")
public record EvaluationResponse(
        @Schema(description = "UUID de la evaluacion") UUID id,
        String name,
        LocalDate evaluationDate,
        @Schema(description = "Ponderacion (porcentaje)") BigDecimal weight,
        @Schema(description = "Asignatura") UUID subjectId,
        @Schema(description = "Periodo academico") String period,

        @JsonView({Views.Detailed.class, Views.Admin.class}) Instant createdAt,
        @JsonView({Views.Detailed.class, Views.Admin.class}) Instant updatedAt
) {

    /** Factory canonico: la instanciacion del DTO vive aqui, no en los call sites. */
    public static EvaluationResponse fromEntity(Evaluation e) {
        return new EvaluationResponse(
                e.id, e.name, e.evaluationDate, e.weight, e.subjectId, e.period,
                e.createdAt, e.updatedAt);
    }
}
