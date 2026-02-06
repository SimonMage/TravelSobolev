package com.sobolev.travel.repository;

import com.sobolev.travel.entity.Poi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository per l'entità POI.
 */
@Repository
public interface PoiRepository extends JpaRepository<Poi, Integer> {

    // Cerca POI per externalId (identificativo del provider esterno)
    Optional<Poi> findByExternalId(String externalId);
}
