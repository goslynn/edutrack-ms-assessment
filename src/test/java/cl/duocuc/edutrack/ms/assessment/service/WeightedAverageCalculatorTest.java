package cl.duocuc.edutrack.ms.assessment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Casos de prueba <b>documentados</b> del calculo de promedio ponderado
 * ({@code BE-ASS-002}). El promedio es {@code sum(nota_i * ponderacion_i) /
 * sum(ponderacion_i)}, redondeado a 1 decimal con {@link java.math.RoundingMode#HALF_UP}
 * (convencion chilena de notas). El resultado es independiente de si las
 * ponderaciones suman 100: lo que importa es su proporcion relativa.
 */
class WeightedAverageCalculatorTest {

    private static WeightedScore ws(String score, String weight) {
        return new WeightedScore(new BigDecimal(score), new BigDecimal(weight));
    }

    @Test
    @DisplayName("Ponderaciones iguales => media aritmetica: (4+5+6)/3 = 5.0")
    void equalWeights_isArithmeticMean() {
        BigDecimal avg = WeightedAverageCalculator.compute(List.of(
                ws("4.0", "1"), ws("5.0", "1"), ws("6.0", "1")));
        assertEquals(new BigDecimal("5.0"), avg);
    }

    @Test
    @DisplayName("Ponderaciones porcentuales: 7.0@70% + 1.0@30% = (490+30)/100 = 5.2")
    void percentageWeights() {
        BigDecimal avg = WeightedAverageCalculator.compute(List.of(
                ws("7.0", "70"), ws("1.0", "30")));
        assertEquals(new BigDecimal("5.2"), avg);
    }

    @Test
    @DisplayName("Una sola nota: el promedio es esa nota")
    void singleGrade() {
        BigDecimal avg = WeightedAverageCalculator.compute(List.of(ws("6.4", "100")));
        assertEquals(new BigDecimal("6.4"), avg);
    }

    @Test
    @DisplayName("Redondeo HALF_UP hacia arriba: 17/3 = 5.666.. => 5.7")
    void roundsHalfUp() {
        BigDecimal avg = WeightedAverageCalculator.compute(List.of(
                ws("5.0", "1"), ws("6.0", "1"), ws("6.0", "1")));
        assertEquals(new BigDecimal("5.7"), avg);
    }

    @Test
    @DisplayName("Redondeo hacia abajo: 13/3 = 4.333.. => 4.3")
    void roundsDown() {
        BigDecimal avg = WeightedAverageCalculator.compute(List.of(
                ws("4.0", "1"), ws("4.0", "1"), ws("5.0", "1")));
        assertEquals(new BigDecimal("4.3"), avg);
    }

    @Test
    @DisplayName("Ponderaciones que no suman 100: 7.0@20 + 2.0@30 = 200/50 = 4.0")
    void weightsNotSummingHundred() {
        BigDecimal avg = WeightedAverageCalculator.compute(List.of(
                ws("7.0", "20"), ws("2.0", "30")));
        assertEquals(new BigDecimal("4.0"), avg);
    }

    @Test
    @DisplayName("Conjunto vacio => IllegalArgumentException (el servicio responde 404 antes)")
    void emptySet_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> WeightedAverageCalculator.compute(List.of()));
    }
}
