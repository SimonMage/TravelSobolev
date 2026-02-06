package com.sobolev.travel.dto.external;

import java.util.List;

/**
 * Response from GeoAPIfy Geocoding API for city search
 */
public record GeoApifyGeocodingResponse(
    List<Result> results
) {
    public record Result(
        String place_id,
        String country,
        String country_code,
        String state,
        String county,
        String city,
        String postcode,
        String district,
        String suburb,
        String street,
        String housenumber,
        Double lon,
        Double lat,
        String result_type,
        String formatted,
        String address_line1,
        String address_line2,
        String category,
        Timezone timezone,
        Datasource datasource,
        String name,
        Rank rank,
        Bbox bbox
    ) {}

    public record Timezone(
        String name,
        String offset_STD,
        Integer offset_STD_seconds,
        String offset_DST,
        Integer offset_DST_seconds,
        String abbreviation_STD,
        String abbreviation_DST
    ) {}

    public record Datasource(
        String sourcename,
        String attribution,
        String license,
        String url
    ) {}

    public record Rank(
        Double importance,
        Double popularity,
        Double confidence,
        String match_type
    ) {}

    public record Bbox(
        Double lon1,
        Double lat1,
        Double lon2,
        Double lat2
    ) {}
}
