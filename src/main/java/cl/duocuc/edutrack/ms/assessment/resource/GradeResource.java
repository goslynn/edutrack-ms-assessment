package cl.duocuc.edutrack.ms.assessment.resource;

import cl.duocuc.edutrack.ms.assessment.model.dto.AssessmentValidations;
import cl.duocuc.edutrack.ms.assessment.model.dto.GradeAuditLogResponse;
import cl.duocuc.edutrack.ms.assessment.model.dto.GradeRequest;
import cl.duocuc.edutrack.ms.assessment.model.dto.GradeResponse;
import cl.duocuc.edutrack.ms.assessment.security.AssessmentResourceId;
import cl.duocuc.edutrack.ms.assessment.service.GradeService;
import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import cl.duocuc.edutrack.ms.infrastructure.security.Permission;
import cl.duocuc.edutrack.ms.infrastructure.security.RequirePermission;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * Lectura y correccion de notas, mas el historial inmutable de cambios. La
 * correccion de nota ({@code PUT}) usa {@link AssessmentResourceId#GRADES} (WRITE);
 * el historial usa {@link AssessmentResourceId#AUDIT} (READ) — un grant reservado a
 * ADMIN/SUPERUSER, no al docente ({@code BE-ASS-003}).
 */
@Path("/grades")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Grades", description = "Notas y su historial de cambios")
public class GradeResource {

    @Inject
    GradeService gradeService;

    @GET
    @JsonView(Views.List.class)
    @RequirePermission(resource = AssessmentResourceId.GRADES, value = Permission.READ)
    @Operation(summary = "Listar notas", description = "Filtrable por alumno, asignatura y/o periodo. Requiere READ sobre assessment.grades.")
    @APIResponse(responseCode = "200", description = "Listado de notas",
            content = @Content(schema = @Schema(implementation = GradeResponse.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    public List<GradeResponse> list(
            @Parameter(description = "Filtrar por alumno") @QueryParam("studentId") UUID studentId,
            @Parameter(description = "Filtrar por asignatura") @QueryParam("subjectId") UUID subjectId,
            @Parameter(description = "Filtrar por periodo") @QueryParam("period") String period) {
        return gradeService.list(studentId, subjectId, period).stream()
                .map(GradeResponse::fromEntity).toList();
    }

    @GET
    @Path("/{id}")
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = AssessmentResourceId.GRADES, value = Permission.READ)
    @Operation(summary = "Obtener nota", description = "Requiere READ sobre assessment.grades.")
    @APIResponse(responseCode = "200", description = "Nota encontrada",
            content = @Content(schema = @Schema(implementation = GradeResponse.class)))
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Nota no encontrada")
    public GradeResponse get(@Parameter(description = "UUID de la nota") @PathParam("id") UUID id) {
        return GradeResponse.fromEntity(gradeService.findById(id));
    }

    @PUT
    @Path("/{id}")
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = AssessmentResourceId.GRADES, value = Permission.WRITE)
    @Operation(summary = "Corregir nota",
            description = "Modifica el valor de la nota; registra el cambio en el log de auditoria. "
                    + "Valida rango 1.0-7.0 (422). Requiere WRITE sobre assessment.grades.")
    @APIResponse(responseCode = "200", description = "Nota corregida",
            content = @Content(schema = @Schema(implementation = GradeResponse.class)))
    @APIResponse(responseCode = "400", description = "Body invalido")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Nota no encontrada")
    @APIResponse(responseCode = "422", description = "Nota fuera del rango 1.0-7.0")
    public GradeResponse update(
            @Parameter(description = "UUID de la nota") @PathParam("id") UUID id,
            @Valid @ConvertGroup(to = AssessmentValidations.UpdateGrade.class)
            @JsonView(Views.Update.class) GradeRequest req) {
        return GradeResponse.fromEntity(gradeService.updateScore(id, req.score()));
    }

    @GET
    @Path("/{id}/history")
    @RequirePermission(resource = AssessmentResourceId.AUDIT, value = Permission.READ)
    @Operation(summary = "Historial de cambios de una nota",
            description = "Log inmutable (usuario, timestamp, valor anterior/nuevo). "
                    + "Reservado a ADMIN/SUPERUSER: requiere READ sobre assessment.audit.")
    @APIResponse(responseCode = "200", description = "Historial de cambios",
            content = @Content(schema = @Schema(implementation = GradeAuditLogResponse.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Nota no encontrada")
    public List<GradeAuditLogResponse> history(@Parameter(description = "UUID de la nota") @PathParam("id") UUID id) {
        return gradeService.history(id).stream()
                .map(GradeAuditLogResponse::fromEntity).toList();
    }
}
