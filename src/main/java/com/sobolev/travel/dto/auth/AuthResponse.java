package com.sobolev.travel.dto.auth;

public record AuthResponse(
    String token,
    String username,
    String email
) {}
