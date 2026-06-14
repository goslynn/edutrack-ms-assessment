package cl.duocuc.edutrack.ms.assessment.model.dto;

import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import cl.duocuc.edutrack.ms.infrastructure.validation.Validations;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request unico del recurso nota. La granularidad por endpoint se modela con
 * {@code @JsonView} + validation groups:
 *
 * <ul>
 *   <li>{@code Create} ({@code POST .../grades}): {@code studentId} + {@code score}.</li>
 *   <li>{@code Update} ({@code PUT /grades/{id}}): solo {@code score} (el alumno y la
 *       evaluacion no se reasignan: se corrige el valor).</li>
 * </ul>
 *
 * <p>Notese que <b>no</b> hay restriccion de rango ({@code @DecimalMin/Max}) sobre
 * {@code score}: la escala chilena 1.0–7.0 es invariante de dominio y la spec exige
 * {@code 422} (no {@code 400}), por lo que la valida el servicio via {@code Score}.
 * Aqui solo se exige <b>presencia</b> ({@code @NotNull}).</p>
 */
@Schema(description = "Datos de registro/correccion de una nota.")
public record GradeRequest(

        @JsonView(Views.Create.class)
        @Schema(description = "Alumno al que se registra la nota")
        @NotNull(groups = Validations.OnCreate.class) UUID studentId,

        @JsonView({Views.Create.class, Views.Update.class})
        @Schema(description = "Nota en escala chilena 1.0–7.0", examples = "5.5")
        @NotNull(groups = {Validations.OnCreate.class, AssessmentValidations.OnUpdateGrade.class})
        BigDecimal score
) {}
