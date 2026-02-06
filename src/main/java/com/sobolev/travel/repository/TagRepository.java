package com.sobolev.travel.repository;

import com.sobolev.travel.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository per l'entità Tag.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    // Trova tag per nome (case-sensitive come da DB)
    Optional<Tag> findByName(String name);
}
