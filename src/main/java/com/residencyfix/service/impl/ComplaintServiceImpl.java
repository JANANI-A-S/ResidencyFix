package com.residencyfix.service.impl;

import com.residencyfix.dto.ComplaintDTO;
import com.residencyfix.model.Complaint;
import com.residencyfix.model.ComplaintStatus;
import com.residencyfix.repository.ComplaintRepository;
import com.residencyfix.service.ComplaintService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @Override
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    @Override
    public List<Complaint> getComplaintsByStatus(ComplaintStatus status) {
        return complaintRepository.findByStatus(status);
    }

    @Override
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));
    }

    @Override
    public Complaint createComplaint(ComplaintDTO complaintDTO) {
        Complaint complaint = new Complaint();
        complaint.setResidentName(complaintDTO.getResidentName());
        complaint.setRoomNumber(complaintDTO.getRoomNumber());
        complaint.setCategory(complaintDTO.getCategory());
        complaint.setDescription(complaintDTO.getDescription());
        complaint.setStatus(complaintDTO.getStatus() != null ? complaintDTO.getStatus() : ComplaintStatus.NEW);
        complaint.setStatusNote(complaintDTO.getStatusNote());
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        return complaintRepository.save(complaint);
    }

    @Override
    public Complaint updateComplaint(Long id, ComplaintDTO complaintDTO) {
        Complaint complaint = getComplaintById(id);
        complaint.setResidentName(complaintDTO.getResidentName());
        complaint.setRoomNumber(complaintDTO.getRoomNumber());
        complaint.setCategory(complaintDTO.getCategory());
        complaint.setDescription(complaintDTO.getDescription());
        complaint.setStatus(complaintDTO.getStatus() != null ? complaintDTO.getStatus() : complaint.getStatus());
        complaint.setStatusNote(complaintDTO.getStatusNote() != null ? complaintDTO.getStatusNote() : complaint.getStatusNote());
        complaint.setUpdatedAt(LocalDateTime.now());
        return complaintRepository.save(complaint);
    }

    @Override
    public void deleteComplaint(Long id) {
        Complaint complaint = getComplaintById(id);
        complaintRepository.delete(complaint);
    }

    @Override
    public List<Complaint> searchComplaintByResidentName(String name) {
        return complaintRepository.findByResidentNameContainingIgnoreCase(name);
    }

    @Override
    public List<Complaint> searchComplaintByResidentNameAndStatus(String name, ComplaintStatus status) {
        return complaintRepository.findByResidentNameContainingIgnoreCaseAndStatus(name, status);
    }
}
