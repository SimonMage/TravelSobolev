package com.sobolev.travel.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entità JPA che rappresenta un Point Of Interest (POI) esterno.
 *
 * Contiene un `externalId` usato per mappare la risorsa esterna e `rawJson` per
 * conservare la risposta grezza del provider esterno (colonna jsonb in Postgres).
 * La mappatura ManyToMany con `TripStop` permette riutilizzo dei POI in più viaggi.
 */
@Entity
@Table(name = "poi")
public class Poi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "external_id", length = 100, nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(name = "raw_json", columnDefinition = "jsonb", nullable = false)
    private String rawJson;

    @ManyToMany(mappedBy = "pois", fetch = FetchType.LAZY)
    private Set<TripStop> tripStops = new HashSet<>();

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public Set<TripStop> getTripStops() {
        return tripStops;
    }

    public void setTripStops(Set<TripStop> tripStops) {
        this.tripStops = tripStops;
    }
}
