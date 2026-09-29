package com.residencyfix.service.impl;

import com.residencyfix.dto.ComplaintDTO;
import com.residencyfix.exception.BusinessRuleException;
import com.residencyfix.exception.ResourceNotFoundException;
import com.residencyfix.exception.UnauthorizedActionException;
import com.residencyfix.model.Category;
import com.residencyfix.model.Complaint;
import com.residencyfix.model.ComplaintStatus;
import com.residencyfix.model.Resident;
import com.residencyfix.repository.CategoryRepository;
import com.residencyfix.repository.ComplaintRepository;
import com.residencyfix.repository.ResidentRepository;
import com.residencyfix.repository.StaffRepository;
import com.residencyfix.service.ComplaintService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CategoryRepository categoryRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository,
                                CategoryRepository categoryRepository,
                                ResidentRepository residentRepository,
                                StaffRepository staffRepository) {
        this.complaintRepository = complaintRepository;
        this.categoryRepository = categoryRepository;
        this.residentRepository = residentRepository;
        this.staffRepository = staffRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
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

        // Associate with Category entity if present
        if (complaintDTO.getCategory() != null && !complaintDTO.getCategory().isBlank()) {
            Category category = categoryRepository.findByNameIgnoreCase(complaintDTO.getCategory().trim())
                    .orElseGet(() -> categoryRepository.save(new Category(complaintDTO.getCategory().trim(), "Maintenance Category")));
            complaint.setCategoryEntity(category);
        }

        // Associate with Resident entity if present
        if (complaintDTO.getResidentName() != null && !complaintDTO.getResidentName().isBlank()) {
            residentRepository.findByNameIgnoreCase(complaintDTO.getResidentName().trim())
                .ifPresent(complaint::setResident);
        }

        return complaintRepository.save(complaint);
    }

    @Override
    public Complaint updateComplaint(Long id, ComplaintDTO complaintDTO) {
        Complaint complaint = getComplaintById(id);

        if (complaintDTO.getStatus() != null && complaintDTO.getStatus() != complaint.getStatus()) {
            validateStatusTransition(complaint.getStatus(), complaintDTO.getStatus(), complaintDTO.getStatusNote(), "staff");
            complaint.setStatus(complaintDTO.getStatus());
        }

        if (complaintDTO.getResidentName() != null) {
            complaint.setResidentName(complaintDTO.getResidentName());
        }
        if (complaintDTO.getRoomNumber() != null) {
            complaint.setRoomNumber(complaintDTO.getRoomNumber());
        }
        if (complaintDTO.getCategory() != null) {
            complaint.setCategory(complaintDTO.getCategory());
        }
        if (complaintDTO.getDescription() != null) {
            complaint.setDescription(complaintDTO.getDescription());
        }
        if (complaintDTO.getStatusNote() != null) {
            complaint.setStatusNote(complaintDTO.getStatusNote());
        }
        complaint.setUpdatedAt(LocalDateTime.now());

        return complaintRepository.save(complaint);
    }

    /**
     * Enforce Problem Statement Business Rules:
     * 1. A complaint can only move forward through the defined status sequence, never backward without a remark.
     * 2. Only assigned staff or the warden can mark a complaint resolved.
     */
    @Override
    public Complaint updateComplaintStatus(Long id, ComplaintStatus newStatus, String statusNote, String actorRole) {
        Complaint complaint = getComplaintById(id);
        ComplaintStatus currentStatus = complaint.getStatus();

        validateStatusTransition(currentStatus, newStatus, statusNote, actorRole);

        complaint.setStatus(newStatus);
        if (statusNote != null && !statusNote.isBlank()) {
            complaint.setStatusNote(statusNote);
        }
        complaint.setUpdatedAt(LocalDateTime.now());

        return complaintRepository.save(complaint);
    }

    private void validateStatusTransition(ComplaintStatus currentStatus, ComplaintStatus newStatus, String statusNote, String actorRole) {
        if (currentStatus == newStatus) {
            return;
        }

        // Business Rule 2: Only assigned staff or the warden can mark a complaint resolved
        if (newStatus == ComplaintStatus.RESOLVED) {
            if (actorRole == null || (!actorRole.equalsIgnoreCase("staff") && !actorRole.equalsIgnoreCase("warden"))) {
                throw new UnauthorizedActionException("Only assigned staff or the hostel warden can mark a complaint as resolved.");
            }
        }

        // Business Rule 1: A complaint can only move forward through the defined status sequence, never backward without a remark.
        int currentStep = getStatusStep(currentStatus);
        int newStep = getStatusStep(newStatus);

        if (newStep < currentStep) {
            // Moving backward requires an explanatory remark
            if (statusNote == null || statusNote.trim().isEmpty()) {
                throw new BusinessRuleException("A complaint cannot move backward in status from " 
                        + currentStatus.getLabel() + " to " + newStatus.getLabel() + " without an explanatory remark/note.");
            }
        }
    }

    private int getStatusStep(ComplaintStatus status) {
        if (status == null) return 1;
        return switch (status) {
            case NEW -> 1;
            case IN_PROGRESS -> 2;
            case RESOLVED -> 3;
            case REJECTED -> 4;
        };
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

    @Override
    public List<Complaint> getComplaintsByCategory(String category) {
        return complaintRepository.findByCategoryIgnoreCase(category);
    }

    @Override
    public List<Complaint> filterComplaints(String search, ComplaintStatus status, String category) {
        List<Complaint> list = complaintRepository.findAll();
        return list.stream()
                .filter(c -> search == null || search.isBlank() ||
                        (c.getResidentName() != null && c.getResidentName().toLowerCase().contains(search.toLowerCase().trim())) ||
                        (c.getRoomNumber() != null && c.getRoomNumber().toLowerCase().contains(search.toLowerCase().trim())) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(search.toLowerCase().trim())))
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> category == null || category.isBlank() || category.equalsIgnoreCase("all") ||
                        (c.getCategory() != null && c.getCategory().equalsIgnoreCase(category.trim())))
                .sorted(Comparator.comparing(Complaint::getCreatedAt).reversed())
                .toList();
    }

    // Core Feature 4: Warden views all open complaints sorted by age
    @Override
    public List<Complaint> getOpenComplaintsSortedByAge() {
        return complaintRepository.findAll().stream()
                .filter(c -> c.getStatus() != ComplaintStatus.RESOLVED && c.getStatus() != ComplaintStatus.REJECTED)
                .sorted(Comparator.comparing(Complaint::getCreatedAt)) // Oldest first
                .toList();
    }

    // Core Feature 5: Auto-flag complaints open for more than 5 days as overdue
    @Override
    public List<Complaint> getOverdueComplaints() {
        return complaintRepository.findAll().stream()
                .filter(Complaint::isOverdue)
                .sorted(Comparator.comparing(Complaint::getCreatedAt))
                .toList();
    }

    @Override
    public long countByStatus(ComplaintStatus status) {
        return complaintRepository.countByStatus(status);
    }

    @Override
    public long countByCategory(String category) {
        return complaintRepository.countByCategoryIgnoreCase(category);
    }
}
