package com.sobolev.travel.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobolev.travel.dto.auth.AuthResponse;
import com.sobolev.travel.dto.auth.RegisterRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GeographyIntegrationTest {

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
        RegisterRequest registerRequest = new RegisterRequest(
            "geouser" + System.currentTimeMillis(),
            "geo" + System.currentTimeMillis() + "@test.com",
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
    void getCountries_ReturnsSeededData() throws Exception {
        mockMvc.perform(get("/api/countries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
            .andExpect(jsonPath("$[?(@.name == 'Italia')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'Francia')]").exists());
    }

    @Test
    void getRegions_ReturnsAllRegions() throws Exception {
        mockMvc.perform(get("/api/regions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))))
            .andExpect(jsonPath("$[?(@.name == 'Lombardia')]").exists());
    }

    @Test
    void getRegions_FilterByCountry() throws Exception {
        mockMvc.perform(get("/api/regions")
                .param("countryId", "1")) // Italia
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.countryName == 'Italia')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'Lombardia')]").exists());
    }

    @Test
    void getCities_ReturnsAllCities() throws Exception {
        mockMvc.perform(get("/api/cities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))))
            .andExpect(jsonPath("$[?(@.name == 'Roma')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'Milano')]").exists());
    }

    @Test
    void getCities_FilterByRegion() throws Exception {
        mockMvc.perform(get("/api/cities")
                .param("regionId", "2")) // Lazio
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'Roma')]").exists());
    }

    @Test
    void getCities_SearchByName() throws Exception {
        mockMvc.perform(get("/api/cities")
                .param("search", "rom"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'Roma')]").exists());
    }

    @Test
    void getCities_FilterByTags() throws Exception {
        mockMvc.perform(get("/api/cities")
                .param("tags", "arte"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].tags", hasItem("arte")));
    }

    @Test
    void getCityById_ReturnsCity() throws Exception {
        mockMvc.perform(get("/api/cities/2")) // Roma
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Roma"))
            .andExpect(jsonPath("$.regionName").value("Lazio"))
            .andExpect(jsonPath("$.countryName").value("Italia"))
            .andExpect(jsonPath("$.latitude").exists())
            .andExpect(jsonPath("$.longitude").exists())
            .andExpect(jsonPath("$.tags").isArray());
    }

    @Test
    void getCityById_SavesSearchHistory_WhenAuthenticated() throws Exception {
        // Access city as authenticated user
        mockMvc.perform(get("/api/cities/2")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk());

        // Check search history
        mockMvc.perform(get("/api/search-history")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.cityId == 2)]").exists());
    }

    @Test
    void getTags_ReturnsAllTags() throws Exception {
        mockMvc.perform(get("/api/tags"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))))
            .andExpect(jsonPath("$[?(@.name == 'arte')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'storia')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'gastronomia')]").exists());
    }

    @Test
    void searchHistory_ClearHistory() throws Exception {
        // First access a city to create history
        mockMvc.perform(get("/api/cities/1")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk());

        // Clear history
        mockMvc.perform(delete("/api/search-history")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isNoContent());

        // Verify cleared
        mockMvc.perform(get("/api/search-history")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }
}
