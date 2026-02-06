package com.sobolev.travel.dto.poi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PoiCreateRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    String name,

    String description,

    Double latitude,

    Double longitude
) {}
