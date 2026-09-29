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
                                 @RequestParam(value = "category", required = false) String category,
                                 @RequestParam(value = "search", required = false) String search,
                                 HttpSession session) {
        List<Complaint> complaints = complaintService.filterComplaints(search, status, category);

        String role = (String) session.getAttribute("loggedInRole");
        if (role == null) role = "resident";

        model.addAttribute("role", role);
        model.addAttribute("complaints", complaints);
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("categories", List.of("Plumbing", "Electrical", "Cleaning"));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("search", search);
        return "complaints";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session) {
        ComplaintDTO complaintDTO = new ComplaintDTO();
        String residentName = (String) session.getAttribute("residentName");
        complaintDTO.setResidentName(residentName);
        String room = (String) session.getAttribute("userRoom");
        if (room != null && !room.isBlank()) {
            complaintDTO.setRoomNumber(room);
        }
        complaintDTO.setStatus(ComplaintStatus.NEW);
        model.addAttribute("complaintDTO", complaintDTO);
        model.addAttribute("mode", "resident");
        model.addAttribute("showResidentName", residentName == null || residentName.isBlank());
        model.addAttribute("showStatus", false);
        model.addAttribute("showRoomNumber", true);
        model.addAttribute("showCategory", true);
        model.addAttribute("showDescription", true);
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("categories", List.of("Plumbing", "Electrical", "Cleaning"));
        return "complaint-form";
    }

    @PostMapping
    public String createComplaint(@Valid @ModelAttribute("complaintDTO") ComplaintDTO complaintDTO,
                                 BindingResult result,
                                 Model model,
                                 HttpSession session) {
        String residentName = (String) session.getAttribute("residentName");
        if (residentName == null || residentName.isBlank()) {
            residentName = complaintDTO.getResidentName();
        }
        complaintDTO.setResidentName(residentName);
        complaintDTO.setStatus(ComplaintStatus.NEW);

        if (residentName == null || residentName.isBlank()) {
            result.rejectValue("residentName", "error.residentName", "Resident name is required");
        }

        // Validate strictly the criteria mentioned in the problem statement
        if (complaintDTO.getCategory() == null || complaintDTO.getCategory().isBlank()) {
            result.rejectValue("category", "error.category", "Category is required");
        } else if (!complaintDTO.getCategory().equalsIgnoreCase("Plumbing") &&
                   !complaintDTO.getCategory().equalsIgnoreCase("Electrical") &&
                   !complaintDTO.getCategory().equalsIgnoreCase("Cleaning")) {
            result.rejectValue("category", "error.category", "Category must be Plumbing, Electrical, or Cleaning");
        }

        if (result.hasErrors()) {
            model.addAttribute("mode", "resident");
            model.addAttribute("showResidentName", residentName == null || residentName.isBlank());
            model.addAttribute("showStatus", false);
            model.addAttribute("showRoomNumber", true);
            model.addAttribute("showCategory", true);
            model.addAttribute("showDescription", true);
            model.addAttribute("statuses", ComplaintStatus.values());
            model.addAttribute("categories", List.of("Plumbing", "Electrical", "Cleaning"));
            return "complaint-form";
        }

        complaintService.createComplaint(complaintDTO);
        String role = (String) session.getAttribute("loggedInRole");
        if (role == null || role.isBlank() || "resident".equalsIgnoreCase(role)) {
            session.setAttribute("loggedInRole", "resident");
            session.setAttribute("residentName", residentName);
            session.setAttribute("userName", residentName);
            session.setAttribute("userRoom", complaintDTO.getRoomNumber());
            role = "resident";
        }
        if ("warden".equalsIgnoreCase(role)) {
            return "redirect:/dashboard/warden";
        }
        return "redirect:/dashboard/resident";
    }

    @GetMapping("/{id}")
    public String showComplaint(@PathVariable Long id, Model model, HttpSession session) {
        Complaint complaint = complaintService.getComplaintById(id);
        String role = (String) session.getAttribute("loggedInRole");
        if (role == null) role = "resident";
        model.addAttribute("complaint", complaint);
        model.addAttribute("role", role);
        return "complaint-details";
    }

    @GetMapping("/{id}/status")
    public String showStatusForm(@PathVariable Long id, Model model, HttpSession session) {
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
        String role = (String) session.getAttribute("loggedInRole");
        model.addAttribute("complaint", complaint);
        model.addAttribute("complaintDTO", complaintDTO);
        model.addAttribute("mode", "staff");
        model.addAttribute("role", role);
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
                                       @RequestParam(value = "statusNote", required = false) String statusNote,
                                       HttpSession session) {
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
        String role = (String) session.getAttribute("loggedInRole");
        if ("warden".equalsIgnoreCase(role)) {
            return "redirect:/dashboard/warden";
        }
        return "redirect:/dashboard/staff";
    }

    @PostMapping("/{id}/delete")
    public String deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return "redirect:/complaints";
    }
}
