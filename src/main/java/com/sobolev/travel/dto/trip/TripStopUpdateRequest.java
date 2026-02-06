package com.sobolev.travel.dto.trip;

import java.time.LocalDate;

public record TripStopUpdateRequest(
    LocalDate stopDate,
    String notes
) {}
