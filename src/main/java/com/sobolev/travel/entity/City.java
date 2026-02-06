package com.sobolev.travel.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entità JPA che rappresenta una città.
 *
 * Questa classe mappa la tabella `city` nel database e contiene le informazioni
 * geografiche e relazionali utili all'app (nome, coordinate e tag associati).
 *
 * Come viene usata:
 * - `CityRepository` esegue query su questa entità.
 * - `CityService` e `CityController` la trasformano in DTO (`CityDto`) per le risposte API.
 */
@Entity
@Table(name = "city")
public class City {

    /**
     * Identificatore numerico della città (PK, auto increment).
     * Usato per riferimenti nelle relazioni e per l'API (id city).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Relazione ManyToOne con `Region`.
     * FetchType.LAZY evita il caricamento della regione finché non serve.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    /**
     * Nome della città (es. "Rome").
     */
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Coordinate geografiche (latitude) - usate per integrazioni con servizi esterni
     * come meteo o geocoding.
     */
    @Column(nullable = false)
    private Double latitude;

    /**
     * Coordinate geografiche (longitude).
     */
    @Column(nullable = false)
    private Double longitude;

    /**
     * Relazione ManyToMany con `Tag` (categorie della città), tramite tabella `city_tag`.
     * Viene inizializzata con HashSet per evitare NullPointerException e per operazioni di set.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "city_tag",
        joinColumns = @JoinColumn(name = "city_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
