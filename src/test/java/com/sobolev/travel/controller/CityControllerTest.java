package com.sobolev.travel.controller;

import com.sobolev.travel.dto.geography.CityDto;
import com.sobolev.travel.dto.external.WeatherResponse;
import com.sobolev.travel.security.CustomUserDetails;
import com.sobolev.travel.security.CustomUserDetailsService;
import com.sobolev.travel.security.JwtTokenProvider;
import com.sobolev.travel.service.CityService;
import com.sobolev.travel.service.ExternalApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CityController.class)
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CityService cityService;

    @MockBean
    private ExternalApiService externalApiService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private CityDto testCityDto;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        testCityDto = new CityDto(1, "Roma", 41.9028, 12.4964, 1, "Lazio", "Italia", List.of("arte", "storia"));
        userDetails = new CustomUserDetails(
            1, "testuser", "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void getAllCities_ReturnsAllCities() throws Exception {
        when(cityService.getAllCities()).thenReturn(List.of(testCityDto));

        mockMvc.perform(get("/api/cities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Roma"))
            .andExpect(jsonPath("$[0].countryName").value("Italia"));
    }

    @Test
    void getCitiesByRegion_ReturnsFilteredCities() throws Exception {
        when(cityService.getCitiesByRegion(1)).thenReturn(List.of(testCityDto));

        mockMvc.perform(get("/api/cities")
                .param("regionId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].regionId").value(1));
    }

    @Test
    void searchCities_ReturnsMatchingCities() throws Exception {
        when(cityService.searchCities("rom")).thenReturn(List.of(testCityDto));

        mockMvc.perform(get("/api/cities")
                .param("search", "rom"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Roma"));
    }

    @Test
    void getCitiesByTags_ReturnsMatchingCities() throws Exception {
        when(cityService.getCitiesByTags(List.of("arte"))).thenReturn(List.of(testCityDto));

        mockMvc.perform(get("/api/cities")
                .param("tags", "arte"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].tags").isArray());
    }

    @Test
    void getCityById_AnonymousUser_DoesNotSaveHistory() throws Exception {
        when(cityService.getCityById(eq(1), any())).thenReturn(testCityDto);

        mockMvc.perform(get("/api/cities/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Roma"));
    }

    @Test
    void getCityById_AuthenticatedUser_SavesHistory() throws Exception {
        when(cityService.getCityById(1, 1)).thenReturn(testCityDto);

        mockMvc.perform(get("/api/cities/1")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Roma"));
    }

    @Test
    void getCityWeather_ReturnsWeather() throws Exception {
        WeatherResponse weatherResponse = new WeatherResponse(
            "Roma", 25.5, 24.0, 60, "clear sky", "01d", 3.5, 1015
        );

        when(externalApiService.getWeatherForCity(1)).thenReturn(weatherResponse);

        mockMvc.perform(get("/api/cities/1/weather"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cityName").value("Roma"))
            .andExpect(jsonPath("$.temperature").value(25.5));
    }
}
