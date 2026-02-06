package com.sobolev.travel.dto.trip;

import java.time.LocalDate;

public record TripStopDto(
    String stopName,
    String cityName,
    String regionName,
    LocalDate stopDate,
    String notes
) {}
