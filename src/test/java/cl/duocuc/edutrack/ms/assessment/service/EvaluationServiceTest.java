package cl.duocuc.edutrack.ms.assessment.service;

import cl.duocuc.edutrack.ms.assessment.model.entity.Evaluation;
import cl.duocuc.edutrack.ms.assessment.repository.EvaluationRepository;
import cl.duocuc.edutrack.ms.assessment.repository.GradeRepository;
import cl.duocuc.edutrack.ms.infrastructure.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests del {@link EvaluationService}. Atomicos: repositorios mockeados (sin
 * Quarkus, sin DB). Cubren alta, lectura ({@code 404}) y la regla de negocio de
 * borrado (no se puede eliminar una evaluacion con notas => {@code 409}).
 */
class EvaluationServiceTest {

    private EvaluationRepository evaluationRepository;
    private GradeRepository gradeRepository;
    private EvaluationService service;

    private final UUID subjectId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        evaluationRepository = mock(EvaluationRepository.class);
        gradeRepository = mock(GradeRepository.class);
        service = new EvaluationService();
        service.evaluationRepository = evaluationRepository;
        service.gradeRepository = gradeRepository;
    }

    private Evaluation evaluation() {
        Evaluation e = new Evaluation();
        e.id = UUID.randomUUID();
        e.name = "Prueba 1";
        e.evaluationDate = LocalDate.of(2026, 4, 10);
        e.weight = new BigDecimal("30.00");
        e.subjectId = subjectId;
        e.period = "2026-1";
        return e;
    }

    @Test
    @DisplayName("Alta: persiste la evaluacion con sus datos")
    void create_persists() {
        Evaluation e = service.create("Prueba 1", LocalDate.of(2026, 4, 10),
                new BigDecimal("30.00"), subjectId, "2026-1");

        assertEquals("Prueba 1", e.name);
        assertEquals(new BigDecimal("30.00"), e.weight);
        assertEquals(subjectId, e.subjectId);
        assertEquals("2026-1", e.period);
        verify(evaluationRepository).persist(e);
    }

    @Test
    @DisplayName("findById inexistente => 404")
    void findById_missing_notFound() {
        UUID id = UUID.randomUUID();
        when(evaluationRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class, () -> service.findById(id));
        assertEquals(404, ex.status());
    }

    @Test
    @DisplayName("update modifica los campos provistos")
    void update_modifiesFields() {
        Evaluation e = evaluation();
        when(evaluationRepository.findByIdOptional(e.id)).thenReturn(Optional.of(e));

        Evaluation result = service.update(e.id, "Prueba 1 (recuperativa)", null,
                new BigDecimal("40.00"), null, null);

        assertEquals("Prueba 1 (recuperativa)", result.name);
        assertEquals(new BigDecimal("40.00"), result.weight);
        assertEquals(LocalDate.of(2026, 4, 10), result.evaluationDate, "lo no provisto no cambia");
    }

    @Test
    @DisplayName("delete sin notas: elimina la evaluacion")
    void delete_noGrades_deletes() {
        Evaluation e = evaluation();
        when(evaluationRepository.findByIdOptional(e.id)).thenReturn(Optional.of(e));
        when(gradeRepository.countByEvaluation(e.id)).thenReturn(0L);

        service.delete(e.id);

        verify(evaluationRepository).delete(e);
    }

    @Test
    @DisplayName("delete con notas => 409 y no elimina")
    void delete_withGrades_conflict() {
        Evaluation e = evaluation();
        when(evaluationRepository.findByIdOptional(e.id)).thenReturn(Optional.of(e));
        when(gradeRepository.countByEvaluation(e.id)).thenReturn(3L);

        DomainException ex = assertThrows(DomainException.class, () -> service.delete(e.id));

        assertEquals(409, ex.status());
        verify(evaluationRepository, never()).delete(any(Evaluation.class));
    }
}
