package com.sobolev.travel.dto.trip;

import java.time.LocalDate;
import java.util.List;

public record TripDto(
    Integer id,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    List<TripStopDto> stops
) {}
