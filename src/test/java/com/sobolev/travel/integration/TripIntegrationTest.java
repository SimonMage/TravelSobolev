package com.sobolev.travel.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobolev.travel.dto.auth.AuthResponse;
import com.sobolev.travel.dto.auth.RegisterRequest;
import com.sobolev.travel.dto.trip.TripCreateRequest;
import com.sobolev.travel.dto.trip.TripStopCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TripIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("Sobolev")
        .withUsername("postgres")
        .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register a test user and get token
        RegisterRequest registerRequest = new RegisterRequest(
            "tripuser" + System.currentTimeMillis(),
            "trip" + System.currentTimeMillis() + "@test.com",
            "password123",
            null,
            null,
            null
        );

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(), AuthResponse.class);
        authToken = authResponse.token();
    }

    @Test
    void createTrip_Success() throws Exception {
        TripCreateRequest request = new TripCreateRequest(
            "Integration Test Trip",
            LocalDate.of(2024, 8, 1),
            LocalDate.of(2024, 8, 15)
        );

        mockMvc.perform(post("/api/trips")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Integration Test Trip"))
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void fullTripWorkflow() throws Exception {
        // Create trip
        TripCreateRequest tripRequest = new TripCreateRequest(
            "Full Workflow Trip",
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 9, 10)
        );

        MvcResult createResult = mockMvc.perform(post("/api/trips")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tripRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Integer tripId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .get("id").asInt();

        // Get trip
        mockMvc.perform(get("/api/trips/" + tripId)
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Full Workflow Trip"));

        // Add stop (using Roma which is seeded with id 2)
        TripStopCreateRequest stopRequest = new TripStopCreateRequest(
            "Roma",
            "Lazio",
            LocalDate.of(2024, 9, 5),
            "Visit Colosseum"
        );

        mockMvc.perform(post("/api/trips/" + tripId + "/stops")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stopRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cityName").value("Roma"));

        // Get trip with stop
        mockMvc.perform(get("/api/trips/" + tripId)
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stops").isArray())
            .andExpect(jsonPath("$.stops[0].cityName").value("Roma"));

        // Export CSV
        mockMvc.perform(get("/api/trips/" + tripId + "/export")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv"));

        // Delete trip
        mockMvc.perform(delete("/api/trips/" + tripId)
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/trips/" + tripId)
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void createTrip_InvalidDates_ReturnsBadRequest() throws Exception {
        TripCreateRequest request = new TripCreateRequest(
            "Invalid Trip",
            LocalDate.of(2024, 8, 15),
            LocalDate.of(2024, 8, 1) // End before start
        );

        mockMvc.perform(post("/api/trips")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Start date must be before or equal to end date"));
    }

    @Test
    void addStop_DateOutOfRange_ReturnsBadRequest() throws Exception {
        // Create trip
        TripCreateRequest tripRequest = new TripCreateRequest(
            "Date Range Test Trip",
            LocalDate.of(2024, 10, 1),
            LocalDate.of(2024, 10, 10)
        );

        MvcResult createResult = mockMvc.perform(post("/api/trips")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tripRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Integer tripId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .get("id").asInt();

        // Try to add stop outside date range
        TripStopCreateRequest stopRequest = new TripStopCreateRequest(
            "Roma",
            "Lazio",
            LocalDate.of(2024, 10, 15), // Outside trip dates
            "Invalid stop"
        );

        mockMvc.perform(post("/api/trips/" + tripId + "/stops")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stopRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Stop date must be within trip date range"));
    }
}
