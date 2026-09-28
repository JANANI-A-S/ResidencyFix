package com.residencyfix.repository;

import com.residencyfix.model.Complaint;
import com.residencyfix.model.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStatus(ComplaintStatus status);
    List<Complaint> findByResidentNameContainingIgnoreCase(String residentName);
    List<Complaint> findByResidentNameContainingIgnoreCaseAndStatus(String residentName, ComplaintStatus status);
}
