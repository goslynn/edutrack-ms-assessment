package cl.duocuc.edutrack.ms.assessment.client;

import cl.duocuc.edutrack.ms.infrastructure.discovery.IdentityHeadersFactory;
import cl.duocuc.edutrack.ms.infrastructure.discovery.ServiceIds;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.UUID;

/**
 * Cliente declarativo del Student Service. La URL la deriva la infraestructura de
 * discovery de commons ({@code DiscoveryConfigSourceFactory} sustituye
 * {@code {service}} = {@link ServiceIds#STUDENT} en {@code edutrack.discovery.pattern});
 * la identidad del request ({@code X-User-Id}/{@code X-User-Roles}) se reenvia sola
 * via {@link IdentityHeadersFactory}.
 *
 * <p>Sigue el estilo de client de la plataforma: retorna {@link Response} crudo
 * para que el consumidor ({@link cl.duocuc.edutrack.ms.assessment.service.RemoteStudentGateway})
 * inspeccione el status (200 existe / 404 no existe) sin acoplarse a los DTOs de
 * Student. La llamada es directa app-a-app, no pasa por el Gateway.</p>
 */
@RegisterRestClient(configKey = ServiceIds.STUDENT)
@RegisterClientHeaders(IdentityHeadersFactory.class)
public interface StudentClient {

    @GET
    @Path("/" + ServiceIds.STUDENT + "/students/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getById(@PathParam("id") UUID id);
}
