package cl.duocuc.edutrack.ms.assessment.model.dto;

import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import cl.duocuc.edutrack.ms.infrastructure.validation.Validations;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request unico del recurso evaluacion. La granularidad por endpoint se modela con
 * {@code @JsonView} (que campos viajan) y validation groups (que campos son
 * obligatorios):
 *
 * <ul>
 *   <li>{@code Create} ({@code POST}): todos los campos obligatorios.</li>
 *   <li>{@code Update} ({@code PUT}): los mismos campos, todos opcionales
 *       (actualizacion parcial; lo no provisto no cambia).</li>
 * </ul>
 *
 * <p>Las restricciones de <b>formato</b> ({@code @Size}, {@code @DecimalMin/Max})
 * van en el grupo {@code Default} (siempre, null-safe); las de <b>presencia</b>
 * ({@code @NotBlank}/{@code @NotNull}) en el grupo {@code OnCreate}.</p>
 */
@Schema(description = "Datos de alta/actualizacion de una evaluacion.")
public record EvaluationRequest(

        @JsonView({Views.Create.class, Views.Update.class})
        @Schema(description = "Nombre de la evaluacion", examples = "Prueba Unidad 1")
        @Size(max = 150)
        @NotBlank(groups = Validations.OnCreate.class) String name,

        @JsonView({Views.Create.class, Views.Update.class})
        @Schema(description = "Fecha de la evaluacion (ISO-8601)", examples = "2026-04-10")
        @NotNull(groups = Validations.OnCreate.class) LocalDate evaluationDate,

        @JsonView({Views.Create.class, Views.Update.class})
        @Schema(description = "Ponderacion como porcentaje (0, 100]", examples = "30.00")
        @DecimalMin(value = "0.00", inclusive = false)
        @DecimalMax(value = "100.00")
        @NotNull(groups = Validations.OnCreate.class) BigDecimal weight,

        @JsonView({Views.Create.class, Views.Update.class})
        @Schema(description = "Asignatura a la que pertenece la evaluacion")
        @NotNull(groups = Validations.OnCreate.class) UUID subjectId,

        @JsonView({Views.Create.class, Views.Update.class})
        @Schema(description = "Periodo academico", examples = "2026-1")
        @Size(max = 20)
        @NotBlank(groups = Validations.OnCreate.class) String period
) {}
