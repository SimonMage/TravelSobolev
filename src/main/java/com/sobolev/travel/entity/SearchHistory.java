package com.sobolev.travel.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entità che rappresenta la cronologia di ricerca di un utente per una specifica città.
 *
 * Contiene riferimenti all'utente, alla città, alla query di ricerca e al timestamp.
 */
@Entity
@Table(name = "search_history")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(length = 255)
    private String query;

    @Column(name = "searched_at")
    private LocalDateTime searchedAt;

    /**
     * Callback JPA che imposta automaticamente il timestamp di ricerca alla creazione.
     * Questo evita che il chiamante debba fornire manualmente la data.
     */
    @PrePersist
    protected void onCreate() {
        searchedAt = LocalDateTime.now();
    }

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

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public LocalDateTime getSearchedAt() {
        return searchedAt;
    }

    public void setSearchedAt(LocalDateTime searchedAt) {
        this.searchedAt = searchedAt;
    }
}
