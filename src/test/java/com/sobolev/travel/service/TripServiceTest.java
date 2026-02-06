package com.sobolev.travel.service;

import com.sobolev.travel.dto.trip.TripCreateRequest;
import com.sobolev.travel.dto.trip.TripDto;
import com.sobolev.travel.dto.trip.TripStopCreateRequest;
import com.sobolev.travel.dto.trip.TripStopDto;
import com.sobolev.travel.entity.*;
import com.sobolev.travel.exception.BadRequestException;
import com.sobolev.travel.exception.ResourceNotFoundException;
import com.sobolev.travel.mapper.EntityMapper;
import com.sobolev.travel.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripStopRepository tripStopRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PoiRepository poiRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private TripService tripService;

    private User testUser;
    private Trip testTrip;
    private City testCity;
    private Region testRegion;
    private Country testCountry;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        testCountry = new Country();
        testCountry.setId(1);
        testCountry.setName("Italia");

        testRegion = new Region();
        testRegion.setId(1);
        testRegion.setName("Lazio");
        testRegion.setCountry(testCountry);

        testCity = new City();
        testCity.setId(1);
        testCity.setName("Roma");
        testCity.setLatitude(41.9028);
        testCity.setLongitude(12.4964);
        testCity.setRegion(testRegion);

        testTrip = new Trip();
        testTrip.setId(1);
        testTrip.setUser(testUser);
        testTrip.setName("Test Trip");
        testTrip.setStartDate(LocalDate.of(2024, 6, 1));
        testTrip.setEndDate(LocalDate.of(2024, 6, 10));
        testTrip.setStops(new ArrayList<>());
    }

    @Test
    void createTrip_Success() {
        TripCreateRequest request = new TripCreateRequest(
            "My Trip",
            LocalDate.of(2024, 7, 1),
            LocalDate.of(2024, 7, 10)
        );

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip trip = invocation.getArgument(0);
            trip.setId(1);
            return trip;
        });
        when(mapper.toTripDto(any(Trip.class))).thenReturn(
            new TripDto(1, "My Trip", LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 10), Collections.emptyList())
        );

        TripDto result = tripService.createTrip(request, 1);

        assertNotNull(result);
        assertEquals("My Trip", result.name());
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_InvalidDates_ThrowsBadRequest() {
        TripCreateRequest request = new TripCreateRequest(
            "My Trip",
            LocalDate.of(2024, 7, 10),
            LocalDate.of(2024, 7, 1) // End date before start date
        );

        assertThrows(BadRequestException.class, () -> tripService.createTrip(request, 1));
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addStop_Success() {
        TripStopCreateRequest request = new TripStopCreateRequest(
            "Roma",
            "Lazio",
            LocalDate.of(2024, 6, 5),
            "Visit the Colosseum"
        );

        when(tripRepository.findByNameAndUserIdIgnoreCase("Test Trip", 1)).thenReturn(Optional.of(testTrip));
        when(cityRepository.findByNameAndRegion_Name("Roma", "Lazio")).thenReturn(Optional.of(testCity));
        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);
        when(mapper.toTripStopDto(any(TripStop.class))).thenReturn(
            new TripStopDto("Test Stop", "Roma", "Lazio", LocalDate.of(2024, 6, 5), "Visit the Colosseum", Collections.emptyList())
        );

        TripStopDto result = tripService.addStop("Test Trip", request, 1);

        assertNotNull(result);
        assertEquals("Roma", result.cityName());
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void addStop_DateOutOfRange_ThrowsBadRequest() {
        TripStopCreateRequest request = new TripStopCreateRequest(
            "Roma",
            "Lazio",
            LocalDate.of(2024, 7, 15), // Outside trip dates
            "Visit"
        );

        when(tripRepository.findByNameAndUserIdIgnoreCase("Test Trip", 1)).thenReturn(Optional.of(testTrip));

        assertThrows(BadRequestException.class, () -> tripService.addStop("Test Trip", request, 1));
    }

    @Test
    void getTripById_NotFound_ThrowsException() {
        when(tripRepository.findByNameAndUserIdIgnoreCase("NonExistent", 1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tripService.getTripByName("NonExistent", 1));
    }

    @Test
    void deleteTrip_Success() {
        when(tripRepository.findByNameAndUserIdIgnoreCase("Test Trip", 1)).thenReturn(Optional.of(testTrip));
        doNothing().when(tripRepository).delete(testTrip);

        assertDoesNotThrow(() -> tripService.deleteTrip("Test Trip", 1));
        verify(tripRepository).delete(testTrip);
    }

    @Test
    void exportTripToCsv_Success() {
        TripStop stop = new TripStop();
        stop.setId(1);
        stop.setTrip(testTrip);
        stop.setCity(testCity);
        stop.setStopDate(LocalDate.of(2024, 6, 5));
        stop.setNotes("Test notes");
        stop.setPois(Collections.emptySet());
        testTrip.getStops().add(stop);

        when(tripRepository.findByNameAndUserIdIgnoreCase("Test Trip", 1)).thenReturn(Optional.of(testTrip));

        String csv = tripService.exportTripToCsv("Test Trip", 1);

        assertNotNull(csv);
        assertTrue(csv.contains("Trip Name"));
        assertTrue(csv.contains("Test Trip"));
        assertTrue(csv.contains("Roma"));
    }
}
