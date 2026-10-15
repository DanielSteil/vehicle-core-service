package br.com.fiap.soat.vehiclecore.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Vehicle Core Service")
                .version("1.0.0")
                .description("Serviço principal da revenda: cadastro e edição de veículos. "
                        + "Cada alteração é repassada ao serviço de venda por HTTP."));
    }
}
