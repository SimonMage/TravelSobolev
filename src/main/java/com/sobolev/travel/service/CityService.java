package com.sobolev.travel.service;

import com.sobolev.travel.dto.external.CitySearchResult;
import com.sobolev.travel.dto.geography.CityDto;
import com.sobolev.travel.entity.City;
import com.sobolev.travel.entity.SearchHistory;
import com.sobolev.travel.entity.Tag;
import com.sobolev.travel.entity.User;
import com.sobolev.travel.exception.ResourceNotFoundException;
import com.sobolev.travel.mapper.EntityMapper;
import com.sobolev.travel.repository.CityRepository;
import com.sobolev.travel.repository.SearchHistoryRepository;
import com.sobolev.travel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CityService {

    private static final Logger log = LoggerFactory.getLogger(CityService.class);

    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final ExternalApiService externalApiService;
    private final EntityMapper mapper;

    public CityService(CityRepository cityRepository,
                       UserRepository userRepository,
                       SearchHistoryRepository searchHistoryRepository,
                       ExternalApiService externalApiService,
                       EntityMapper mapper) {
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.externalApiService = externalApiService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CityDto> getAllCities() {
        return cityRepository.findAll().stream()
            .map(mapper::toCityDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CityDto> getCitiesByRegion(Integer regionId) {
        return cityRepository.findByRegionId(regionId).stream()
            .map(mapper::toCityDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CityDto> getCitiesByRegionName(String regionName) {
        // Find all regions with this name and get cities from all of them
        // Note: region names might not be unique across countries
        List<City> cities = cityRepository.findAll().stream()
            .filter(city -> city.getRegion().getName().equalsIgnoreCase(regionName))
            .toList();
        return cities.stream()
            .map(mapper::toCityDto)
            .toList();
    }

    /**
     * FR-2.1: Search cities by name.
     * First searches local DB, then queries GeoAPIfy if no local results found.
     * FR-2.2: Saves search to history if user is authenticated.
     */
    @Transactional
    public List<CitySearchResult> searchCities(String name, Integer userId) {
        List<CitySearchResult> results = new ArrayList<>();

        // First, search local database
        List<City> localCities = cityRepository.findByNameContainingIgnoreCase(name);

        for (City city : localCities) {
            List<String> tags = city.getTags() != null
                ? city.getTags().stream().map(Tag::getName).toList()
                : Collections.emptyList();

            results.add(new CitySearchResult(
                "local_" + city.getId(),
                city.getName(),
                city.getRegion().getName(),
                city.getRegion().getCountry().getName(),
                city.getLatitude(),
                city.getLongitude(),
                city.getId(),
                tags,
                "local"
            ));
        }

        // If no local results, search via GeoAPIfy
        if (results.isEmpty()) {
            try {
                List<CitySearchResult> externalResults = externalApiService.searchCitiesExternal(name, 10);
                results.addAll(externalResults);
            } catch (Exception e) {
                log.warn("Failed to search cities via GeoAPIfy: {}", e.getMessage());
            }
        }

        // FR-2.2: Save search history
        if (userId != null) {
            saveSearchHistory(userId, name, localCities.isEmpty() ? null : localCities.get(0).getId());
        }

        return results;
    }

    /**
     * Legacy method for backward compatibility - returns CityDto
     */
    @Transactional(readOnly = true)
    public List<CityDto> searchCities(String name) {
        return cityRepository.findByNameContainingIgnoreCase(name).stream()
            .map(mapper::toCityDto)
            .toList();
    }

    /**
     * Get city by ID and optionally save to search history
     */
    @Transactional
    public CityDto getCityById(Integer id, Integer userId) {
        City city = cityRepository.findByIdWithTags(id)
            .orElseThrow(() -> new ResourceNotFoundException("City", id));

        // Save search history if user is authenticated
        if (userId != null) {
            saveSearchHistory(userId, city.getName(), city.getId());
        }

        return mapper.toCityDto(city);
    }

    /**
     * Get city by name and optionally region name. If multiple cities match, returns the first one.
     * Optionally saves to search history if user is authenticated.
     */
    @Transactional
    public CityDto getCityByName(String cityName, String regionName, Integer userId) {
        City city;

        if (regionName != null && !regionName.isBlank()) {
            // Search by city name and region name
            city = cityRepository.findByCityNameAndRegionNameIgnoreCase(cityName, regionName)
                .orElseThrow(() -> new ResourceNotFoundException("City '" + cityName + "' in region '" + regionName + "' not found"));
        } else {
            // Search by city name only (may return multiple, take first)
            List<City> cities = cityRepository.findByNameIgnoreCaseWithDetails(cityName);
            if (cities.isEmpty()) {
                throw new ResourceNotFoundException("City '" + cityName + "' not found");
            }
            city = cities.get(0);
        }

        // Save search history if user is authenticated
        if (userId != null) {
            saveSearchHistory(userId, city.getName(), city.getId());
        }

        return mapper.toCityDto(city);
    }

    /**
     * FR-2.5: Filter cities by tags
     */
    @Transactional(readOnly = true)
    public List<CityDto> getCitiesByTags(List<String> tagNames) {
        return cityRepository.findByTagNames(tagNames).stream()
            .map(mapper::toCityDto)
            .toList();
    }

    /**
     * Save search to history
     */
    private void saveSearchHistory(Integer userId, String query, Integer cityId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUser(user);
            searchHistory.setQuery(query);
            if (cityId != null) {
                City city = cityRepository.findById(cityId).orElse(null);
                searchHistory.setCity(city);
            }
            searchHistoryRepository.save(searchHistory);
        }
    }
}
