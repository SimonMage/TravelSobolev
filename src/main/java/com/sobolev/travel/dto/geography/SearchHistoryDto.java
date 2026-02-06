package com.sobolev.travel.dto.geography;

import java.time.LocalDateTime;

public record SearchHistoryDto(
    Integer id,
    String query,
    Integer cityId,
    String cityName,
    LocalDateTime searchedAt
) {}
