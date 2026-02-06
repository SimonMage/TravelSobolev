package com.sobolev.travel.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entità JPA che rappresenta un viaggio (Trip) pianificato da un utente.
 *
 * Contiene informazioni base come nome, date di inizio/fine e la lista di tappe (TripStop).
 * Le tappe sono ordinate per data di stop grazie a @OrderBy.
 */
@Entity
@Table(name = "trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopDate ASC")
    private List<TripStop> stops = new ArrayList<>();

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<TripStop> getStops() {
        return stops;
    }

    public void setStops(List<TripStop> stops) {
        this.stops = stops;
    }

    /**
     * Aggiunge una tappa al viaggio e imposta il riferimento bidirezionale.
     */
    public void addStop(TripStop stop) {
        stops.add(stop);
        stop.setTrip(this);
    }

    /**
     * Rimuove una tappa dal viaggio e pulisce il riferimento bidirezionale.
     */
    public void removeStop(TripStop stop) {
        stops.remove(stop);
        stop.setTrip(null);
    }
}
