package com.sobolev.travel.controller;

import com.sobolev.travel.dto.user.UserDto;
import com.sobolev.travel.dto.user.UserProfileDto;
import com.sobolev.travel.security.CustomUserDetails;
import com.sobolev.travel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller per operazioni utente (profilo corrente, aggiornamento profilo).
 * Endpoint protetti che richiedono autenticazione.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserDto user = userService.getCurrentUser(userDetails.getId());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserProfileDto profileDto) {
        UserDto user = userService.updateProfile(userDetails.getId(), profileDto);
        return ResponseEntity.ok(user);
    }
}
