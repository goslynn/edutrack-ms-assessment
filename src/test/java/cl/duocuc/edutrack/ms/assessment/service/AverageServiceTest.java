package cl.duocuc.edutrack.ms.assessment.service;

import cl.duocuc.edutrack.ms.assessment.repository.GradeRepository;
import cl.duocuc.edutrack.ms.infrastructure.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests del {@link AverageService} ({@code BE-ASS-002}): delega el calculo en
 * {@link WeightedAverageCalculator} y traduce "sin notas" a {@code 404}. El detalle
 * aritmetico se cubre en {@link WeightedAverageCalculatorTest}.
 */
class AverageServiceTest {

    private GradeRepository gradeRepository;
    private AverageService service;

    private final UUID studentId = UUID.randomUUID();
    private final UUID subjectId = UUID.randomUUID();
    private final String period = "2026-1";

    @BeforeEach
    void setUp() {
        gradeRepository = mock(GradeRepository.class);
        service = new AverageService();
        service.gradeRepository = gradeRepository;
    }

    @Test
    @DisplayName("Sin notas para la combinacion alumno/asignatura/periodo => 404")
    void noGrades_notFound() {
        when(gradeRepository.weightedScores(studentId, subjectId, period)).thenReturn(List.of());

        DomainException ex = assertThrows(DomainException.class,
                () -> service.weightedAverage(studentId, subjectId, period));
        assertEquals(404, ex.status());
    }

    @Test
    @DisplayName("Con notas: devuelve el promedio ponderado y la cantidad de notas")
    void withGrades_returnsWeightedAverageAndCount() {
        when(gradeRepository.weightedScores(studentId, subjectId, period)).thenReturn(List.of(
                new WeightedScore(new BigDecimal("7.0"), new BigDecimal("70")),
                new WeightedScore(new BigDecimal("1.0"), new BigDecimal("30"))));

        AverageResult result = service.weightedAverage(studentId, subjectId, period);

        assertEquals(new BigDecimal("5.2"), result.average());
        assertEquals(2, result.gradeCount());
    }
}
