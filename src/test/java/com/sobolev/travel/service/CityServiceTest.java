package com.sobolev.travel.service;

import com.sobolev.travel.dto.geography.CityDto;
import com.sobolev.travel.entity.*;
import com.sobolev.travel.exception.ResourceNotFoundException;
import com.sobolev.travel.mapper.EntityMapper;
import com.sobolev.travel.repository.CityRepository;
import com.sobolev.travel.repository.SearchHistoryRepository;
import com.sobolev.travel.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private ExternalApiService externalApiService;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private CityService cityService;

    private City testCity;
    private CityDto testCityDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        Country country = new Country();
        country.setId(1);
        country.setName("Italia");

        Region region = new Region();
        region.setId(1);
        region.setName("Lazio");
        region.setCountry(country);

        testCity = new City();
        testCity.setId(1);
        testCity.setName("Roma");
        testCity.setLatitude(41.9028);
        testCity.setLongitude(12.4964);
        testCity.setRegion(region);

        testCityDto = new CityDto(1, "Roma", 41.9028, 12.4964, 1, "Lazio", "Italia", List.of("arte", "storia"));

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
    }

    @Test
    void getAllCities_ReturnsAllCities() {
        when(cityRepository.findAll()).thenReturn(List.of(testCity));
        when(mapper.toCityDto(testCity)).thenReturn(testCityDto);

        List<CityDto> result = cityService.getAllCities();

        assertEquals(1, result.size());
        assertEquals("Roma", result.get(0).name());
    }

    @Test
    void getCityById_SavesSearchHistory_WhenUserAuthenticated() {
        when(cityRepository.findByIdWithTags(1)).thenReturn(Optional.of(testCity));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(searchHistoryRepository.save(any(SearchHistory.class))).thenReturn(new SearchHistory());
        when(mapper.toCityDto(testCity)).thenReturn(testCityDto);

        CityDto result = cityService.getCityById(1, 1);

        assertNotNull(result);
        assertEquals("Roma", result.name());
        verify(searchHistoryRepository).save(any(SearchHistory.class));
    }

    @Test
    void getCityById_DoesNotSaveSearchHistory_WhenUserNotAuthenticated() {
        when(cityRepository.findByIdWithTags(1)).thenReturn(Optional.of(testCity));
        when(mapper.toCityDto(testCity)).thenReturn(testCityDto);

        CityDto result = cityService.getCityById(1, null);

        assertNotNull(result);
        verify(searchHistoryRepository, never()).save(any(SearchHistory.class));
    }

    @Test
    void getCityById_NotFound_ThrowsException() {
        when(cityRepository.findByIdWithTags(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cityService.getCityById(99, null));
    }

    @Test
    void searchCities_ReturnsMatchingCities() {
        when(cityRepository.findByNameContainingIgnoreCase("rom")).thenReturn(List.of(testCity));
        when(mapper.toCityDto(testCity)).thenReturn(testCityDto);

        List<CityDto> result = cityService.searchCities("rom");

        assertEquals(1, result.size());
        assertEquals("Roma", result.get(0).name());
    }

    @Test
    void getCitiesByTags_ReturnsMatchingCities() {
        when(cityRepository.findByTagNames(List.of("arte"))).thenReturn(List.of(testCity));
        when(mapper.toCityDto(testCity)).thenReturn(testCityDto);

        List<CityDto> result = cityService.getCitiesByTags(List.of("arte"));

        assertEquals(1, result.size());
        assertTrue(result.get(0).tags().contains("arte"));
    }
}
