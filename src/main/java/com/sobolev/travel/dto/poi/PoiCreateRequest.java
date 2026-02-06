package com.sobolev.travel.dto.poi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PoiCreateRequest(
    @NotBlank(message = "External ID is required")
    String externalId,

    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "Raw JSON is required")
    Object rawJson
) {}
