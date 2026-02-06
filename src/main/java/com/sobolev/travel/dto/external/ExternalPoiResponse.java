package com.sobolev.travel.dto.external;

import java.util.List;

public record ExternalPoiResponse(
    String placeId,
    String name,
    List<String> categories,
    Double lat,
    Double lon,
    String address,
    String description,
    boolean isSynthetic  // true if POI was generated synthetically
) {
    // Constructor for non-synthetic POIs
    public ExternalPoiResponse(String placeId, String name, List<String> categories,
                               Double lat, Double lon, String address, String description) {
        this(placeId, name, categories, lat, lon, address, description, false);
    }
}
