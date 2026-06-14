package cl.duocuc.edutrack.ms.assessment.resource;

import cl.duocuc.edutrack.ms.assessment.model.dto.AverageResponse;
import cl.duocuc.edutrack.ms.assessment.security.AssessmentResourceId;
import cl.duocuc.edutrack.ms.assessment.service.AverageService;
import cl.duocuc.edutrack.ms.infrastructure.security.Permission;
import cl.duocuc.edutrack.ms.infrastructure.security.RequirePermission;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

/**
 * Promedio ponderado por alumno, asignatura y periodo ({@code BE-ASS-002}). Es una
 * lectura derivada de las notas, por lo que se protege con
 * {@link AssessmentResourceId#GRADES} (READ).
 */
@Path("/averages")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Averages", description = "Promedios ponderados")
public class AverageResource {

    @Inject
    AverageService averageService;

    @GET
    @RequirePermission(resource = AssessmentResourceId.GRADES, value = Permission.READ)
    @Operation(summary = "Promedio ponderado",
            description = "Calcula el promedio ponderado del alumno en la asignatura y periodo. "
                    + "Requiere READ sobre assessment.grades.")
    @APIResponse(responseCode = "200", description = "Promedio ponderado",
            content = @Content(schema = @Schema(implementation = AverageResponse.class)))
    @APIResponse(responseCode = "400", description = "Faltan parametros de consulta")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "No hay notas para la combinacion indicada")
    public AverageResponse average(
            @Parameter(description = "Alumno", required = true) @QueryParam("studentId") @NotNull UUID studentId,
            @Parameter(description = "Asignatura", required = true) @QueryParam("subjectId") @NotNull UUID subjectId,
            @Parameter(description = "Periodo academico", required = true) @QueryParam("period") @NotBlank String period) {
        var result = averageService.weightedAverage(studentId, subjectId, period);
        return AverageResponse.of(studentId, subjectId, period, result);
    }
}
