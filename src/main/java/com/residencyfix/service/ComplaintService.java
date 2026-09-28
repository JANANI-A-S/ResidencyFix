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
    void deleteComplaint(Long id);
    List<Complaint> searchComplaintByResidentName(String name);
    List<Complaint> searchComplaintByResidentNameAndStatus(String name, ComplaintStatus status);
}
