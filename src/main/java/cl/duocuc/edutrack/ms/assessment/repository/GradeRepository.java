package cl.duocuc.edutrack.ms.assessment.repository;

import cl.duocuc.edutrack.ms.assessment.model.entity.Grade;
import cl.duocuc.edutrack.ms.assessment.service.WeightedScore;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repositorio Panache de {@link Grade}. Aporta las queries de dominio: unicidad
 * por (evaluacion, alumno), listados filtrables y la proyeccion
 * {@code (nota, ponderacion)} que alimenta el promedio ponderado.
 */
@ApplicationScoped
public class GradeRepository implements PanacheRepositoryBase<Grade, UUID> {

    /** {@code true} si el alumno ya tiene nota en esa evaluacion (unicidad). */
    public boolean existsForStudent(UUID evaluationId, UUID studentId) {
        return count("evaluation.id = ?1 and studentId = ?2", evaluationId, studentId) > 0;
    }

    /** Notas registradas en una evaluacion. */
    public List<Grade> listByEvaluation(UUID evaluationId) {
        return list("evaluation.id", evaluationId);
    }

    /** Cuantas notas cuelgan de una evaluacion (guard para borrarla). */
    public long countByEvaluation(UUID evaluationId) {
        return count("evaluation.id", evaluationId);
    }

    /**
     * Listado de notas; cada filtro ({@code studentId}, {@code subjectId},
     * {@code period}) es opcional. {@code subjectId}/{@code period} navegan la
     * asociacion con la evaluacion.
     */
    public List<Grade> listFiltered(UUID studentId, UUID subjectId, String period) {
        StringBuilder q = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        if (studentId != null) {
            q.append(" and studentId = :studentId");
            params.put("studentId", studentId);
        }
        if (subjectId != null) {
            q.append(" and evaluation.subjectId = :subjectId");
            params.put("subjectId", subjectId);
        }
        if (period != null) {
            q.append(" and evaluation.period = :period");
            params.put("period", period);
        }
        return list(q.toString(), params);
    }

    /**
     * Proyeccion {@code (nota, ponderacion)} de un alumno en una asignatura y
     * periodo, cruzando cada nota con la ponderacion de su evaluacion. Es lo que
     * consume {@link cl.duocuc.edutrack.ms.assessment.service.WeightedAverageCalculator}.
     */
    public List<WeightedScore> weightedScores(UUID studentId, UUID subjectId, String period) {
        return getEntityManager().createQuery(
                        "select new cl.duocuc.edutrack.ms.assessment.service.WeightedScore(g.score, e.weight) "
                                + "from Grade g join g.evaluation e "
                                + "where g.studentId = :studentId "
                                + "and e.subjectId = :subjectId "
                                + "and e.period = :period",
                        WeightedScore.class)
                .setParameter("studentId", studentId)
                .setParameter("subjectId", subjectId)
                .setParameter("period", period)
                .getResultList();
    }
}
