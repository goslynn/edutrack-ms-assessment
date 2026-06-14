package cl.duocuc.edutrack.ms.assessment.repository;

import cl.duocuc.edutrack.ms.assessment.model.entity.Evaluation;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Panache de {@link Evaluation}. El CRUD basico ({@code persist},
 * {@code findByIdOptional}, {@code delete}) lo aporta {@link PanacheRepositoryBase};
 * aqui solo el listado filtrable por asignatura y/o periodo.
 */
@ApplicationScoped
public class EvaluationRepository implements PanacheRepositoryBase<Evaluation, UUID> {

    /**
     * Listado de evaluaciones; cada filtro ({@code subjectId}, {@code period}) es
     * opcional ({@code null} = sin filtrar por ese campo).
     */
    public List<Evaluation> list(UUID subjectId, String period) {
        if (subjectId != null && period != null) {
            return list("subjectId = ?1 and period = ?2", subjectId, period);
        }
        if (subjectId != null) {
            return list("subjectId", subjectId);
        }
        if (period != null) {
            return list("period", period);
        }
        return listAll();
    }
}
