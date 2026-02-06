package com.sobolev.travel.repository;

import com.sobolev.travel.entity.Poi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per l'entità POI personalizzati dell'utente.
 */
@Repository
public interface PoiRepository extends JpaRepository<Poi, Integer> {

    // Trova tutti i POI di un utente specifico
    List<Poi> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // Trova un POI per nome e utente
    Optional<Poi> findByUserIdAndNameIgnoreCase(Integer userId, String name);

    // Elimina tutti i POI di un utente
    void deleteByUserId(Integer userId);
}
