package cl.duocuc.edutrack.ms.assessment;

import cl.duocuc.edutrack.ms.infrastructure.discovery.ServiceIds;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

/**
 * Punto de montaje JAX-RS del Assessment Service. El {@link ApplicationPath} es el
 * primer segmento del path y, por el contrato de discovery del Gateway, debe
 * coincidir con el nombre logico del servicio ({@link ServiceIds#ASSESSMENT}) y con
 * el nombre de la app en Fly.io ({@code edutrack-assessment}).
 */
@ApplicationPath("/" + ServiceIds.ASSESSMENT)
@OpenAPIDefinition(info = @Info(
        title = "Assessment Service API",
        version = "1.0.0-SNAPSHOT",
        description = "Evaluaciones, notas, promedios ponderados y auditoria del Colegio Bernardo O'Higgins"))
public class AssessmentApplication extends Application {
}
