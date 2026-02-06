package com.sobolev.travel.dto.poi;

import java.time.LocalDateTime;

public record PoiDto(
    Integer id,
    String name,
    String description,
    Double latitude,
    Double longitude,
    LocalDateTime createdAt
) {}
