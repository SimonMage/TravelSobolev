package com.sobolev.travel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobolev.travel.dto.trip.TripCreateRequest;
import com.sobolev.travel.dto.trip.TripDto;
import com.sobolev.travel.dto.trip.TripStopDto;
import com.sobolev.travel.exception.BadRequestException;
import com.sobolev.travel.exception.ResourceNotFoundException;
import com.sobolev.travel.security.CustomUserDetails;
import com.sobolev.travel.security.CustomUserDetailsService;
import com.sobolev.travel.security.JwtTokenProvider;
import com.sobolev.travel.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripService tripService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private CustomUserDetails userDetails;
    private TripDto testTripDto;

    @BeforeEach
    void setUp() {
        userDetails = new CustomUserDetails(
            1, "testuser", "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        testTripDto = new TripDto(
            1, "Test Trip",
            LocalDate.of(2024, 6, 1),
            LocalDate.of(2024, 6, 10),
            List.of(new TripStopDto("Test Stop", "Roma", "Lazio", LocalDate.of(2024, 6, 5), "Notes", Collections.emptyList()))
        );
    }

    @Test
    void getUserTrips_ReturnsTrips() throws Exception {
        when(tripService.getUserTrips(1)).thenReturn(List.of(testTripDto));

        mockMvc.perform(get("/api/trips")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Test Trip"));
    }

    @Test
    void getUserTrips_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/trips"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getTripById_ReturnsTrip() throws Exception {
        when(tripService.getTripByName("Test Trip", 1)).thenReturn(testTripDto);

        mockMvc.perform(get("/api/trips/Test Trip")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Trip"))
            .andExpect(jsonPath("$.stops").isArray());
    }

    @Test
    void getTripById_NotFound() throws Exception {
        when(tripService.getTripByName("NonExistent", 1))
            .thenThrow(new ResourceNotFoundException("Trip 'NonExistent' not found for user"));

        mockMvc.perform(get("/api/trips/NonExistent")
                .with(user(userDetails)))
            .andExpect(status().isNotFound());
    }

    @Test
    void createTrip_Success() throws Exception {
        TripCreateRequest request = new TripCreateRequest(
            "New Trip",
            LocalDate.of(2024, 7, 1),
            LocalDate.of(2024, 7, 10)
        );

        TripDto createdTrip = new TripDto(
            2, "New Trip",
            LocalDate.of(2024, 7, 1),
            LocalDate.of(2024, 7, 10),
            Collections.emptyList()
        );

        when(tripService.createTrip(any(TripCreateRequest.class), eq(1))).thenReturn(createdTrip);

        mockMvc.perform(post("/api/trips")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("New Trip"));
    }

    @Test
    void createTrip_InvalidDates_ReturnsBadRequest() throws Exception {
        TripCreateRequest request = new TripCreateRequest(
            "New Trip",
            LocalDate.of(2024, 7, 10),
            LocalDate.of(2024, 7, 1) // Invalid: end before start
        );

        when(tripService.createTrip(any(TripCreateRequest.class), eq(1)))
            .thenThrow(new BadRequestException("Start date must be before or equal to end date"));

        mockMvc.perform(post("/api/trips")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTrip_Success() throws Exception {
        mockMvc.perform(delete("/api/trips/Test Trip")
                .with(user(userDetails)))
            .andExpect(status().isNoContent());
    }

    @Test
    void exportTripToCsv_Success() throws Exception {
        String csvContent = "Trip Name,Start Date,End Date,Stop Date,City,Notes,POI Name\n" +
                           "\"Test Trip\",\"2024-06-01\",\"2024-06-10\",\"2024-06-05\",\"Roma\",\"Notes\",\"\"";

        when(tripService.exportTripToCsv("Test Trip", 1)).thenReturn(csvContent);

        mockMvc.perform(get("/api/trips/1/export")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Trip Name")));
    }
}
