package cl.duocuc.edutrack.ms.assessment.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests del value object {@link Score}: la escala chilena valida es {@code [1.0, 7.0]}
 * inclusive. Una nota fuera de ese rango es invariante de dominio (la traduce el
 * servicio a {@code 422}, no Bean Validation) — ver {@code BE-ASS-001}.
 */
class ScoreTest {

    @Test
    @DisplayName("Los limites 1.0 y 7.0 son validos (inclusive)")
    void boundaries_areValid() {
        assertTrue(Score.isValid(new BigDecimal("1.0")));
        assertTrue(Score.isValid(new BigDecimal("7.0")));
    }

    @Test
    @DisplayName("Un valor intermedio es valido")
    void midRange_isValid() {
        assertTrue(Score.isValid(new BigDecimal("4.5")));
    }

    @Test
    @DisplayName("Bajo 1.0 es invalido")
    void belowMin_isInvalid() {
        assertFalse(Score.isValid(new BigDecimal("0.9")));
        assertFalse(Score.isValid(new BigDecimal("0.0")));
    }

    @Test
    @DisplayName("Sobre 7.0 es invalido")
    void aboveMax_isInvalid() {
        assertFalse(Score.isValid(new BigDecimal("7.1")));
        assertFalse(Score.isValid(new BigDecimal("10.0")));
    }

    @Test
    @DisplayName("null es invalido (la presencia la cubre Bean Validation aparte)")
    void nullValue_isInvalid() {
        assertFalse(Score.isValid(null));
    }
}
