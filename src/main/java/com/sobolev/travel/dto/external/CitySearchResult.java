package com.sobolev.travel.dto.external;

import java.util.List;

/**
 * DTO for city search results combining local DB and GeoAPIfy results
 */
public record CitySearchResult(
    String placeId,
    String name,
    String region,
    String country,
    Double latitude,
    Double longitude,
    Integer localCityId,  // null if from GeoAPIfy only
    List<String> tags,    // empty if from GeoAPIfy
    String source         // "local" or "geoapify"
) {}
