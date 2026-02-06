package com.sobolev.travel.repository;

import com.sobolev.travel.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per l'entità Trip.
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {

    // Recupera i viaggi di un utente ordinati per data di inizio (decrescente)
    List<Trip> findByUserIdOrderByStartDateDesc(Integer userId);

    // Carica un trip con le sue tappe e le città associate per evitare N+1
    @Query("SELECT t FROM Trip t LEFT JOIN FETCH t.stops s LEFT JOIN FETCH s.city WHERE t.id = :id")
    Optional<Trip> findByIdWithStops(@Param("id") Integer id);

    // Carica un trip con tutte le tappe e città associate (se autorizzato dall'utente)
    @Query("SELECT t FROM Trip t LEFT JOIN FETCH t.stops s LEFT JOIN FETCH s.city WHERE t.id = :id AND t.user.id = :userId")
    Optional<Trip> findByIdAndUserIdWithStopsAndPois(@Param("id") Integer id, @Param("userId") Integer userId);

    Optional<Trip> findByIdAndUserId(Integer id, Integer userId);

    @Query("SELECT t FROM Trip t LEFT JOIN FETCH t.stops WHERE LOWER(t.name) = LOWER(:name) AND t.user.id = :userId")
    Optional<Trip> findByNameAndUserIdIgnoreCase(@Param("name") String name, @Param("userId") Integer userId);
}
