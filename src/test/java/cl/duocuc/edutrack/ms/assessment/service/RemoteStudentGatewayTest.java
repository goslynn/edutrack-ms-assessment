package cl.duocuc.edutrack.ms.assessment.service;

import cl.duocuc.edutrack.ms.assessment.client.StudentClient;
import cl.duocuc.edutrack.ms.infrastructure.exception.DomainException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests del {@link RemoteStudentGateway}: traduce el status HTTP de Student a la
 * semantica de existencia ({@code 200} existe, {@code 404} no), y aplica
 * fail-closed (cualquier otro status / fallo => error, nunca "existe").
 */
class RemoteStudentGatewayTest {

    private StudentClient client;
    private RemoteStudentGateway gateway;
    private final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        client = mock(StudentClient.class);
        gateway = new RemoteStudentGateway();
        gateway.client = client;
    }

    @Test
    @DisplayName("Student responde 200 => el alumno existe")
    void ok_exists() {
        when(client.getById(studentId)).thenReturn(Response.ok().build());
        assertTrue(gateway.exists(studentId));
    }

    @Test
    @DisplayName("Student responde 404 => el alumno no existe")
    void notFound_doesNotExist() {
        when(client.getById(studentId)).thenReturn(Response.status(Response.Status.NOT_FOUND).build());
        assertFalse(gateway.exists(studentId));
    }

    @Test
    @DisplayName("Student responde 500 => DomainException (fail-closed, no se asume que existe)")
    void serverError_failsClosed() {
        when(client.getById(studentId)).thenReturn(Response.serverError().build());
        DomainException ex = assertThrows(DomainException.class, () -> gateway.exists(studentId));
        assertTrue(ex.status() >= 500);
    }

    @Test
    @DisplayName("El cliente lanza (timeout/caido) => DomainException (fail-closed)")
    void clientThrows_failsClosed() {
        when(client.getById(studentId)).thenThrow(new RuntimeException("connection refused"));
        DomainException ex = assertThrows(DomainException.class, () -> gateway.exists(studentId));
        assertTrue(ex.status() >= 500);
    }
}
