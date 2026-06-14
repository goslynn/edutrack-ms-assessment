package cl.duocuc.edutrack.ms.assessment.service;

import cl.duocuc.edutrack.ms.assessment.repository.GradeRepository;
import cl.duocuc.edutrack.ms.infrastructure.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Calculo del <b>promedio ponderado</b> de un alumno en una asignatura y periodo
 * ({@code BE-ASS-002}). Obtiene los pares {@code (nota, ponderacion)} del
 * repositorio y delega la aritmetica en {@link WeightedAverageCalculator} (puro,
 * con casos de prueba documentados). Si no hay notas para la combinacion pedida
 * responde {@code 404} (no hay promedio que calcular).
 */
@ApplicationScoped
public class AverageService {

    @Inject
    GradeRepository gradeRepository;

    public AverageResult weightedAverage(UUID studentId, UUID subjectId, String period) {
        List<WeightedScore> scores = gradeRepository.weightedScores(studentId, subjectId, period);
        if (scores.isEmpty()) {
            throw new NotFoundException("ASSESSMENT.AVERAGE.NO_GRADES",
                    "No hay notas para el alumno en la asignatura y periodo indicados")
                    .with("studentId", studentId)
                    .with("subjectId", subjectId)
                    .with("period", period);
        }
        BigDecimal average = WeightedAverageCalculator.compute(scores);
        return new AverageResult(average, scores.size());
    }
}
