package com.sobolev.travel.dto.external;

import java.util.List;

/**
 * Response from GeoAPIfy Places API
 */
public record GeoApifyPlacesResponse(
    String type,
    List<Feature> features
) {
    public record Feature(
        String type,
        Properties properties,
        Geometry geometry
    ) {}

    public record Properties(
        String place_id,
        String name,
        String country,
        String country_code,
        String state,
        String city,
        String postcode,
        String district,
        String suburb,
        String street,
        String housenumber,
        Double lon,
        Double lat,
        String formatted,
        String address_line1,
        String address_line2,
        List<String> categories,
        Datasource datasource
    ) {}

    public record Datasource(
        String sourcename,
        String attribution,
        String license,
        String url
    ) {}

    public record Geometry(
        String type,
        List<Double> coordinates
    ) {}
}
