package com.sobolev.travel.controller;

import com.sobolev.travel.dto.external.CitySearchResult;
import com.sobolev.travel.dto.external.ExternalPoiResponse;
import com.sobolev.travel.dto.external.WeatherResponse;
import com.sobolev.travel.dto.geography.CityDto;
import com.sobolev.travel.security.CustomUserDetails;
import com.sobolev.travel.service.CityService;
import com.sobolev.travel.service.ExternalApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per le operazioni geografiche relative alle città.
 *
 * Responsabilità:
 * - Espone endpoint per ottenere città, cercare città, ottenere meteo e POI.
 * - Orchestrates calls between HTTP layer and services (`CityService` e `ExternalApiService`).
 *
 * Nota sull'autenticazione:
 * - Alcuni endpoint salvano la ricerca nella cronologia se la richiesta è autenticata;
 *   in questo caso il metodo riceve il principal `CustomUserDetails` tramite @AuthenticationPrincipal.
 */
@RestController
@RequestMapping("/api/cities")
@Tag(name = "Geography", description = "Geography data endpoints")
public class CityController {

    // Service di dominio per operazioni CRUD e ricerche locali sulle città
    private final CityService cityService;
    // Service per chiamate ad API esterne (meteo, POI, geocoding)
    private final ExternalApiService externalApiService;

    /**
     * Costruttore iniettato con i servizi necessari.
     */
    public CityController(CityService cityService, ExternalApiService externalApiService) {
        this.cityService = cityService;
        this.externalApiService = externalApiService;
    }

    /**
     * Recupera tutte le città oppure le filtra per regione o tag.
     * - Se `regionName` è fornito, ritorna città di quella regione.
     * - Se `tags` è fornito, ritorna città che possiedono quei tag.
     * - Altrimenti ritorna tutte le città.
     *
     * @param regionName nome della regione (opzionale)
     * @param tags lista di tag (opzionale)
     * @return lista di {@link CityDto}
     */
    @GetMapping
    @Operation(summary = "Get all cities or filter by region/tags")
    public ResponseEntity<List<CityDto>> getCities(
            @RequestParam(required = false) String regionName,
            @RequestParam(required = false) List<String> tags) {

        if (regionName != null && !regionName.isBlank()) {
            return ResponseEntity.ok(cityService.getCitiesByRegionName(regionName));
        }
        if (tags != null && !tags.isEmpty()) {
            return ResponseEntity.ok(cityService.getCitiesByTags(tags));
        }
        return ResponseEntity.ok(cityService.getAllCities());
    }

    /**
     * Cerca città per nome. Effettua prima una ricerca nel DB locale e, se niente viene
     * trovato, interroga GeoAPIfy tramite `CityService`/`ExternalApiService`.
     * Se la richiesta è autenticata, salva la ricerca nella cronologia usando l'ID utente.
     *
     * @param query testo di ricerca
     * @param userDetails principal opzionale (popolato quando autenticato)
     * @return lista di risultati di ricerca esterni o locali {@link CitySearchResult}
     */
    @GetMapping("/search")
    @Operation(summary = "Search cities by name (local + GeoAPIfy)")
    public ResponseEntity<List<CitySearchResult>> searchCities(
            @RequestParam String query,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(cityService.searchCities(query, userId));
    }

    /**
     * Recupera una città per nome (opzionalmente limitando la ricerca a una regione specifica).
     * Se la richiesta è autenticata, la ricerca viene salvata nella cronologia.
     *
     * @param cityName nome della città (path variable)
     * @param region nome della regione (query param opzionale)
     * @param userDetails principal opzionale per salvare la history
     * @return {@link CityDto} della città trovata
     */
    @GetMapping("/{cityName}")
    @Operation(summary = "Get city by name (saves to search history if authenticated)")
    public ResponseEntity<CityDto> getCityByName(
            @PathVariable String cityName,
            @RequestParam(required = false) String region,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(cityService.getCityByName(cityName, region, userId));
    }

    /**
     * Recupera i dati meteo per una città usando OpenWeatherMap (tramite `ExternalApiService`).
     * Nota: questo endpoint risolve la città localmente e poi chiama il servizio esterno.
     *
     * @param cityName nome della città (path variable)
     * @param region nome della regione (opzionale)
     * @return {@link WeatherResponse} con i dati meteo correnti
     */
    @GetMapping("/{cityName}/weather")
    @Operation(summary = "Get weather for a city by name (OpenWeatherMap)")
    public ResponseEntity<WeatherResponse> getCityWeather(
            @PathVariable String cityName,
            @RequestParam(required = false) String region) {
        CityDto city = cityService.getCityByName(cityName, region, null);
        return ResponseEntity.ok(externalApiService.getWeatherForCity(city.id()));
    }

    /**
     * Recupera i punti di interesse per una città usando GeoAPIfy.
     * Se GeoAPIfy non ritorna risultati, il servizio restituisce POI sintetici come fallback.
     *
     * @param cityName nome della città
     * @param region nome della regione (opzionale)
     * @return lista di {@link ExternalPoiResponse}
     */
    @GetMapping("/{cityName}/pois")
    @Operation(summary = "Get points of interest for a city by name (GeoAPIfy)")
    public ResponseEntity<List<ExternalPoiResponse>> getCityPois(
            @PathVariable String cityName,
            @RequestParam(required = false) String region) {
        CityDto city = cityService.getCityByName(cityName, region, null);
        return ResponseEntity.ok(externalApiService.getPoisForCity(city.id()));
    }

    /**
     * Filtra città per tag. Endpoint dedicato per chiamate che richiedono solo il filtro per tag.
     *
     * @param tags lista di tag da applicare come filtro
     * @return lista di {@link CityDto} che corrispondono ai tag richiesti
     */
    @GetMapping("/filter")
    @Operation(summary = "Filter cities by tags")
    public ResponseEntity<List<CityDto>> filterCitiesByTags(
            @RequestParam List<String> tags) {
        return ResponseEntity.ok(cityService.getCitiesByTags(tags));
    }
}
