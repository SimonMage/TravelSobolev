package com.sobolev.travel.dto.geography;

import java.util.List;

public record CityDto(
    Integer id,
    String name,
    Double latitude,
    Double longitude,
    Integer regionId,
    String regionName,
    String countryName,
    List<String> tags
) {}
