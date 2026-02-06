package com.sobolev.travel.repository;

import com.sobolev.travel.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository per la cronologia delle ricerche (SearchHistory).
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Integer> {

    // Recupera la cronologia di un utente con la città associata ordinata per data decrescente
    @Query("SELECT sh FROM SearchHistory sh LEFT JOIN FETCH sh.city WHERE sh.user.id = :userId ORDER BY sh.searchedAt DESC")
    List<SearchHistory> findByUserIdOrderBySearchedAtDesc(@Param("userId") Integer userId);

    // Elimina la cronologia di un utente
    void deleteByUserId(Integer userId);
}
