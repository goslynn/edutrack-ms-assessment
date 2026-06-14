package cl.duocuc.edutrack.ms.assessment.service;

import cl.duocuc.edutrack.ms.assessment.model.entity.Evaluation;
import cl.duocuc.edutrack.ms.assessment.repository.EvaluationRepository;
import cl.duocuc.edutrack.ms.assessment.repository.GradeRepository;
import cl.duocuc.edutrack.ms.infrastructure.exception.ConflictException;
import cl.duocuc.edutrack.ms.infrastructure.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Logica de dominio de evaluaciones: alta, listado filtrable, lectura,
 * actualizacion y borrado. El borrado esta protegido por una invariante: una
 * evaluacion con notas no puede eliminarse ({@code 409}) para no perder datos
 * academicos (las notas tienen su propio ciclo y su log de auditoria).
 *
 * <p>Colaboradores inyectados por campo (visibilidad de paquete) para permitir
 * tests unitarios planos con Mockito sin arrancar Quarkus.</p>
 */
@ApplicationScoped
public class EvaluationService {

    @Inject
    EvaluationRepository evaluationRepository;

    @Inject
    GradeRepository gradeRepository;

    @Transactional
    public Evaluation create(String name, LocalDate evaluationDate, BigDecimal weight,
                             UUID subjectId, String period) {
        Evaluation evaluation = new Evaluation();
        evaluation.name = name;
        evaluation.evaluationDate = evaluationDate;
        evaluation.weight = weight;
        evaluation.subjectId = subjectId;
        evaluation.period = period;
        evaluationRepository.persist(evaluation);
        return evaluation;
    }

    /** Listado filtrable por asignatura y/o periodo (ambos opcionales). */
    public List<Evaluation> list(UUID subjectId, String period) {
        return evaluationRepository.list(subjectId, period);
    }

    /** Busca por id o lanza {@code 404}. */
    public Evaluation findById(UUID id) {
        return evaluationRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("ASSESSMENT.EVALUATION.NOT_FOUND",
                        "Evaluacion no encontrada").with("id", id));
    }

    /** Actualiza los campos provistos (no nulos); los nulos se dejan intactos. */
    @Transactional
    public Evaluation update(UUID id, String name, LocalDate evaluationDate, BigDecimal weight,
                             UUID subjectId, String period) {
        Evaluation evaluation = findById(id);
        if (name != null) evaluation.name = name;
        if (evaluationDate != null) evaluation.evaluationDate = evaluationDate;
        if (weight != null) evaluation.weight = weight;
        if (subjectId != null) evaluation.subjectId = subjectId;
        if (period != null) evaluation.period = period;
        return evaluation;
    }

    /**
     * Elimina la evaluacion. Falla con {@code 409} si tiene notas asociadas: no se
     * borra informacion academica de forma implicita.
     *
     * @throws ConflictException ({@code 409}) si la evaluacion tiene notas.
     */
    @Transactional
    public void delete(UUID id) {
        Evaluation evaluation = findById(id);
        if (gradeRepository.countByEvaluation(id) > 0) {
            throw new ConflictException("ASSESSMENT.EVALUATION.HAS_GRADES",
                    "No se puede eliminar una evaluacion con notas registradas").with("id", id);
        }
        evaluationRepository.delete(evaluation);
    }
}
