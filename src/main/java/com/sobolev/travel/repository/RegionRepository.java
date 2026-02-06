package com.sobolev.travel.repository;

import com.sobolev.travel.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per l'entità Region.
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {

    /**
     * Trova le regioni in base all'ID del paese.
     *
     * @param countryId l'ID del paese
     * @return la lista delle regioni corrispondenti
     */
    List<Region> findByCountryId(Integer countryId);

    /**
     * Trova le regioni in base al nome, ignorando la distinzione tra maiuscole e minuscole,
     * e recupera anche il paese associato.
     *
     * @param name il nome della regione
     * @return la lista delle regioni corrispondenti con il paese associato
     */
    @Query("SELECT r FROM Region r LEFT JOIN FETCH r.country WHERE LOWER(r.name) = LOWER(:name)")
    List<Region> findByNameIgnoreCaseWithCountry(@Param("name") String name);

    /**
     * Trova una regione in base al nome della regione e al nome del paese,
     * ignorando la distinzione tra maiuscole e minuscole.
     *
     * @param regionName  il nome della regione
     * @param countryName il nome del paese
     * @return la regione corrispondente, se trovata
     */
    @Query("SELECT r FROM Region r LEFT JOIN FETCH r.country c WHERE LOWER(r.name) = LOWER(:regionName) AND LOWER(c.name) = LOWER(:countryName)")
    Optional<Region> findByNameAndCountryNameIgnoreCase(@Param("regionName") String regionName, @Param("countryName") String countryName);
}
