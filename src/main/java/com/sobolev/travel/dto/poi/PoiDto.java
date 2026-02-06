package com.sobolev.travel.dto.poi;

public record PoiDto(
    String externalId,
    String name,
    Object rawJson
) {}
