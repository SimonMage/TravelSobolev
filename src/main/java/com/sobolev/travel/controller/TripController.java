package com.sobolev.travel.controller;

import com.sobolev.travel.dto.poi.PoiCreateRequest;
import com.sobolev.travel.dto.poi.PoiDto;
import com.sobolev.travel.dto.trip.*;
import com.sobolev.travel.security.CustomUserDetails;
import com.sobolev.travel.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per la gestione dei viaggi (Trip) e delle relative risorse (stops, POI).
 *
 * Tutti gli endpoint di mutazione richiedono autenticazione (bearer JWT).
 */
@RestController
@RequestMapping("/api/trips")
@Tag(name = "Trips", description = "Trip management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    @Operation(summary = "Get all trips for current user")
    public ResponseEntity<List<TripDto>> getUserTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Recupera tutti i viaggi dell'utente attualmente autenticato
        return ResponseEntity.ok(tripService.getUserTrips(userDetails.getId()));
    }

    @GetMapping("/{tripName}")
    @Operation(summary = "Get trip by name")
    public ResponseEntity<TripDto> getTripByName(
            @PathVariable String tripName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Recupera un viaggio specifico per nome
        return ResponseEntity.ok(tripService.getTripByName(tripName, userDetails.getId()));
    }

    @PostMapping
    @Operation(summary = "Create a new trip")
    public ResponseEntity<TripDto> createTrip(
            @Valid @RequestBody TripCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Crea un nuovo viaggio
        TripDto trip = tripService.createTrip(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

    @PutMapping("/{tripName}")
    @Operation(summary = "Update a trip by name")
    public ResponseEntity<TripDto> updateTrip(
            @PathVariable String tripName,
            @Valid @RequestBody TripUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Aggiorna un viaggio esistente
        return ResponseEntity.ok(tripService.updateTrip(tripName, request, userDetails.getId()));
    }

    @DeleteMapping("/{tripName}")
    @Operation(summary = "Delete a trip by name")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable String tripName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Elimina un viaggio
        tripService.deleteTrip(tripName, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tripName}/export")
    @Operation(summary = "Export trip to CSV by name")
    public ResponseEntity<String> exportTripToCsv(
            @PathVariable String tripName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Esporta un viaggio in formato CSV
        String csv = tripService.exportTripToCsv(tripName, userDetails.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "trip_" + tripName + ".csv");

        return ResponseEntity.ok()
            .headers(headers)
            .body(csv);
    }

    // Trip Stop endpoints
    @PostMapping("/{tripName}/stops")
    @Operation(summary = "Add a stop to a trip by trip name")
    public ResponseEntity<TripStopDto> addStop(
            @PathVariable String tripName,
            @Valid @RequestBody TripStopCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Aggiunge una tappa a un viaggio
        TripStopDto stop = tripService.addStop(tripName, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(stop);
    }

    @PutMapping("/{tripName}/stops/{stopName}")
    @Operation(summary = "Update a trip stop by trip name and stop name")
    public ResponseEntity<TripStopDto> updateStop(
            @PathVariable String tripName,
            @PathVariable String stopName,
            @Valid @RequestBody TripStopUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Aggiorna una tappa esistente di un viaggio
        return ResponseEntity.ok(tripService.updateStop(tripName, stopName, request, userDetails.getId()));
    }

    @DeleteMapping("/{tripName}/stops/{stopName}")
    @Operation(summary = "Delete a trip stop by trip name and stop name")
    public ResponseEntity<Void> deleteStop(
            @PathVariable String tripName,
            @PathVariable String stopName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Elimina una tappa da un viaggio
        tripService.deleteStop(tripName, stopName, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
