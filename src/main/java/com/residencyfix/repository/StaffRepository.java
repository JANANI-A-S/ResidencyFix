package com.residencyfix.repository;

import com.residencyfix.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmailIgnoreCase(String email);
    Optional<Staff> findByNameIgnoreCase(String name);
}
