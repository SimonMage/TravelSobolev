package com.sobolev.travel.controller;

import com.sobolev.travel.dto.geography.CountryDto;
import com.sobolev.travel.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per le operazioni sui paesi.
 */
@RestController
@RequestMapping("/api/countries")
@Tag(name = "Geography", description = "Geography data endpoints")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    /**
     * Restituisce tutti i paesi.
     *
     * @return lista di tutti i paesi
     */
    @GetMapping
    @Operation(summary = "Get all countries")
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }

    /**
     * Restituisce un paese in base al nome.
     *
     * @param name il nome del paese da cercare
     * @return il paese corrispondente al nome fornito
     */
    @GetMapping("/{name}")
    @Operation(summary = "Get country by name")
    public ResponseEntity<CountryDto> getCountryByName(@PathVariable String name) {
        return ResponseEntity.ok(countryService.getCountryByName(name));
    }
}
