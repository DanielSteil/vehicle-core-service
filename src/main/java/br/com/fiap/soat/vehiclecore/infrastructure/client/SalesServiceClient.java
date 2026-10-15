package br.com.fiap.soat.vehiclecore.infrastructure.client;

import br.com.fiap.soat.vehiclecore.application.exception.SalesServiceIntegrationException;
import br.com.fiap.soat.vehiclecore.application.exception.SalesServiceUnavailableException;
import br.com.fiap.soat.vehiclecore.application.port.SalesServiceGateway;
import br.com.fiap.soat.vehiclecore.domain.exception.VehicleNotEditableException;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Cliente HTTP do serviço de venda. Repete a chamada em falhas transitórias, o que é
 * seguro porque o PUT é idempotente; erros 4xx não são repetidos.
 */
public class SalesServiceClient implements SalesServiceGateway {

    private static final Logger log = LoggerFactory.getLogger(SalesServiceClient.class);

    private final RestClient restClient;
    private final int maxAttempts;
    private final Duration retryBackoff;

    public SalesServiceClient(RestClient restClient, int maxAttempts, Duration retryBackoff) {
        this.restClient = restClient;
        this.maxAttempts = maxAttempts;
        this.retryBackoff = retryBackoff;
    }

    @Override
    public void syncVehicle(Vehicle vehicle) {
        SyncVehicleRequest body = SyncVehicleRequest.from(vehicle.getData());
        for (int attempt = 1; ; attempt++) {
            try {
                restClient.put()
                        .uri("/internal/vehicles/{id}", vehicle.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (HttpClientErrorException.Conflict e) {
                throw new VehicleNotEditableException(vehicle.getId());
            } catch (HttpClientErrorException e) {
                throw new SalesServiceIntegrationException(
                        "O serviço de venda recusou a sincronização: HTTP " + e.getStatusCode().value(), e);
            } catch (HttpServerErrorException | ResourceAccessException e) {
                if (attempt >= maxAttempts) {
                    throw new SalesServiceUnavailableException(
                            "Serviço de venda indisponível após " + attempt + " tentativa(s)", e);
                }
                log.warn("Falha ao sincronizar o veículo {} (tentativa {}/{}): {}",
                        vehicle.getId(), attempt, maxAttempts, e.getMessage());
                waitBeforeRetry(attempt);
            }
        }
    }

    private void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(retryBackoff.multipliedBy(attempt));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SalesServiceUnavailableException("Sincronização interrompida", e);
        }
    }
}
