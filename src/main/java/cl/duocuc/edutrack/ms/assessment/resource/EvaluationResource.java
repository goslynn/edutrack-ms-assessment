package cl.duocuc.edutrack.ms.assessment.resource;

import cl.duocuc.edutrack.ms.assessment.model.dto.EvaluationRequest;
import cl.duocuc.edutrack.ms.assessment.model.dto.EvaluationResponse;
import cl.duocuc.edutrack.ms.assessment.model.dto.GradeRequest;
import cl.duocuc.edutrack.ms.assessment.model.dto.GradeResponse;
import cl.duocuc.edutrack.ms.assessment.security.AssessmentResourceId;
import cl.duocuc.edutrack.ms.assessment.service.EvaluationService;
import cl.duocuc.edutrack.ms.assessment.service.GradeService;
import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import cl.duocuc.edutrack.ms.infrastructure.security.Permission;
import cl.duocuc.edutrack.ms.infrastructure.security.RequirePermission;
import cl.duocuc.edutrack.ms.infrastructure.validation.Validations;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
 * CRUD de evaluaciones y registro de notas anidado bajo una evaluacion. Protegido
 * con permisos Unix-style: {@link AssessmentResourceId#EVALUATIONS} para las
 * evaluaciones, {@link AssessmentResourceId#GRADES} para las notas.
 */
@Path("/evaluations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Evaluations", description = "Gestion de evaluaciones y registro de notas")
public class EvaluationResource {

    @Inject
    EvaluationService evaluationService;

    @Inject
    GradeService gradeService;

    @GET
    @JsonView(Views.List.class)
    @RequirePermission(resource = AssessmentResourceId.EVALUATIONS, value = Permission.READ)
    @Operation(summary = "Listar evaluaciones", description = "Filtrable por asignatura y/o periodo. Requiere READ sobre assessment.evaluations.")
    @APIResponse(responseCode = "200", description = "Listado de evaluaciones",
            content = @Content(schema = @Schema(implementation = EvaluationResponse.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    public List<EvaluationResponse> list(
            @Parameter(description = "Filtrar por asignatura") @QueryParam("subjectId") UUID subjectId,
            @Parameter(description = "Filtrar por periodo") @QueryParam("period") String period) {
        return evaluationService.list(subjectId, period).stream()
                .map(EvaluationResponse::fromEntity).toList();
    }

    @POST
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = AssessmentResourceId.EVALUATIONS, value = Permission.WRITE)
    @Operation(summary = "Crear evaluacion", description = "Requiere WRITE sobre assessment.evaluations.")
    @APIResponse(responseCode = "201", description = "Evaluacion creada",
            content = @Content(schema = @Schema(implementation = EvaluationResponse.class)))
    @APIResponse(responseCode = "400", description = "Body invalido")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    public Response create(
            @Valid @ConvertGroup(to = Validations.Create.class)
            @JsonView(Views.Create.class) EvaluationRequest req) {
        var evaluation = evaluationService.create(
                req.name(), req.evaluationDate(), req.weight(), req.subjectId(), req.period());
        return Response.status(Response.Status.CREATED)
                .entity(EvaluationResponse.fromEntity(evaluation))
                .build();
    }

    @GET
    @Path("/{id}")
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = AssessmentResourceId.EVALUATIONS, value = Permission.READ)
    @Operation(summary = "Obtener evaluacion", description = "Requiere READ sobre assessment.evaluations.")
    @APIResponse(responseCode = "200", description = "Evaluacion encontrada",
            content = @Content(schema = @Schema(implementation = EvaluationResponse.class)))
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Evaluacion no encontrada")
    public EvaluationResponse get(@Parameter(description = "UUID de la evaluacion") @PathParam("id") UUID id) {
        return EvaluationResponse.fromEntity(evaluationService.findById(id));
    }

    @PUT
    @Path("/{id}")
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = AssessmentResourceId.EVALUATIONS, value = Permission.WRITE)
    @Operation(summary = "Actualizar evaluacion", description = "Actualizacion parcial. Requiere WRITE sobre assessment.evaluations.")
    @APIResponse(responseCode = "200", description = "Evaluacion actualizada",
            content = @Content(schema = @Schema(implementation = EvaluationResponse.class)))
    @APIResponse(responseCode = "400", description = "Body invalido")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Evaluacion no encontrada")
    public EvaluationResponse update(
            @Parameter(description = "UUID de la evaluacion") @PathParam("id") UUID id,
            @Valid @JsonView(Views.Update.class) EvaluationRequest req) {
        var evaluation = evaluationService.update(
                id, req.name(), req.evaluationDate(), req.weight(), req.subjectId(), req.period());
        return EvaluationResponse.fromEntity(evaluation);
    }

    @DELETE
    @Path("/{id}")
    @RequirePermission(resource = AssessmentResourceId.EVALUATIONS, value = Permission.WRITE)
    @Operation(summary = "Eliminar evaluacion",
            description = "Falla con 409 si tiene notas. Requiere WRITE sobre assessment.evaluations.")
    @APIResponse(responseCode = "204", description = "Evaluacion eliminada")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Evaluacion no encontrada")
    @APIResponse(responseCode = "409", description = "La evaluacion tiene notas registradas")
    public Response delete(@Parameter(description = "UUID de la evaluacion") @PathParam("id") UUID id) {
        evaluationService.delete(id);
        return Response.noContent().build();
    }

    // ── Notas anidadas bajo la evaluacion ────────────────────────────────────

    @POST
    @Path("/{evaluationId}/grades")
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = AssessmentResourceId.GRADES, value = Permission.WRITE)
    @Operation(summary = "Registrar nota",
            description = "Registra la nota de un alumno en la evaluacion. Valida rango 1.0-7.0 (422), "
                    + "existencia del alumno en Student (404) y unicidad (409). Requiere WRITE sobre assessment.grades.")
    @APIResponse(responseCode = "201", description = "Nota registrada",
            content = @Content(schema = @Schema(implementation = GradeResponse.class)))
    @APIResponse(responseCode = "400", description = "Body invalido")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Evaluacion o alumno inexistente")
    @APIResponse(responseCode = "409", description = "El alumno ya tiene nota en la evaluacion")
    @APIResponse(responseCode = "422", description = "Nota fuera del rango 1.0-7.0")
    public Response registerGrade(
            @Parameter(description = "UUID de la evaluacion") @PathParam("evaluationId") UUID evaluationId,
            @Valid @ConvertGroup(to = Validations.Create.class)
            @JsonView(Views.Create.class) GradeRequest req) {
        var grade = gradeService.register(evaluationId, req.studentId(), req.score());
        return Response.status(Response.Status.CREATED)
                .entity(GradeResponse.fromEntity(grade))
                .build();
    }

    @GET
    @Path("/{evaluationId}/grades")
    @JsonView(Views.List.class)
    @RequirePermission(resource = AssessmentResourceId.GRADES, value = Permission.READ)
    @Operation(summary = "Listar notas de una evaluacion", description = "Requiere READ sobre assessment.grades.")
    @APIResponse(responseCode = "200", description = "Notas de la evaluacion",
            content = @Content(schema = @Schema(implementation = GradeResponse.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Evaluacion no encontrada")
    public List<GradeResponse> listGrades(
            @Parameter(description = "UUID de la evaluacion") @PathParam("evaluationId") UUID evaluationId) {
        evaluationService.findById(evaluationId);
        return gradeService.listByEvaluation(evaluationId).stream()
                .map(GradeResponse::fromEntity).toList();
    }
}
