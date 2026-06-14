package cl.duocuc.edutrack.ms.assessment.repository;

import cl.duocuc.edutrack.ms.assessment.model.entity.GradeAuditLog;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Panache del log de auditoria de notas. Solo expone
 * {@code persist} (heredado) y lecturas: el log es <b>inmutable</b>, no hay
 * actualizacion ni borrado ({@code BE-ASS-003}).
 */
@ApplicationScoped
public class GradeAuditLogRepository implements PanacheRepositoryBase<GradeAuditLog, UUID> {

    /** Historial de una nota, en orden cronologico de cambio. */
    public List<GradeAuditLog> findByGrade(UUID gradeId) {
        return list("gradeId", Sort.by("createdAt"), gradeId);
    }
}
