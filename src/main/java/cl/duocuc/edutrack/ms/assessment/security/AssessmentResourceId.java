package cl.duocuc.edutrack.ms.assessment.security;

/**
 * Catalogo de <em>resource keys</em> que el Assessment Service protege con permisos
 * Unix-style (modelo de {@code infrastructure.security}). Cada clave es un
 * identificador estable y legible ({@code "<servicio>.<recurso>"}); es opaca para
 * Auth (solo se compara por igualdad) y constituye el contrato cross-servicio: el
 * mismo string nombra el recurso en ambos lados de un grant, sin UUIDs que
 * coordinar.
 *
 * <p>Las claves se usan como valor de {@code @RequirePermission(resource = ...)}
 * y deben coincidir con los grants sembrados en Auth. El wildcard global vive en
 * {@code infrastructure.security.ResourceIds#ALL}.</p>
 */
public interface AssessmentResourceId {

    /** Evaluaciones: {@code POST/GET/PUT/DELETE /assessment/evaluations}. */
    String EVALUATIONS = "assessment.evaluations";

    /**
     * Notas: {@code POST /assessment/evaluations/{id}/grades},
     * {@code GET/PUT /assessment/grades/...} y promedios ponderados.
     */
    String GRADES = "assessment.grades";

    /**
     * Historial de modificaciones de notas (auditoria inmutable). Lectura
     * reservada a ADMIN/SUPERUSER ({@code GET /assessment/grades/{id}/history}):
     * el grant de esta clave NO debe concederse al rol docente, que registra y
     * modifica notas pero no debe poder leer ni manipular el log de cambios.
     */
    String AUDIT = "assessment.audit";
}
