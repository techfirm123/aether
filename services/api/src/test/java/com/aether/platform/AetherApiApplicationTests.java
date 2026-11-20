package com.aether.platform;

import com.aether.platform.web.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AetherTestConfiguration.class)
class AetherApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void livenessEndpointReportsApplicationIsAlive() throws Exception {

        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"status":"UP"}
                        """));
    }

    @Test
    void readinessEndpointReportsApplicationAndDatabaseAreReady() throws Exception {

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"status":"UP"}
                        """));
    }

    @Test
    void openApiDocumentIsAvailableInTestProfile() throws Exception {

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void correlationIdIsGeneratedAndReturnedForEveryRequest() throws Exception {

        MvcResult result = mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andReturn();

        String correlationId = result.getResponse()
                .getHeader(CorrelationIdFilter.HEADER_NAME);

        assertThat(correlationId).isNotBlank();
        assertThat(UUID.fromString(correlationId)).isNotNull();
    }

    @Test
    void validCallerCorrelationIdIsPreserved() throws Exception {

        String suppliedId = "2b1bb621-57d5-4eb5-9362-9d63b6f0b2c1";

        mockMvc.perform(get("/actuator/health/liveness")
                        .header(CorrelationIdFilter.HEADER_NAME, suppliedId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string(CorrelationIdFilter.HEADER_NAME, suppliedId));
    }

    @Test
    void unknownRouteRemainsNotFound() throws Exception {

        mockMvc.perform(get("/api/v1/does-not-exist"))
                .andExpect(status().isNotFound());
    }
}