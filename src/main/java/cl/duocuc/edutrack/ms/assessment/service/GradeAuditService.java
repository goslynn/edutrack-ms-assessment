package cl.duocuc.edutrack.ms.assessment.service;

import cl.duocuc.edutrack.ms.assessment.model.entity.Grade;
import cl.duocuc.edutrack.ms.assessment.model.entity.GradeAuditLog;
import cl.duocuc.edutrack.ms.assessment.repository.GradeAuditLogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Componente de auditoria de notas ({@code BE-ASS-003}). Centraliza el
 * <b>append</b> de un {@link GradeAuditLog} cada vez que una nota se crea o se
 * modifica, y la lectura del historial. No anota {@code @Transactional}: se
 * invoca dentro de la transaccion del {@link GradeService}, de modo que la nota y
 * su entrada de auditoria se confirman atomicamente.
 *
 * <p>El "quien" y el "cuando" no se setean aqui: viajan con la superclase
 * {@code CreatableEntity} ({@code creatorUser}/{@code createdAt}, rellenados por
 * {@code AuditContext} en el {@code @PrePersist}). Aqui solo se fijan los datos
 * propios del cambio (ids denormalizados + valor anterior/nuevo).</p>
 */
@ApplicationScoped
public class GradeAuditService {

    @Inject
    GradeAuditLogRepository repository;

    /**
     * Registra un cambio de nota en el log inmutable.
     *
     * @param grade    nota afectada (ya persistida: aporta {@code id},
     *                 {@code studentId} y la evaluacion)
     * @param oldValue valor anterior, o {@code null} si es el alta de la nota
     * @param newValue valor nuevo
     */
    public void record(Grade grade, BigDecimal oldValue, BigDecimal newValue) {
        GradeAuditLog log = new GradeAuditLog();
        log.gradeId = grade.id;
        log.studentId = grade.studentId;
        log.evaluationId = grade.evaluation.id;
        log.oldValue = oldValue;
        log.newValue = newValue;
        repository.persist(log);
    }

    /** Historial cronologico de cambios de una nota. */
    public List<GradeAuditLog> history(UUID gradeId) {
        return repository.findByGrade(gradeId);
    }
}
