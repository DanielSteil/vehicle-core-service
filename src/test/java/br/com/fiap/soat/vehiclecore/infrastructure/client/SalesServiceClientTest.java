package br.com.fiap.soat.vehiclecore.infrastructure.client;

import br.com.fiap.soat.vehiclecore.application.exception.SalesServiceIntegrationException;
import br.com.fiap.soat.vehiclecore.application.exception.SalesServiceUnavailableException;
import br.com.fiap.soat.vehiclecore.domain.exception.VehicleNotEditableException;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import br.com.fiap.soat.vehiclecore.domain.model.VehicleData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SalesServiceClientTest {

    private static final int MAX_ATTEMPTS = 3;

    private final Vehicle vehicle = new Vehicle(UUID.randomUUID(),
            new VehicleData("BYD", "Dolphin", 2025, "Verde", new BigDecimal("149800.00")), 0L);
    private final String url = "http://sales/internal/vehicles/" + vehicle.getId();

    private MockRestServiceServer server;
    private SalesServiceClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://sales");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SalesServiceClient(builder.build(), MAX_ATTEMPTS, Duration.ofMillis(1));
    }

    @Test
    void sendsTheVehicleDataWithPut() {
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json("""
                        {"brand":"BYD","model":"Dolphin","year":2025,"color":"Verde","price":149800.00}
                        """))
                .andRespond(withSuccess());

        assertThatCode(() -> client.syncVehicle(vehicle)).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void conflictMeansTheVehicleCanNoLongerBeEdited() {
        server.expect(requestTo(url)).andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> client.syncVehicle(vehicle)).isInstanceOf(VehicleNotEditableException.class);
        server.verify();
    }

    @Test
    void otherClientErrorsAreNotRetried() {
        server.expect(ExpectedCount.once(), requestTo(url)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.syncVehicle(vehicle))
                .isInstanceOf(SalesServiceIntegrationException.class)
                .hasMessageContaining("401");
        server.verify();
    }

    @Test
    void retriesServerErrorsUntilTheLimit() {
        server.expect(ExpectedCount.times(MAX_ATTEMPTS), requestTo(url)).andRespond(withServerError());

        assertThatThrownBy(() -> client.syncVehicle(vehicle))
                .isInstanceOf(SalesServiceUnavailableException.class)
                .hasMessageContaining("3 tentativa");
        server.verify();
    }

    @Test
    void recoversWhenARetrySucceeds() {
        server.expect(requestTo(url)).andRespond(withServerError());
        server.expect(requestTo(url)).andRespond(withSuccess());

        assertThatCode(() -> client.syncVehicle(vehicle)).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void retriesConnectionFailures() {
        server.expect(ExpectedCount.times(MAX_ATTEMPTS), requestTo(url))
                .andRespond(withException(new IOException("Connection refused")));

        assertThatThrownBy(() -> client.syncVehicle(vehicle)).isInstanceOf(SalesServiceUnavailableException.class);
        server.verify();
    }

    @Test
    void stopsRetryingWhenInterrupted() {
        server.expect(requestTo(url)).andRespond(withServerError());
        Thread.currentThread().interrupt();

        try {
            assertThatThrownBy(() -> client.syncVehicle(vehicle))
                    .isInstanceOf(SalesServiceUnavailableException.class)
                    .hasMessage("Sincronização interrompida");
        } finally {
            Thread.interrupted();
        }
    }
}
