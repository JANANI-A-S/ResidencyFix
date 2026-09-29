package com.residencyfix.service;

import com.residencyfix.dto.ComplaintDTO;
import com.residencyfix.model.Complaint;
import com.residencyfix.model.ComplaintStatus;

import java.util.List;

public interface ComplaintService {
    List<Complaint> getAllComplaints();
    List<Complaint> getComplaintsByStatus(ComplaintStatus status);
    Complaint getComplaintById(Long id);
    Complaint createComplaint(ComplaintDTO complaintDTO);
    Complaint updateComplaint(Long id, ComplaintDTO complaintDTO);
    Complaint updateComplaintStatus(Long id, ComplaintStatus status, String statusNote, String actorRole);
    void deleteComplaint(Long id);
    List<Complaint> searchComplaintByResidentName(String name);
    List<Complaint> searchComplaintByResidentNameAndStatus(String name, ComplaintStatus status);
    List<Complaint> getComplaintsByCategory(String category);
    List<Complaint> filterComplaints(String search, ComplaintStatus status, String category);
    
    // Core Feature 4: Warden views all open complaints sorted by age
    List<Complaint> getOpenComplaintsSortedByAge();

    // Core Feature 5: Auto-flag complaints open for more than 5 days as overdue
    List<Complaint> getOverdueComplaints();

    long countByStatus(ComplaintStatus status);
    long countByCategory(String category);
}
