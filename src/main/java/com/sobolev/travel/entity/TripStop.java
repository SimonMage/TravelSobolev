package com.sobolev.travel.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entità che rappresenta una tappa di un viaggio (TripStop).
 *
 * Contiene il riferimento al Trip, alla City, la data della tappa, note e i POI associati.
 */
@Entity
@Table(name = "trip_stop")
public class TripStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "stop_date", nullable = false)
    private LocalDate stopDate;

    @Column(name = "stop_name")
    private String stopName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "trip_stop_poi",
        joinColumns = @JoinColumn(name = "trip_stop_id"),
        inverseJoinColumns = @JoinColumn(name = "poi_id")
    )
    private Set<Poi> pois = new HashSet<>();

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public LocalDate getStopDate() {
        return stopDate;
    }

    public void setStopDate(LocalDate stopDate) {
        this.stopDate = stopDate;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Set<Poi> getPois() {
        return pois;
    }

    public void setPois(Set<Poi> pois) {
        this.pois = pois;
    }

    /**
     * Aggiunge un POI alla tappa e aggiorna la collezione bidirezionale sul POI.
     * Usiamo cacade persist/merge per mantenere i POI sincronizzati.
     */
    public void addPoi(Poi poi) {
        pois.add(poi);
        poi.getTripStops().add(this);
    }

    /**
     * Rimuove un POI dalla tappa e aggiorna la collezione bidirezionale sul POI.
     */
    public void removePoi(Poi poi) {
        pois.remove(poi);
        poi.getTripStops().remove(this);
    }
}
