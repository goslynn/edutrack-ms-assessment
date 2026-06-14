package cl.duocuc.edutrack.ms.assessment.service;

import cl.duocuc.edutrack.ms.assessment.model.entity.Evaluation;
import cl.duocuc.edutrack.ms.assessment.model.entity.Grade;
import cl.duocuc.edutrack.ms.assessment.model.entity.GradeAuditLog;
import cl.duocuc.edutrack.ms.assessment.model.entity.Score;
import cl.duocuc.edutrack.ms.assessment.repository.EvaluationRepository;
import cl.duocuc.edutrack.ms.assessment.repository.GradeRepository;
import cl.duocuc.edutrack.ms.infrastructure.exception.ConflictException;
import cl.duocuc.edutrack.ms.infrastructure.exception.DomainException;
import cl.duocuc.edutrack.ms.infrastructure.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Logica de dominio de notas. Concentra las reglas de la spec del Assessment
 * Service:
 *
 * <ul>
 *   <li><b>Rango de nota</b> ({@code BE-ASS-001}): la escala chilena es
 *       {@code [1.0, 7.0]}. El rango es invariante de dominio (no Bean Validation),
 *       por lo que una nota fuera de rango se traduce a {@code 422} via
 *       {@link DomainException} consultando {@link Score#isValid}.</li>
 *   <li><b>Integridad referencial con Student</b> ({@code BE-ASS-004}): no se
 *       registra nota para un alumno inexistente; la existencia la verifica
 *       {@link StudentGateway} ({@code 404} si no existe).</li>
 *   <li><b>Auditoria inmutable</b> ({@code BE-ASS-003}): cada alta o cambio de
 *       nota genera una entrada en {@link GradeAuditService} (valor anterior /
 *       nuevo, usuario y timestamp). En el alta el valor anterior es {@code null}.</li>
 * </ul>
 *
 * <p>El alta valida en orden creciente de costo: existencia de la evaluacion
 * (local) → rango de la nota (local) → existencia del alumno (remota) → unicidad
 * (local), de modo que un dato invalido corta antes de la llamada inter-servicio.</p>
 *
 * <p>Colaboradores inyectados por campo (visibilidad de paquete) para tests
 * unitarios planos con Mockito sin arrancar Quarkus.</p>
 */
@ApplicationScoped
public class GradeService {

    @Inject
    GradeRepository gradeRepository;

    @Inject
    EvaluationRepository evaluationRepository;

    @Inject
    StudentGateway studentGateway;

    @Inject
    GradeAuditService auditService;

    /**
     * Registra una nota de un alumno en una evaluacion.
     *
     * @throws NotFoundException ({@code 404}) si la evaluacion no existe, o si el
     *                           alumno no existe en Student ({@code BE-ASS-004}).
     * @throws DomainException   ({@code 422}) si la nota esta fuera de {@code [1.0,7.0]}
     *                           ({@code BE-ASS-001}).
     * @throws ConflictException ({@code 409}) si el alumno ya tiene nota en la evaluacion.
     */
    @Transactional
    public Grade register(UUID evaluationId, UUID studentId, BigDecimal score) {
        Evaluation evaluation = evaluationRepository.findByIdOptional(evaluationId)
                .orElseThrow(() -> new NotFoundException("ASSESSMENT.EVALUATION.NOT_FOUND",
                        "Evaluacion no encontrada").with("evaluationId", evaluationId));

        requireInRange(score);

        if (!studentGateway.exists(studentId)) {
            throw new NotFoundException("ASSESSMENT.STUDENT.NOT_FOUND",
                    "El alumno no existe en Student Service").with("studentId", studentId);
        }
        if (gradeRepository.existsForStudent(evaluationId, studentId)) {
            throw new ConflictException("ASSESSMENT.GRADE.ALREADY_EXISTS",
                    "El alumno ya tiene nota registrada en esta evaluacion")
                    .with("evaluationId", evaluationId).with("studentId", studentId);
        }

        Grade grade = new Grade();
        grade.evaluation = evaluation;
        grade.studentId = studentId;
        grade.score = score;
        gradeRepository.persist(grade);

        auditService.record(grade, null, score);
        return grade;
    }

    /** Busca por id o lanza {@code 404}. */
    public Grade findById(UUID id) {
        return gradeRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("ASSESSMENT.GRADE.NOT_FOUND",
                        "Nota no encontrada").with("id", id));
    }

    /** Listado de notas filtrable por alumno, asignatura y/o periodo (opcionales). */
    public List<Grade> list(UUID studentId, UUID subjectId, String period) {
        return gradeRepository.listFiltered(studentId, subjectId, period);
    }

    /**
     * Notas registradas en una evaluacion. La existencia de la evaluacion la
     * valida el recurso (via {@link EvaluationService#findById}) antes de listar.
     */
    public List<Grade> listByEvaluation(UUID evaluationId) {
        return gradeRepository.listByEvaluation(evaluationId);
    }

    /**
     * Modifica la nota. Si el valor no cambia, es un no-op sin entrada de
     * auditoria. Si cambia, actualiza la nota y registra el cambio con su valor
     * anterior y nuevo ({@code BE-ASS-003}).
     *
     * @throws NotFoundException ({@code 404}) si la nota no existe.
     * @throws DomainException   ({@code 422}) si la nueva nota esta fuera de rango.
     */
    @Transactional
    public Grade updateScore(UUID id, BigDecimal newScore) {
        Grade grade = findById(id);
        requireInRange(newScore);

        BigDecimal oldScore = grade.score;
        if (oldScore.compareTo(newScore) == 0) {
            return grade;
        }
        grade.score = newScore;
        auditService.record(grade, oldScore, newScore);
        return grade;
    }

    /**
     * Historial inmutable de cambios de una nota ({@code BE-ASS-003}). Verifica
     * primero que la nota exista ({@code 404}).
     */
    public List<GradeAuditLog> history(UUID gradeId) {
        findById(gradeId);
        return auditService.history(gradeId);
    }

    private void requireInRange(BigDecimal score) {
        if (!Score.isValid(score)) {
            throw new DomainException(422, "ASSESSMENT.GRADE.SCORE_OUT_OF_RANGE",
                    "La nota debe estar en el rango 1.0 a 7.0").with("score", score);
        }
    }
}
