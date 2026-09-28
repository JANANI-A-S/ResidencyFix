package com.residencyfix.controller;

import com.residencyfix.model.Complaint;
import com.residencyfix.model.ComplaintStatus;
import com.residencyfix.service.ComplaintService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final ComplaintService complaintService;

    public HomeController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", "ResidencyFix");
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("roleOptions", List.of("Resident", "Staff", "Warden"));
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String role,
                              @RequestParam(value = "email", required = false) String email,
                              HttpSession session) {
        String normalizedRole = role == null ? "resident" : role.trim().toLowerCase();
        session.setAttribute("loggedInRole", normalizedRole);

        if ("resident".equals(normalizedRole)) {
            String residentName = (email == null || email.isBlank()) ? "Resident" : email.split("@")[0];
            session.setAttribute("residentName", residentName);
        } else {
            session.removeAttribute("residentName");
        }

        return "redirect:/dashboard/" + normalizedRole;
    }

    @GetMapping("/dashboard/{role}")
    public String dashboard(@PathVariable String role,
                            HttpSession session,
                            Model model) {
        String normalizedRole = role == null ? "resident" : role.toLowerCase();
        String roleTitle = switch (normalizedRole) {
            case "staff" -> "Staff";
            case "warden" -> "Warden";
            default -> "Resident";
        };

        List<Complaint> complaints = complaintService.getAllComplaints();
        if ("resident".equals(normalizedRole)) {
            String residentName = (String) session.getAttribute("residentName");
            if (residentName != null && !residentName.isBlank()) {
                complaints = complaintService.searchComplaintByResidentName(residentName);
            } else {
                complaints = List.of();
            }
        }

        long open = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.NEW).count();
        long inProgress = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS).count();
        long resolved = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED).count();
        long overdue = complaints.stream().filter(c -> c.isOverdue()).count();

        model.addAttribute("role", normalizedRole);
        model.addAttribute("roleTitle", roleTitle);
        model.addAttribute("complaints", complaints);
        model.addAttribute("statuses", Arrays.asList(ComplaintStatus.values()));
        model.addAttribute("summary", Map.of(
                "open", open,
                "inProgress", inProgress,
                "resolved", resolved,
                "overdue", overdue
        ));
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("roleOptions", List.of("Resident", "Staff", "Warden"));
        model.addAttribute("profileName", "Aisha Patel");
        model.addAttribute("profileRole", "Resident");
        model.addAttribute("profileUnit", "Block A / Unit 204");
        return "profile";
    }
}
