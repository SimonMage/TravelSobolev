package com.sobolev.travel.service;

import com.sobolev.travel.dto.external.*;
import com.sobolev.travel.entity.City;
import com.sobolev.travel.exception.ExternalServiceException;
import com.sobolev.travel.exception.ResourceNotFoundException;
import com.sobolev.travel.repository.CityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * Service che incapsula le chiamate verso servizi esterni come OpenWeatherMap e GeoAPIfy.
 *
 * Responsabilità:
 * - Recuperare meteo e POI per una città risolta localmente
 * - Gestire timeout ed errori delle chiamate esterne convertendoli in {@link ExternalServiceException}
 * - Fornire un fallback di POI sintetici quando GeoAPIfy non restituisce risultati
 *
 * Nota: le WebClient bean sono iniettate tramite Qualifier (configurate in `WebClientConfig`).
 */
@Service
public class ExternalApiService {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiService.class);

    private final WebClient weatherWebClient;
    private final WebClient geoapifyWebClient;
    private final CityRepository cityRepository;

    @Value("${external.weather.api-key}")
    private String weatherApiKey;

    @Value("${external.geoapify.api-key}")
    private String geoapifyApiKey;

    // GeoAPIfy POI categories for tourism
    private static final List<String> POI_CATEGORIES = List.of(
        "tourism.sights",
        "tourism.attraction",
        "entertainment.museum",
        "entertainment.culture",
        "catering.restaurant",
        "commercial.shopping_mall",
        "leisure.park",
        "building.historic"
    );

    public ExternalApiService(@Qualifier("weatherWebClient") WebClient weatherWebClient,
                              @Qualifier("geoapifyWebClient") WebClient geoapifyWebClient,
                              CityRepository cityRepository) {
        this.weatherWebClient = weatherWebClient;
        this.geoapifyWebClient = geoapifyWebClient;
        this.cityRepository = cityRepository;
    }

    /**
     * Recupera il meteo corrente per una città identificata da ID locale.
     *
     * Flusso:
     * - Risolve la città tramite {@link CityRepository}
     * - Chiama OpenWeatherMap via WebClient (timeout e onErrorResume mappano errori in ExternalServiceException)
     * - Converte la risposta grezza in {@link WeatherResponse} tramite mapWeatherResponse
     */
    public WeatherResponse getWeatherForCity(Integer cityId) {
        City city = cityRepository.findById(cityId)
            .orElseThrow(() -> new ResourceNotFoundException("City", cityId));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/weather")
                    .queryParam("lat", city.getLatitude())
                    .queryParam("lon", city.getLongitude())
                    .queryParam("appid", weatherApiKey)
                    .queryParam("units", "metric")
                    .build())
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Weather API error: {} - {}", e.getStatusCode(), e.getMessage());
                    return Mono.error(new ExternalServiceException("Weather service unavailable"));
                })
                .block();

            return mapWeatherResponse(city.getName(), response);
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching weather data", e);
            throw new ExternalServiceException("Failed to fetch weather data", e);
        }
    }

    /**
     * Recupera i POI per una città identificata da ID locale.
     * Se la chiamata esterna non restituisce risultati, viene usato un fallback sintetico.
     */
    public List<ExternalPoiResponse> getPoisForCity(Integer cityId) {
        City city = cityRepository.findById(cityId)
            .orElseThrow(() -> new ResourceNotFoundException("City", cityId));

        return getPoisForCoordinates(city.getLatitude(), city.getLongitude(), city.getName());
    }

    /**
     * Recupera i POI per coordinate specifiche.
     * Costruisce la query per GeoAPIfy usando categorie, filtro circle e bias proximity.
     * Limita la chiamata a 50 risultati e mappa la risposta in {@link ExternalPoiResponse}.
     * In caso di errori o risultato vuoto usa `generateSyntheticPois`.
     */
    public List<ExternalPoiResponse> getPoisForCoordinates(Double latitude, Double longitude, String cityName) {
        try {
            // Build categories string
            String categories = String.join(",", POI_CATEGORIES);

            log.info("Calling GeoAPIfy Places API for coordinates: lat={}, lon={}, city={}", latitude, longitude, cityName);

            GeoApifyPlacesResponse response = geoapifyWebClient.get()
                .uri(uriBuilder -> {
                    var uri = uriBuilder
                        .path("/v2/places")
                        .queryParam("categories", categories)
                        .queryParam("filter", String.format(Locale.US, "circle:%f,%f,10000", longitude, latitude))
                        .queryParam("bias", String.format(Locale.US, "proximity:%f,%f", longitude, latitude))
                        .queryParam("limit", 50)
                        .queryParam("apiKey", geoapifyApiKey)
                        .build();
                    log.info("GeoAPIfy Places API URL: {}", uri);
                    return uri;
                })
                .retrieve()
                .bodyToMono(GeoApifyPlacesResponse.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("GeoAPIfy Places API error: {} - {} - Response: {}",
                        e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString());
                    return Mono.error(new ExternalServiceException("POI service unavailable"));
                })
                .block();

            List<ExternalPoiResponse> pois = new ArrayList<>();

            if (response != null && response.features() != null && !response.features().isEmpty()) {
                for (GeoApifyPlacesResponse.Feature feature : response.features()) {
                    GeoApifyPlacesResponse.Properties props = feature.properties();
                    if (props != null && props.name() != null && !props.name().isBlank()) {
                        pois.add(new ExternalPoiResponse(
                            props.place_id(),
                            props.name(),
                            props.categories() != null ? props.categories() : Collections.emptyList(),
                            props.lat(),
                            props.lon(),
                            props.formatted(),
                            null,
                            false
                        ));
                    }
                }
            }

            // If no POIs found, generate synthetic ones
            if (pois.isEmpty()) {
                log.info("No POIs found for coordinates ({}, {}), generating synthetic POIs for {}",
                    latitude, longitude, cityName);
                pois = generateSyntheticPois(cityName, latitude, longitude);
            }

            return pois;
        } catch (ExternalServiceException e) {
            log.warn("GeoAPIfy service unavailable, returning synthetic POIs for {}: {}", cityName, e.getMessage());
            return generateSyntheticPois(cityName, latitude, longitude);
        } catch (Exception e) {
            log.error("Error fetching POI data for {}, returning synthetic POIs", cityName, e);
            // Return synthetic POIs as fallback
            return generateSyntheticPois(cityName, latitude, longitude);
        }
    }

    /**
     * Cerca città usando GeoAPIfy Geocoding API; la risposta viene mappata in {@link CitySearchResult}.
     * Usato come fallback quando la ricerca locale non produce risultati.
     */
    public List<CitySearchResult> searchCitiesExternal(String query, int limit) {
        try {
            GeoApifyGeocodingResponse response = geoapifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v1/geocode/search")
                    .queryParam("text", query)
                    .queryParam("type", "city")
                    .queryParam("limit", limit)
                    .queryParam("apiKey", geoapifyApiKey)
                    .build())
                .retrieve()
                .bodyToMono(GeoApifyGeocodingResponse.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("GeoAPIfy Geocoding API error: {} - {}", e.getStatusCode(), e.getMessage());
                    return Mono.error(new ExternalServiceException("Geocoding service unavailable"));
                })
                .block();

            if (response == null || response.results() == null) {
                return Collections.emptyList();
            }

            return response.results().stream()
                .filter(r -> r.city() != null || r.name() != null)
                .map(r -> new CitySearchResult(
                    r.place_id(),
                    r.city() != null ? r.city() : r.name(),
                    r.state() != null ? r.state() : r.county(),
                    r.country(),
                    r.lat(),
                    r.lon(),
                    null,  // No local city ID
                    Collections.emptyList(),
                    "geoapify"
                ))
                .toList();
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error searching cities via GeoAPIfy", e);
            throw new ExternalServiceException("Failed to search cities", e);
        }
    }

    /**
     * Generate synthetic POIs when the API doesn't return results
     */
    private List<ExternalPoiResponse> generateSyntheticPois(String cityName, Double lat, Double lon) {
        List<ExternalPoiResponse> syntheticPois = new ArrayList<>();

        // Generate common POI types for any city
        syntheticPois.add(new ExternalPoiResponse(
            "synthetic_museum_" + cityName.toLowerCase().replace(" ", "_"),
            "Museo Civico di " + cityName,
            List.of("entertainment.museum"),
            lat + 0.001,
            lon + 0.001,
            "Centro Storico, " + cityName,
            "Museo civico con collezioni di storia locale e arte",
            true
        ));

        syntheticPois.add(new ExternalPoiResponse(
            "synthetic_park_" + cityName.toLowerCase().replace(" ", "_"),
            "Parco Centrale di " + cityName,
            List.of("leisure.park"),
            lat + 0.002,
            lon - 0.001,
            cityName,
            "Parco pubblico con aree verdi e zone ricreative",
            true
        ));

        syntheticPois.add(new ExternalPoiResponse(
            "synthetic_cathedral_" + cityName.toLowerCase().replace(" ", "_"),
            "Cattedrale di " + cityName,
            List.of("building.historic", "tourism.sights"),
            lat - 0.001,
            lon + 0.002,
            "Piazza del Duomo, " + cityName,
            "Edificio storico religioso di grande importanza architettonica",
            true
        ));

        syntheticPois.add(new ExternalPoiResponse(
            "synthetic_theater_" + cityName.toLowerCase().replace(" ", "_"),
            "Teatro Comunale di " + cityName,
            List.of("entertainment.culture"),
            lat + 0.0015,
            lon + 0.0015,
            "Via Teatro, " + cityName,
            "Teatro storico con programmazione di prosa, musica e danza",
            true
        ));

        syntheticPois.add(new ExternalPoiResponse(
            "synthetic_piazza_" + cityName.toLowerCase().replace(" ", "_"),
            "Piazza Principale di " + cityName,
            List.of("tourism.attraction"),
            lat,
            lon,
            cityName,
            "Piazza principale con caffè, negozi e monumenti storici",
            true
        ));

        syntheticPois.add(new ExternalPoiResponse(
            "synthetic_market_" + cityName.toLowerCase().replace(" ", "_"),
            "Mercato Storico di " + cityName,
            List.of("commercial.shopping_mall"),
            lat - 0.0012,
            lon - 0.0008,
            "Via del Mercato, " + cityName,
            "Mercato tradizionale con prodotti locali e artigianato",
            true
        ));

        return syntheticPois;
    }

    @SuppressWarnings("unchecked")
    private WeatherResponse mapWeatherResponse(String cityName, Map<String, Object> response) {
        if (response == null) {
            throw new ExternalServiceException("Empty response from weather service");
        }

        Map<String, Object> main = (Map<String, Object>) response.get("main");
        Map<String, Object> wind = (Map<String, Object>) response.get("wind");
        List<Map<String, Object>> weather = (List<Map<String, Object>>) response.get("weather");

        String description = "";
        String icon = "";
        if (weather != null && !weather.isEmpty()) {
            description = (String) weather.get(0).get("description");
            icon = (String) weather.get(0).get("icon");
        }

        return new WeatherResponse(
            cityName,
            main != null ? ((Number) main.get("temp")).doubleValue() : null,
            main != null ? ((Number) main.get("feels_like")).doubleValue() : null,
            main != null ? ((Number) main.get("humidity")).intValue() : null,
            description,
            icon,
            wind != null ? ((Number) wind.get("speed")).doubleValue() : null,
            main != null ? ((Number) main.get("pressure")).intValue() : null
        );
    }
}
