package cl.duocuc.edutrack.ms.assessment.service;

import java.util.UUID;

/**
 * Puerto (Adapter pattern) hacia el Student Service para verificar integridad
 * referencial: una nota no puede registrarse para un alumno inexistente
 * ({@code BE-ASS-004}). Es un contrato CDI para que el servicio de notas dependa
 * de una abstraccion testeable (mock en unit tests) y no del cliente HTTP
 * concreto; la implementacion por defecto ({@link RemoteStudentGateway}) consulta
 * a Student via REST.
 */
public interface StudentGateway {

    /**
     * @param studentId id del alumno (opaco, propiedad del Student Service)
     * @return {@code true} si el alumno existe en Student; {@code false} si Student
     *         responde {@code 404}.
     * @throws cl.duocuc.edutrack.ms.infrastructure.exception.DomainException si
     *         Student no esta disponible o responde un error inesperado
     *         (fail-closed: no se asume que el alumno existe ante un fallo, para
     *         no crear notas huerfanas).
     */
    boolean exists(UUID studentId);
}
