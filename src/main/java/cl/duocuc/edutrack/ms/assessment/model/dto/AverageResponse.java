package cl.duocuc.edutrack.ms.assessment.model.dto;

import cl.duocuc.edutrack.ms.assessment.service.AverageResult;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response del promedio ponderado de un alumno en una asignatura y periodo
 * ({@code BE-ASS-002}). Es un resultado computado (no respalda una entidad), por lo
 * que el factory es {@code of(...)} y combina la clave de consulta
 * (alumno/asignatura/periodo) con el {@link AverageResult} calculado.
 */
@Schema(description = "Promedio ponderado por alumno, asignatura y periodo.")
public record AverageResponse(
        @Schema(description = "Alumno") UUID studentId,
        @Schema(description = "Asignatura") UUID subjectId,
        @Schema(description = "Periodo academico") String period,
        @Schema(description = "Promedio ponderado (escala chilena, 1 decimal)") BigDecimal average,
        @Schema(description = "Cantidad de notas consideradas") int gradeCount
) {

    /** Factory canonico: ensambla la clave de consulta con el resultado calculado. */
    public static AverageResponse of(UUID studentId, UUID subjectId, String period, AverageResult result) {
        return new AverageResponse(studentId, subjectId, period, result.average(), result.gradeCount());
    }
}
