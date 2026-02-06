package com.sobolev.travel.config;

import com.sobolev.travel.security.JwtAuthenticationEntryPoint;
import com.sobolev.travel.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.http.HttpMethod;

/**
 * Configurazione di sicurezza dell'applicazione.
 *
 * Questa classe configura la SecurityFilterChain per:
 * - Disabilitare CSRF (API stateless)
 * - Abilitare CORS con sorgente personalizzata
 * - Gestire le eccezioni di autenticazione tramite JwtAuthenticationEntryPoint
 * - Impostare la sessione come STATELESS (JWT basato su token, non sessione server)
 * - Definire i percorsi pubblici e quelli protetti
 * - Aggiungere il filtro JWT prima del filtro standard di autenticazione di Spring
 *
 * Perché è fatto così:
 * - L'app usa JWT per autenticazione; nessuna sessione server quindi state-less.
 * - Alcuni endpoint (auth, lettura dati geografici, swagger, actuator) devono rimanere pubblici
 *   per permettere la registrazione, la consultazione e l'health check.
 * - Il filtro `JwtAuthenticationFilter` controlla la presenza e la validità del token
 *   per proteggere gli endpoint che richiedono autenticazione.
 *
 * Come viene usata:
 * - I controller non si occupano di autenticazione: se la richiesta ha un JWT valido
 *   il SecurityContext viene popolato e i controller possono ottenere l'utente autenticato.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Componenti di sicurezza iniettati tramite costruttore
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationEntryPoint authenticationEntryPoint,
                         JwtAuthenticationFilter jwtAuthenticationFilter,
                         CorsConfigurationSource corsConfigurationSource) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Definisce la catena di filtri di sicurezza.
     *
     * - Disabilita CSRF perché l'API è stateless (non usa cookie di sessione).
     * - Abilita CORS con la sorgente configurata in `CorsConfig`.
     * - Imposta la gestione delle eccezioni di autenticazione.
     * - Permette liberamente alcune rotte come `/api/auth/**`, risorse pubbliche e swagger.
     * - Richiede autenticazione per tutte le altre rotte.
     * - Aggiunge `JwtAuthenticationFilter` prima del filtro standard UsernamePassword.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/countries/**").permitAll()
                .requestMatchers("/api/regions/**").permitAll()
                .requestMatchers("/api/cities/**").permitAll()
                .requestMatchers("/api/tags/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean per l'encoding delle password. Usato in `AuthService` per codificare e verificare
     * le password degli utenti.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Espone l'AuthenticationManager come bean, necessario per l'autenticazione esplicita
     * in alcuni scenari (es. endpoint di login che verificano credenziali).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
