package com.sobolev.travel.repository;

import com.sobolev.travel.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository per l'entità UserProfile.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
}
