package cl.duocuc.edutrack.ms.assessment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculo puro del <b>promedio ponderado</b> de un conjunto de notas
 * ({@code BE-ASS-002}). Sin estado y sin dependencias: toda la aritmetica vive
 * aqui para poder validarla con casos documentados (ver
 * {@code WeightedAverageCalculatorTest}) y reusarla desde el servicio.
 *
 * <p>Formula: {@code sum(nota_i * ponderacion_i) / sum(ponderacion_i)}. El
 * resultado se redondea a <b>1 decimal</b> con {@link RoundingMode#HALF_UP}
 * (convencion chilena para la nota final). El metodo es agnostico a la unidad de
 * la ponderacion (porcentaje, fraccion o peso entero): solo cuenta la proporcion
 * relativa, por lo que no exige que las ponderaciones sumen 100.</p>
 */
public final class WeightedAverageCalculator {

    /** Decimales del promedio final (convencion chilena: 1 decimal). */
    private static final int SCALE = 1;

    private WeightedAverageCalculator() {}

    /**
     * @param entries pares {@code (nota, ponderacion)} a promediar; no vacio
     * @return promedio ponderado redondeado a 1 decimal (HALF_UP)
     * @throws IllegalArgumentException si {@code entries} es nulo/vacio o si la
     *         suma de ponderaciones es 0 (no se puede dividir). El servicio
     *         garantiza un conjunto no vacio antes de llamar (responde
     *         {@code 404} cuando no hay notas para la combinacion pedida).
     */
    public static BigDecimal compute(List<WeightedScore> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("No hay notas para promediar");
        }
        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (WeightedScore e : entries) {
            weightedSum = weightedSum.add(e.score().multiply(e.weight()));
            totalWeight = totalWeight.add(e.weight());
        }
        if (totalWeight.signum() == 0) {
            throw new IllegalArgumentException("La suma de ponderaciones debe ser > 0");
        }
        return weightedSum.divide(totalWeight, SCALE, RoundingMode.HALF_UP);
    }
}
