package com.sobolev.travel.dto.trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TripStopCreateRequest(
    @NotBlank(message = "City name is required")
    String cityName,

    String regionName,

    @NotNull(message = "Stop date is required")
    LocalDate stopDate,

    String notes
) {}
