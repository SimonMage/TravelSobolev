package com.sobolev.travel.dto.geography;

public record RegionDto(
    Integer id,
    String name,
    Integer countryId,
    String countryName
) {}
