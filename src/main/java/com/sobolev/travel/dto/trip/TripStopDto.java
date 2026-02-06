package com.sobolev.travel.dto.trip;

import com.sobolev.travel.dto.poi.PoiDto;

import java.time.LocalDate;
import java.util.List;

public record TripStopDto(
    String stopName,
    String cityName,
    String regionName,
    LocalDate stopDate,
    String notes,
    List<PoiDto> pois
) {}
