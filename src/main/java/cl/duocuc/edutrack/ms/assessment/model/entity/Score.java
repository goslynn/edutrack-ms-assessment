package cl.duocuc.edutrack.ms.assessment.model.entity;

import java.math.BigDecimal;

/**
 * Escala de notas chilena: una nota valida vive en el rango cerrado
 * {@code [1.0, 7.0]}. Es un value object sin estado (solo la regla de rango); el
 * almacenamiento de la nota es un {@link BigDecimal} en la entidad {@link Grade}.
 *
 * <p>La validez del rango es <b>invariante de dominio</b>, no una restriccion de
 * formato del request: por eso NO se modela con Bean Validation
 * ({@code @DecimalMin}/{@code @DecimalMax} produciria {@code 400}). La spec
 * exige {@code 422} para una nota fuera de rango ({@code BE-ASS-001}), status que
 * el servicio lanza como {@code DomainException(422, ...)} tras consultar
 * {@link #isValid(BigDecimal)} — mismo patron que la validez del RUT en Student.</p>
 */
public final class Score {

    /** Nota minima de la escala chilena. */
    public static final BigDecimal MIN = new BigDecimal("1.0");

    /** Nota maxima de la escala chilena. */
    public static final BigDecimal MAX = new BigDecimal("7.0");

    private Score() {}

    /**
     * @return {@code true} si {@code value} es no nulo y cae dentro del rango
     *         cerrado {@code [1.0, 7.0]}; {@code false} en caso contrario.
     */
    public static boolean isValid(BigDecimal value) {
        return value != null
                && value.compareTo(MIN) >= 0
                && value.compareTo(MAX) <= 0;
    }
}
