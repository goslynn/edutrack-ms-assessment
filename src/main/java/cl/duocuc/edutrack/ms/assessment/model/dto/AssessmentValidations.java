package cl.duocuc.edutrack.ms.assessment.model.dto;

import cl.duocuc.edutrack.ms.infrastructure.validation.Validations;
import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

/**
 * Grupos de Bean Validation especificos del dominio Assessment. Conviven con la
 * interfaz transversal {@link Validations} (que aporta {@code OnCreate} /
 * {@code Create}); aqui solo lo propio del MS.
 */
public interface AssessmentValidations {

    /** Marcador de presencia obligatoria al modificar una nota (la nota nueva). */
    interface OnUpdateGrade {}

    /**
     * Secuencia del {@code PUT /assessment/grades/{id}}: primero {@link Default}
     * (formato) y, si pasa, {@link OnUpdateGrade} (presencia de la nota nueva).
     */
    @GroupSequence({ Default.class, OnUpdateGrade.class })
    interface UpdateGrade {}
}
