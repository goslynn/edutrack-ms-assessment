package cl.duocuc.edutrack.ms.assessment.service;

import cl.duocuc.edutrack.ms.assessment.model.entity.Evaluation;
import cl.duocuc.edutrack.ms.assessment.model.entity.Grade;
import cl.duocuc.edutrack.ms.assessment.repository.EvaluationRepository;
import cl.duocuc.edutrack.ms.assessment.repository.GradeRepository;
import cl.duocuc.edutrack.ms.infrastructure.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests del {@link GradeService}, donde viven las reglas de la spec:
 * {@code BE-ASS-001} (nota fuera de rango => {@code 422}), {@code BE-ASS-004}
 * (alumno inexistente => {@code 404}) y {@code BE-ASS-003} (cada cambio de nota
 * genera una entrada de auditoria). Atomicos: repos, {@link StudentGateway} y
 * {@link GradeAuditService} mockeados.
 */
class GradeServiceTest {

    private GradeRepository gradeRepository;
    private EvaluationRepository evaluationRepository;
    private StudentGateway studentGateway;
    private GradeAuditService auditService;
    private GradeService service;

    private final UUID studentId = UUID.randomUUID();
    private Evaluation evaluation;

    @BeforeEach
    void setUp() {
        gradeRepository = mock(GradeRepository.class);
        evaluationRepository = mock(EvaluationRepository.class);
        studentGateway = mock(StudentGateway.class);
        auditService = mock(GradeAuditService.class);
        service = new GradeService();
        service.gradeRepository = gradeRepository;
        service.evaluationRepository = evaluationRepository;
        service.studentGateway = studentGateway;
        service.auditService = auditService;

        evaluation = new Evaluation();
        evaluation.id = UUID.randomUUID();
        evaluation.subjectId = UUID.randomUUID();
        evaluation.period = "2026-1";
        evaluation.weight = new BigDecimal("30.00");
        evaluation.evaluationDate = LocalDate.of(2026, 4, 10);
    }

    private void evaluationExists() {
        when(evaluationRepository.findByIdOptional(evaluation.id)).thenReturn(Optional.of(evaluation));
    }

    /** Hace que persist() asigne un id a la nota (como haria Hibernate). */
    private void persistAssignsId() {
        doAnswer(inv -> {
            Grade g = inv.getArgument(0);
            g.id = UUID.randomUUID();
            return null;
        }).when(gradeRepository).persist(any(Grade.class));
    }

    private Grade grade(BigDecimal score) {
        Grade g = new Grade();
        g.id = UUID.randomUUID();
        g.evaluation = evaluation;
        g.studentId = studentId;
        g.score = score;
        return g;
    }

    // ── Registro de nota ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Registro valido: persiste la nota y registra auditoria (old=null, new=nota)")
    void register_valid_persistsAndAudits() {
        evaluationExists();
        when(studentGateway.exists(studentId)).thenReturn(true);
        when(gradeRepository.existsForStudent(evaluation.id, studentId)).thenReturn(false);
        persistAssignsId();
        BigDecimal score = new BigDecimal("5.5");

        Grade result = service.register(evaluation.id, studentId, score);

        ArgumentCaptor<Grade> captor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository).persist(captor.capture());
        Grade persisted = captor.getValue();
        assertSame(result, persisted);
        assertEquals(studentId, persisted.studentId);
        assertEquals(score, persisted.score);
        assertSame(evaluation, persisted.evaluation);
        verify(auditService).record(eq(persisted), isNull(), eq(score));
    }

    @Test
    @DisplayName("Nota fuera de rango => 422; no consulta a Student ni persiste ni audita (BE-ASS-001)")
    void register_outOfRange_unprocessable() {
        evaluationExists();

        DomainException ex = assertThrows(DomainException.class,
                () -> service.register(evaluation.id, studentId, new BigDecimal("7.5")));

        assertEquals(422, ex.status());
        verifyNoInteractions(studentGateway);
        verify(gradeRepository, never()).persist(any(Grade.class));
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Alumno inexistente en Student => 404; no persiste ni audita (BE-ASS-004)")
    void register_studentNotFound_notFound() {
        evaluationExists();
        when(studentGateway.exists(studentId)).thenReturn(false);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.register(evaluation.id, studentId, new BigDecimal("5.0")));

        assertEquals(404, ex.status());
        verify(gradeRepository, never()).persist(any(Grade.class));
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Evaluacion inexistente => 404")
    void register_evaluationNotFound_notFound() {
        when(evaluationRepository.findByIdOptional(evaluation.id)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> service.register(evaluation.id, studentId, new BigDecimal("5.0")));

        assertEquals(404, ex.status());
        verifyNoInteractions(studentGateway);
        verify(gradeRepository, never()).persist(any(Grade.class));
    }

    @Test
    @DisplayName("Nota duplicada (alumno ya tiene nota en la evaluacion) => 409")
    void register_duplicate_conflict() {
        evaluationExists();
        when(studentGateway.exists(studentId)).thenReturn(true);
        when(gradeRepository.existsForStudent(evaluation.id, studentId)).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.register(evaluation.id, studentId, new BigDecimal("5.0")));

        assertEquals(409, ex.status());
        verify(gradeRepository, never()).persist(any(Grade.class));
        verifyNoInteractions(auditService);
    }

    // ── Modificacion de nota ─────────────────────────────────────────────────

    @Test
    @DisplayName("Modificacion: cambia la nota y registra auditoria con valor anterior y nuevo (BE-ASS-003)")
    void updateScore_changed_auditsOldAndNew() {
        Grade g = grade(new BigDecimal("5.0"));
        when(gradeRepository.findByIdOptional(g.id)).thenReturn(Optional.of(g));
        BigDecimal newScore = new BigDecimal("6.0");

        Grade result = service.updateScore(g.id, newScore);

        assertEquals(newScore, result.score);
        verify(auditService).record(eq(g), eq(new BigDecimal("5.0")), eq(newScore));
    }

    @Test
    @DisplayName("Modificacion al mismo valor: no genera entrada de auditoria")
    void updateScore_sameValue_noAudit() {
        Grade g = grade(new BigDecimal("5.0"));
        when(gradeRepository.findByIdOptional(g.id)).thenReturn(Optional.of(g));

        service.updateScore(g.id, new BigDecimal("5.0"));

        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Modificacion fuera de rango => 422 y no audita (BE-ASS-001)")
    void updateScore_outOfRange_unprocessable() {
        Grade g = grade(new BigDecimal("5.0"));
        when(gradeRepository.findByIdOptional(g.id)).thenReturn(Optional.of(g));

        DomainException ex = assertThrows(DomainException.class,
                () -> service.updateScore(g.id, new BigDecimal("0.5")));

        assertEquals(422, ex.status());
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Modificacion de nota inexistente => 404")
    void updateScore_missing_notFound() {
        UUID id = UUID.randomUUID();
        when(gradeRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> service.updateScore(id, new BigDecimal("5.0")));
        assertEquals(404, ex.status());
    }

    // ── Historial ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("history de una nota inexistente => 404 (no consulta el log)")
    void history_missingGrade_notFound() {
        UUID id = UUID.randomUUID();
        when(gradeRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class, () -> service.history(id));
        assertEquals(404, ex.status());
        verifyNoInteractions(auditService);
    }
}
