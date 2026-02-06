package com.sobolev.travel.dto.user;

import java.time.LocalDateTime;

public record UserDto(
    Integer id,
    LocalDateTime createdAt,
    UserProfileDto profile
) {}
