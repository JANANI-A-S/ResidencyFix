package com.residencyfix.repository;

import com.residencyfix.model.Resident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResidentRepository extends JpaRepository<Resident, Long> {
    Optional<Resident> findByEmailIgnoreCase(String email);
    Optional<Resident> findByNameIgnoreCase(String name);
}
