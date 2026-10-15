package br.com.fiap.soat.vehiclecore.infrastructure.config;

import br.com.fiap.soat.vehiclecore.application.port.SalesServiceGateway;
import br.com.fiap.soat.vehiclecore.infrastructure.client.SalesServiceClient;
import br.com.fiap.soat.vehiclecore.infrastructure.client.SalesServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class SalesServiceClientConfig {

    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    @Bean
    SalesServiceGateway salesServiceGateway(SalesServiceProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(INTERNAL_TOKEN_HEADER, properties.internalToken())
                .build();
        return new SalesServiceClient(restClient, properties.maxAttempts(), properties.retryBackoff());
    }
}
