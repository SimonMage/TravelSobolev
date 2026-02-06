package com.sobolev.travel.dto.trip;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TripUpdateRequest(
    @Size(max = 150, message = "Trip name must not exceed 150 characters")
    String name,

    LocalDate startDate,

    LocalDate endDate
) {}
