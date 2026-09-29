package com.residencyfix.controller;

import com.residencyfix.dto.ComplaintDTO;
import com.residencyfix.model.Complaint;
import com.residencyfix.model.ComplaintStatus;
import com.residencyfix.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ApiComplaintController {

    private final ComplaintService complaintService;

    public ApiComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // Core Feature 1: Resident raises a complaint with category, room number and description
    @PostMapping
    public ResponseEntity<Complaint> createComplaint(@Valid @RequestBody ComplaintDTO complaintDTO) {
        Complaint created = complaintService.createComplaint(complaintDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Core Feature 2: Staff updates status: open, in-progress, resolved (Enforces business rules)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Complaint> updateStatus(@PathVariable Long id,
                                                 @RequestParam ComplaintStatus status,
                                                 @RequestParam(required = false) String statusNote,
                                                 @RequestParam(defaultValue = "staff") String role) {
        Complaint updated = complaintService.updateComplaintStatus(id, status, statusNote, role);
        return ResponseEntity.ok(updated);
    }

    // Core Feature 3: Resident can view the status of their own complaints
    @GetMapping("/resident/{residentName}")
    public List<Complaint> getComplaintsByResident(@PathVariable String residentName) {
        return complaintService.searchComplaintByResidentName(residentName);
    }

    // Core Feature 4: Warden views all open complaints sorted by age
    @GetMapping("/warden/open-by-age")
    public List<Complaint> getOpenComplaintsSortedByAge() {
        return complaintService.getOpenComplaintsSortedByAge();
    }

    // Core Feature 5: Auto-flag complaints open for more than 5 days as overdue
    @GetMapping("/overdue")
    public List<Complaint> getOverdueComplaints() {
        return complaintService.getOverdueComplaints();
    }

    // General REST Endpoints
    @GetMapping
    public List<Complaint> getAllComplaints() {
        return complaintService.getAllComplaints();
    }

    @GetMapping("/status/{status}")
    public List<Complaint> getComplaintsByStatus(@PathVariable ComplaintStatus status) {
        return complaintService.getComplaintsByStatus(status);
    }

    @GetMapping("/category/{category}")
    public List<Complaint> getComplaintsByCategory(@PathVariable String category) {
        return complaintService.getComplaintsByCategory(category);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Complaint> updateComplaint(@PathVariable Long id,
                                                   @Valid @RequestBody ComplaintDTO complaintDTO) {
        Complaint updated = complaintService.updateComplaint(id, complaintDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.noContent().build();
    }
}
