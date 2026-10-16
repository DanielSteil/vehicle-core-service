package br.com.fiap.soat.vehiclecore;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Sobe a aplicação inteira contra um PostgreSQL real em contêiner e um serviço de venda
 * simulado pelo WireMock, que responde por HTTP de verdade.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTest {

    protected static final String INTERNAL_TOKEN = "test-internal-token";
    protected static final String SYNC_PATH = "/internal/vehicles/[0-9a-f-]{36}";

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    protected static final WireMockServer SALES_SERVICE = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        POSTGRES.start();
        SALES_SERVICE.start();
    }

    @DynamicPropertySource
    static void salesServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("app.sales-service.base-url", SALES_SERVICE::baseUrl);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetState() {
        jdbcTemplate.execute("TRUNCATE TABLE vehicles");
        SALES_SERVICE.resetAll();
        salesServiceResponds(201);
    }

    protected static void salesServiceResponds(int status) {
        SALES_SERVICE.stubFor(put(urlPathMatching(SYNC_PATH)).willReturn(aResponse().withStatus(status)));
    }

    protected static String vehicleJson(String brand, String model, int year, String color, String price) {
        return """
                {"brand":"%s","model":"%s","year":%d,"color":"%s","price":%s}
                """.formatted(brand, model, year, color, price);
    }
}
