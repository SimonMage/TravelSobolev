package com.sobolev.travel.repository;

import com.sobolev.travel.entity.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository per TripStop con query per caricare POI e per verifiche di ownership.
 */
@Repository
public interface TripStopRepository extends JpaRepository<TripStop, Integer> {

    /**
     * Restituisce un TripStop dato il suo ID, caricando anche i POI associati.
     *
     * @param id L'ID del TripStop da cercare.
     * @return Un Optional contenente il TripStop trovato, o vuoto se non trovato.
     */
    @Query("SELECT ts FROM TripStop ts LEFT JOIN FETCH ts.pois WHERE ts.id = :id")
    Optional<TripStop> findByIdWithPois(@Param("id") Integer id);

    /**
     * Restituisce un TripStop dato il suo ID, l'ID del viaggio e l'ID dell'utente,
     * verificando che il viaggio appartenga all'utente.
     *
     * @param stopId L'ID del TripStop da cercare.
     * @param tripId L'ID del viaggio associato.
     * @param userId L'ID dell'utente proprietario del viaggio.
     * @return Un Optional contenente il TripStop trovato, o vuoto se non trovato.
     */
    @Query("SELECT ts FROM TripStop ts JOIN ts.trip t WHERE ts.id = :stopId AND t.id = :tripId AND t.user.id = :userId")
    Optional<TripStop> findByIdAndTripIdAndUserId(@Param("stopId") Integer stopId,
                                                   @Param("tripId") Integer tripId,
                                                   @Param("userId") Integer userId);

    /**
     * Restituisce un TripStop dato il nome della fermata e l'ID del viaggio,
     * caricando anche i POI e la città associati.
     *
     * @param stopName Il nome della fermata da cercare.
     * @param tripId L'ID del viaggio associato.
     * @return Un Optional contenente il TripStop trovato, o vuoto se non trovato.
     */
    @Query("SELECT ts FROM TripStop ts LEFT JOIN FETCH ts.pois LEFT JOIN FETCH ts.city WHERE LOWER(ts.stopName) = LOWER(:stopName) AND ts.trip.id = :tripId")
    Optional<TripStop> findByStopNameAndTripId(@Param("stopName") String stopName, @Param("tripId") Integer tripId);

    /**
     * Restituisce un TripStop dato il nome della fermata, il nome del viaggio e l'ID dell'utente,
     * verificando che il viaggio appartenga all'utente.
     *
     * @param stopName Il nome della fermata da cercare.
     * @param tripName Il nome del viaggio associato.
     * @param userId L'ID dell'utente proprietario del viaggio.
     * @return Un Optional contenente il TripStop trovato, o vuoto se non trovato.
     */
    @Query("SELECT ts FROM TripStop ts LEFT JOIN FETCH ts.pois LEFT JOIN FETCH ts.city JOIN ts.trip t WHERE LOWER(ts.stopName) = LOWER(:stopName) AND LOWER(t.name) = LOWER(:tripName) AND t.user.id = :userId")
    Optional<TripStop> findByStopNameAndTripNameAndUserId(@Param("stopName") String stopName, @Param("tripName") String tripName, @Param("userId") Integer userId);
}
