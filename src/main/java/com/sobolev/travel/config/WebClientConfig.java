package com.sobolev.travel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configurazione dei WebClient utilizzati per chiamare servizi HTTP esterni.
 *
 * Questa classe definisce due bean `WebClient` con baseUrl configurata tramite
 * properties: uno per il servizio meteo esterno e uno per GeoApify (luoghi/geocoding).
 * I bean vengono iniettati nei servizi che richiedono chiamate HTTP esterne.
 *
 * Come viene usata:
 * - `ExternalApiService` e altri servizi consumano questi bean tramite @Autowired o
 *   costruttore per effettuare richieste HTTP.
 */
@Configuration
public class WebClientConfig {

    // Base URL per il servizio meteo, caricato da application.yml (property: external.weather.base-url)
    @Value("${external.weather.base-url}")
    private String weatherBaseUrl;

    // Base URL per GeoApify (luoghi e geocoding), caricato da application.yml (property: external.geoapify.base-url)
    @Value("${external.geoapify.base-url}")
    private String geoapifyBaseUrl;

    /**
     * Bean WebClient configurato per il servizio meteo.
     *
     * Default headers e baseUrl vengono impostati qui per mantenere la configurazione
     * centralizzata e riusabile. I servizi che consumano l'API meteo possono semplicemente
     * iniettare questo bean per eseguire richieste verso l'endpoint esterno.
     */
    @Bean
    public WebClient weatherWebClient() {
        return WebClient.builder()
            .baseUrl(weatherBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    /**
     * Bean WebClient configurato per GeoApify.
     *
     * Mantiene la stessa impostazione di default header. Avere bean distinti permette
     * di configurare timeout o filtri diversi se necessario per ogni provider esterno.
     */
    @Bean
    public WebClient geoapifyWebClient() {
        return WebClient.builder()
            .baseUrl(geoapifyBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
