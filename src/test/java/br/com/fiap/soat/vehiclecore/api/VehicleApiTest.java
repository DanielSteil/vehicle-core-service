package br.com.fiap.soat.vehiclecore.api;

import br.com.fiap.soat.vehiclecore.IntegrationTest;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VehicleApiTest extends IntegrationTest {

    private ResultActions create(String json) throws Exception {
        return mockMvc.perform(post("/vehicles").contentType(APPLICATION_JSON).content(json));
    }

    private ResultActions update(String id, String json) throws Exception {
        return mockMvc.perform(put("/vehicles/{id}", id).contentType(APPLICATION_JSON).content(json));
    }

    private String createAndGetId(String model, String price) throws Exception {
        String body = create(vehicleJson("Toyota", model, 2022, "Prata", price))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void createRegistersTheVehicleAndPublishesItToTheSalesService() throws Exception {
        String body = create(vehicleJson("Toyota", "Corolla", 2022, "Prata", "120000.00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.price").value(120000.00))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");

        SALES_SERVICE.verify(putRequestedFor(urlEqualTo("/internal/vehicles/" + id))
                .withHeader("X-Internal-Token", equalTo(INTERNAL_TOKEN))
                .withRequestBody(equalToJson("""
                        {"brand":"Toyota","model":"Corolla","year":2022,"color":"Prata","price":120000.00}
                        """)));
        mockMvc.perform(get("/vehicles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Corolla"));
    }

    @Test
    void createReturnsTheLocationOfTheNewVehicle() throws Exception {
        String location = create(vehicleJson("Toyota", "Yaris", 2023, "Branco", "95000.00"))
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location)).andExpect(status().isOk());
    }

    @Test
    void createIsRolledBackWhenTheSalesServiceIsDown() throws Exception {
        salesServiceResponds(503);

        create(vehicleJson("Toyota", "Corolla", 2022, "Prata", "120000.00"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Serviço de venda indisponível"));

        SALES_SERVICE.verify(3, putRequestedFor(urlPathMatching(SYNC_PATH)));
        mockMvc.perform(get("/vehicles")).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void createSucceedsWhenARetryGoesThrough() throws Exception {
        SALES_SERVICE.resetAll();
        SALES_SERVICE.stubFor(WireMock.put(urlPathMatching(SYNC_PATH))
                .inScenario("instável").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500)).willSetStateTo("recuperado"));
        SALES_SERVICE.stubFor(WireMock.put(urlPathMatching(SYNC_PATH))
                .inScenario("instável").whenScenarioStateIs("recuperado")
                .willReturn(aResponse().withStatus(201)));

        create(vehicleJson("Toyota", "Corolla", 2022, "Prata", "120000.00")).andExpect(status().isCreated());

        SALES_SERVICE.verify(2, putRequestedFor(urlPathMatching(SYNC_PATH)));
    }

    @Test
    void wrongIntegrationSetupIsReportedAsBadGateway() throws Exception {
        salesServiceResponds(401);

        create(vehicleJson("Toyota", "Corolla", 2022, "Prata", "120000.00"))
                .andExpect(status().isBadGateway());

        mockMvc.perform(get("/vehicles")).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void createValidatesThePayload() throws Exception {
        create("""
                {"brand":"","model":"Corolla","color":"Prata","price":0}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.brand").exists())
                .andExpect(jsonPath("$.errors.year").exists())
                .andExpect(jsonPath("$.errors.price").exists());

        SALES_SERVICE.verify(0, putRequestedFor(urlPathMatching(SYNC_PATH)));
    }

    @Test
    void createRejectsAYearTooFarAhead() throws Exception {
        create(vehicleJson("Toyota", "Corolla", 2999, "Prata", "120000.00"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
    }

    @Test
    void updateChangesTheVehicleAndPublishesTheChange() throws Exception {
        String id = createAndGetId("Corolla", "120000.00");

        update(id, vehicleJson("Toyota", "Corolla", 2022, "Prata", "115000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(115000.00));

        SALES_SERVICE.verify(putRequestedFor(urlEqualTo("/internal/vehicles/" + id))
                .withRequestBody(equalToJson("""
                        {"brand":"Toyota","model":"Corolla","year":2022,"color":"Prata","price":115000.00}
                        """)));
    }

    @Test
    void updateIsRejectedAndRolledBackWhenTheVehicleWasAlreadyReservedOrSold() throws Exception {
        String id = createAndGetId("Corolla", "120000.00");
        salesServiceResponds(409);

        update(id, vehicleJson("Toyota", "Corolla", 2022, "Prata", "1.00"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("reservado")));

        mockMvc.perform(get("/vehicles/{id}", id)).andExpect(jsonPath("$.price").value(120000.00));
    }

    @Test
    void updateOfUnknownVehicleIsNotFound() throws Exception {
        update(UUID.randomUUID().toString(), vehicleJson("Toyota", "Corolla", 2022, "Prata", "1.00"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOfUnknownVehicleIsNotFound() throws Exception {
        mockMvc.perform(get("/vehicles/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }

    @Test
    void listsTheRegisteredVehiclesNewestFirst() throws Exception {
        createAndGetId("Primeiro", "10000.00");
        createAndGetId("Segundo", "20000.00");
        createAndGetId("Terceiro", "30000.00");

        mockMvc.perform(get("/vehicles").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].model").value("Terceiro"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void rejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/vehicles").param("size", "500")).andExpect(status().isBadRequest());
    }
}
