package com.residencyfix.repository;

import com.residencyfix.model.Complaint;
import com.residencyfix.model.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStatus(ComplaintStatus status);
    
    // Core Feature 4: Warden views all open complaints sorted by age (oldest first)
    List<Complaint> findByStatusOrderByCreatedAtAsc(ComplaintStatus status);

    List<Complaint> findByResidentNameContainingIgnoreCase(String residentName);
    List<Complaint> findByResidentNameContainingIgnoreCaseAndStatus(String residentName, ComplaintStatus status);
    
    List<Complaint> findByCategoryIgnoreCase(String category);
    List<Complaint> findByCategoryIgnoreCaseAndStatus(String category, ComplaintStatus status);

    // Core Feature 5: Auto-flag complaints open for more than 5 days as overdue
    List<Complaint> findByCreatedAtBeforeAndStatusNot(LocalDateTime cutoffDate, ComplaintStatus status);

    long countByStatus(ComplaintStatus status);
    long countByCategoryIgnoreCase(String category);
}
