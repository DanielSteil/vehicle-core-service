package br.com.fiap.soat.vehiclecore.api;

import br.com.fiap.soat.vehiclecore.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlatformEndpointsTest extends IntegrationTest {

    @Test
    void exposesHealthProbesForKubernetes() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void publishesTheOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Vehicle Core Service"))
                .andExpect(jsonPath("$.paths['/vehicles']").exists())
                .andExpect(jsonPath("$.paths['/vehicles/{id}']").exists());
    }
}
