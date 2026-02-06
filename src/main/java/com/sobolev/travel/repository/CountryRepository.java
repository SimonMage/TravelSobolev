package com.sobolev.travel.repository;

import com.sobolev.travel.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository per l'entità Country.
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {

    // Cerca un paese per nome in modo case-insensitive
    Optional<Country> findByNameIgnoreCase(String name);
}
