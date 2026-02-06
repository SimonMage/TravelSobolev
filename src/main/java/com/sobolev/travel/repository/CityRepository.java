package com.sobolev.travel.repository;

import com.sobolev.travel.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per l'entità City.
 *
 * Contiene query JPA custom per caricare relazioni (tags, region, country) evitando il problema N+1
 * quando necessario (LEFT JOIN FETCH).
 */
@Repository
public interface CityRepository extends JpaRepository<City, Integer> {

    // Trova città per region id
    List<City> findByRegionId(Integer regionId);

    // Recupera una city con i tag caricati (utile per visualizzare categorie senza query aggiuntive)
    @Query("SELECT c FROM City c LEFT JOIN FETCH c.tags WHERE c.id = :id")
    Optional<City> findByIdWithTags(@Param("id") Integer id);

    // Trova città che abbiano almeno uno dei tag specificati
    @Query("SELECT DISTINCT c FROM City c JOIN c.tags t WHERE t.name IN :tagNames")
    List<City> findByTagNames(@Param("tagNames") List<String> tagNames);

    List<City> findByNameContainingIgnoreCase(String name);

    // Recupera city con tag, region e country per dettagli completi
    @Query("SELECT c FROM City c LEFT JOIN FETCH c.tags LEFT JOIN FETCH c.region r LEFT JOIN FETCH r.country WHERE LOWER(c.name) = LOWER(:name)")
    List<City> findByNameIgnoreCaseWithDetails(@Param("name") String name);

    // Cerca per nome città e nome regione (case-insensitive)
    @Query("SELECT c FROM City c LEFT JOIN FETCH c.tags LEFT JOIN FETCH c.region r LEFT JOIN FETCH r.country WHERE LOWER(c.name) = LOWER(:cityName) AND LOWER(r.name) = LOWER(:regionName)")
    Optional<City> findByCityNameAndRegionNameIgnoreCase(@Param("cityName") String cityName, @Param("regionName") String regionName);
}
