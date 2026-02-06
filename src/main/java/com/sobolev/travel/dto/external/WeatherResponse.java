package com.sobolev.travel.dto.external;

public record WeatherResponse(
    String cityName,
    Double temperature,
    Double feelsLike,
    Integer humidity,
    String description,
    String icon,
    Double windSpeed,
    Integer pressure
) {}
