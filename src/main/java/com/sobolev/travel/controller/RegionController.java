package com.sobolev.travel.controller;

import com.sobolev.travel.dto.geography.RegionDto;
import com.sobolev.travel.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per le operazioni sulle regioni.
 */
@RestController
@RequestMapping("/api/regions")
@Tag(name = "Geography", description = "Geography data endpoints")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    /**
     * Recupera tutte le regioni o filtra per paese (nome).
     */
    @GetMapping
    @Operation(summary = "Get all regions or filter by country")
    public ResponseEntity<List<RegionDto>> getRegions(
            @RequestParam(required = false) String countryName) {
        if (countryName != null && !countryName.isBlank()) {
            return ResponseEntity.ok(regionService.getRegionsByCountryName(countryName));
        }
        return ResponseEntity.ok(regionService.getAllRegions());
    }

    /**
     * Recupera una regione per nome, opzionalmente limitando la ricerca a un paese.
     */
    @GetMapping("/{regionName}")
    @Operation(summary = "Get region by name (optionally filter by country)")
    public ResponseEntity<RegionDto> getRegionByName(
            @PathVariable String regionName,
            @RequestParam(required = false) String country) {
        return ResponseEntity.ok(regionService.getRegionByName(regionName, country));
    }
}
