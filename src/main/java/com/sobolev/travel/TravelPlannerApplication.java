package com.sobolev.travel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale dell'applicazione Spring Boot.
 * <p>
 * Questo file è il punto di ingresso dell'applicazione e viene rilevato da Spring Boot
 * all'avvio. L'annotazione @SpringBootApplication abilita l'auto-configurazione,
 * la scansione dei componenti e altre convenzioni di Spring Boot.
 *
 * Utilizzo:
 * - Viene invocata la JVM con questa classe per avviare il contesto Spring.
 * - I controller, servizi, repository e componenti presenti nei package sottostanti
 *   vengono registrati automaticamente grazie alla scansione dei componenti.
 *
 * Nota su come viene usata da altre parti del codice:
 * - Non ci sono riferimenti diretti a questa classe all'interno del codice applicativo;
 *   è usata soltanto come punto di avvio dall'ambiente di esecuzione (mvn spring-boot:run
 *   o java -jar target/...).
 */
@SpringBootApplication
public class TravelPlannerApplication {

    /**
     * Metodo main che avvia l'applicazione Spring Boot.
     *
     * L'applicazione viene avviata chiamando SpringApplication.run(...) che crea il
     * contesto dell'applicazione, inizializza i bean e applica le migrazioni DB (se configurate)
     * prima di rendere disponibili gli endpoint HTTP.
     *
     * Esempi di avvio:
     * - mvn spring-boot:run
     * - java -jar target/travelplanner.jar
     */
    public static void main(String[] args) {
        SpringApplication.run(TravelPlannerApplication.class, args);
    }
}
