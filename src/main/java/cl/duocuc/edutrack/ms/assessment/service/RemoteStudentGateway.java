package cl.duocuc.edutrack.ms.assessment.service;

import cl.duocuc.edutrack.ms.assessment.client.StudentClient;
import cl.duocuc.edutrack.ms.infrastructure.exception.DomainException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.UUID;

/**
 * Implementacion por defecto de {@link StudentGateway}: consulta la existencia del
 * alumno al Student Service via {@link StudentClient} ({@code GET /student/students/{id}}).
 *
 * <h3>Traduccion de status</h3>
 * <ul>
 *   <li>{@code 2xx} ⇒ el alumno existe ({@code true}).</li>
 *   <li>{@code 404} ⇒ el alumno no existe ({@code false}); el servicio de notas lo
 *       traduce a {@code 404 BE-ASS-004}.</li>
 *   <li>cualquier otro status / excepcion ⇒ {@link DomainException} (fail-closed):
 *       ante un fallo de Student no se asume que el alumno existe, para no
 *       registrar notas huerfanas.</li>
 * </ul>
 *
 * <p>El client retorna {@link Response} crudo (no dispara mappers de error de MP
 * REST), por eso el status se inspecciona aqui directamente.</p>
 */
@ApplicationScoped
public class RemoteStudentGateway implements StudentGateway {

    private static final Logger LOG = Logger.getLogger(RemoteStudentGateway.class);

    @Inject
    @RestClient
    StudentClient client;

    @Override
    public boolean exists(UUID studentId) {
        Response response = null;
        try {
            response = client.getById(studentId);
            int status = response.getStatus();
            if (status == Response.Status.NOT_FOUND.getStatusCode()) {
                return false;
            }
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                return true;
            }
            throw new DomainException(502, "ASSESSMENT.STUDENT.UPSTREAM_ERROR",
                    "Student Service respondio un status inesperado: " + status)
                    .with("studentId", studentId);
        } catch (DomainException e) {
            throw e;
        } catch (RuntimeException e) {
            LOG.warnf(e, "Fallo verificando existencia del alumno %s en Student — fail-closed", studentId);
            throw new DomainException(503, "ASSESSMENT.STUDENT.UNAVAILABLE",
                    "Student Service no disponible", e).with("studentId", studentId);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
