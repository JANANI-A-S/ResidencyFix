package com.residencyfix.controller;

import com.residencyfix.dto.ComplaintDTO;
import com.residencyfix.model.Complaint;
import com.residencyfix.model.ComplaintStatus;
import com.residencyfix.service.ComplaintService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    public String listComplaints(Model model,
                                 @RequestParam(value = "status", required = false) ComplaintStatus status,
                                 @RequestParam(value = "search", required = false) String search) {
        List<Complaint> complaints;

        if (status != null && search != null && !search.isBlank()) {
            complaints = complaintService.searchComplaintByResidentNameAndStatus(search, status);
        } else if (status != null) {
            complaints = complaintService.getComplaintsByStatus(status);
        } else if (search != null && !search.isBlank()) {
            complaints = complaintService.searchComplaintByResidentName(search);
        } else {
            complaints = complaintService.getAllComplaints();
        }

        model.addAttribute("complaints", complaints);
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        return "complaints";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        ComplaintDTO complaintDTO = new ComplaintDTO();
        complaintDTO.setResidentName("Resident");
        complaintDTO.setStatus(ComplaintStatus.NEW);
        model.addAttribute("complaintDTO", complaintDTO);
        model.addAttribute("mode", "resident");
        model.addAttribute("showResidentName", false);
        model.addAttribute("showStatus", false);
        model.addAttribute("showRoomNumber", true);
        model.addAttribute("showCategory", true);
        model.addAttribute("showDescription", true);
        model.addAttribute("statuses", ComplaintStatus.values());
        return "complaint-form";
    }

    @PostMapping
    public String createComplaint(@Valid @ModelAttribute("complaintDTO") ComplaintDTO complaintDTO,
                                 BindingResult result,
                                 Model model,
                                 HttpSession session) {
        String residentName = (String) session.getAttribute("residentName");
        if (residentName == null || residentName.isBlank()) {
            residentName = "Resident";
        }
        complaintDTO.setResidentName(residentName);
        complaintDTO.setStatus(ComplaintStatus.NEW);

        if (result.hasErrors()) {
            model.addAttribute("mode", "resident");
            model.addAttribute("showResidentName", false);
            model.addAttribute("showStatus", false);
            model.addAttribute("showRoomNumber", true);
            model.addAttribute("showCategory", true);
            model.addAttribute("showDescription", true);
            model.addAttribute("statuses", ComplaintStatus.values());
            return "complaint-form";
        }

        complaintService.createComplaint(complaintDTO);
        return "redirect:/dashboard/resident";
    }

    @GetMapping("/{id}")
    public String showComplaint(@PathVariable Long id, Model model) {
        Complaint complaint = complaintService.getComplaintById(id);
        model.addAttribute("complaint", complaint);
        return "complaint-details";
    }

    @GetMapping("/{id}/status")
    public String showStatusForm(@PathVariable Long id, Model model) {
        Complaint complaint = complaintService.getComplaintById(id);
        ComplaintDTO complaintDTO = new ComplaintDTO(
                complaint.getId(),
                complaint.getResidentName(),
                complaint.getRoomNumber(),
                complaint.getCategory(),
                complaint.getDescription(),
                complaint.getStatus(),
                complaint.getStatusNote()
        );
        model.addAttribute("complaintDTO", complaintDTO);
        model.addAttribute("mode", "staff");
        model.addAttribute("showResidentName", false);
        model.addAttribute("showStatus", true);
        model.addAttribute("showRoomNumber", false);
        model.addAttribute("showCategory", false);
        model.addAttribute("showDescription", false);
        model.addAttribute("statuses", ComplaintStatus.values());
        return "complaint-form";
    }

    @PostMapping("/{id}/status")
    public String updateComplaintStatus(@PathVariable Long id,
                                       @RequestParam ComplaintStatus status,
                                       @RequestParam(value = "statusNote", required = false) String statusNote) {
        Complaint complaint = complaintService.getComplaintById(id);
        complaint.setStatus(status);
        complaint.setStatusNote(statusNote);
        complaint.setUpdatedAt(LocalDateTime.now());

        ComplaintDTO complaintDTO = new ComplaintDTO(
                complaint.getId(),
                complaint.getResidentName(),
                complaint.getRoomNumber(),
                complaint.getCategory(),
                complaint.getDescription(),
                complaint.getStatus(),
                complaint.getStatusNote()
        );

        complaintService.updateComplaint(id, complaintDTO);
        return "redirect:/dashboard/staff";
    }

    @PostMapping("/{id}/delete")
    public String deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return "redirect:/complaints";
    }
}
