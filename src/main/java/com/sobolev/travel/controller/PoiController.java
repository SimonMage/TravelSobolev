package com.sobolev.travel.controller;

import com.sobolev.travel.dto.poi.PoiCreateRequest;
import com.sobolev.travel.dto.poi.PoiDto;
import com.sobolev.travel.security.CustomUserDetails;
import com.sobolev.travel.service.PoiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per le operazioni sui POI personalizzati dell'utente.
 * Endpoint protetti che richiedono autenticazione (bearer JWT).
 */
@RestController
@RequestMapping("/api/pois")
@Tag(name = "User POIs", description = "User custom Points of Interest endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PoiController {

    private final PoiService poiService;

    public PoiController(PoiService poiService) {
        this.poiService = poiService;
    }

    @PostMapping
    @Operation(summary = "Create a new custom POI")
    public ResponseEntity<PoiDto> createPoi(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PoiCreateRequest request) {
        PoiDto created = poiService.createPoi(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all user's custom POIs")
    public ResponseEntity<List<PoiDto>> getUserPois(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(poiService.getUserPois(userDetails.getId()));
    }

    @GetMapping("/{poiId}")
    @Operation(summary = "Get a specific POI by ID")
    public ResponseEntity<PoiDto> getPoiById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer poiId) {
        return ResponseEntity.ok(poiService.getPoiById(userDetails.getId(), poiId));
    }

    @DeleteMapping("/{poiId}")
    @Operation(summary = "Delete a specific POI")
    public ResponseEntity<Void> deletePoi(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer poiId) {
        poiService.deletePoi(userDetails.getId(), poiId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Delete all user's POIs")
    public ResponseEntity<Void> deleteAllUserPois(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        poiService.deleteAllUserPois(userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}

