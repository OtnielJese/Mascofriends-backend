package com.vetivet.repository;

import com.vetivet.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT o FROM Owner o WHERE o.active = true AND " +
           "(LOWER(o.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(o.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(o.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " o.phone LIKE CONCAT('%', :search, '%'))")
    List<Owner> searchOwners(String search);

    List<Owner> findByActiveTrue();
}
