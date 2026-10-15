package br.com.fiap.soat.vehiclecore.infrastructure.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuração da chamada ao serviço de venda.
 *
 * @param maxAttempts  total de tentativas para falhas transitórias (timeout, conexão recusada, 5xx)
 * @param retryBackoff espera antes da 2ª tentativa; cresce linearmente nas seguintes
 */
@Validated
@ConfigurationProperties(prefix = "app.sales-service")
public record SalesServiceProperties(
        @NotBlank String baseUrl,
        @NotBlank String internalToken,
        @NotNull Duration connectTimeout,
        @NotNull Duration readTimeout,
        @Min(1) int maxAttempts,
        @NotNull Duration retryBackoff) {
}
